package net.square.module.impl.combat.assist;

import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.square.Visage;
import net.square.module.VisageCheck;
import net.square.storage.DataStorage;
import net.square.utilities.math.MathUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import static net.square.utilities.math.MathUtils.*;

public class AssistCheck extends VisageCheck {

    public AssistCheck(Visage visage) {
        super(visage, "Assist");
    }

    @Override
    public void handleIn(Player player, PacketReceiveEvent event) {

        if (!this.isEnabled())
            return;
        DataStorage storage = getDataStorageOf(player);
        PacketPlayInFlying actualPacket = (PacketPlayInFlying) event.getPacket();
        /* PacketPlayInFlying#d() returns yaw as float */
        float yaw = actualPacket.d();
        /* PacketPlayInFlying#e() returns pitch as float */
        float pitch = actualPacket.e();

        float deltaYaw = Math.abs(yaw - storage.lastYaw);
        float deltaPitch = Math.abs(pitch - storage.lastPitch);

        this.irregularMovement(player, storage, deltaPitch, deltaYaw);
        this.roundedRotation(player, storage, deltaPitch, deltaYaw);
        this.checkDivisor(player, storage, deltaPitch, deltaYaw);
        this.extremeSmoothRots(player, storage, deltaYaw);
        if (event.getType().equals(Packet.Client.POSITION_LOOK)) {
            this.checkGenericFlaw(player, storage, yaw, deltaYaw);
        }

        storage.yawAccel = (deltaYaw - storage.lastYawDelta);
        storage.pitchAccel = (deltaPitch - storage.lastPitchDelta);
        storage.lastYawDelta = deltaYaw;
        storage.lastPitchDelta = deltaPitch;
        storage.lastPitch = pitch;
        storage.lastYaw = yaw;
    }

    private void checkGenericFlaw(Player player, DataStorage storage, float rotationYaw, float deltaYaw) {
        Entity target = storage.lastAuraEntity;
        if (target == null) {
            return;
        }
        Location origin = player.getLocation().clone();
        Vector end = target.getLocation().clone().toVector();
        float optimalYaw = origin.setDirection(end.subtract(origin.toVector())).getYaw() % 360.0f;
        float fixedRotYaw = (rotationYaw % 360.0f + 360.0f) % 360.0f;
        double difference = Math.abs(fixedRotYaw - optimalYaw);
        if (deltaYaw > 3.0f) {
            storage.differenceSamples.add(difference);
        }
        if (storage.differenceSamples.isFull()) {
            final double average = MathUtils.getAverage(storage.differenceSamples);
            final double deviation = MathUtils.getStandardDeviation(storage.differenceSamples);
            final boolean invalid = average < 7.0 && deviation < 12.0;
            if (invalid) {
                if (storage.assistA6Threshold++ > 3) {
                    this.markPlayer(player, 1, "Assist", "generic flaw in aim", "A6");
                }
            } else {
                storage.assistA6Threshold -= storage.assistA6Threshold > 0 ? 1 : 0;
            }
            this.debug(
                player, String.format("avg=%.3f deviation=%.3f buf=%d", average, deviation, storage.assistA6Threshold));
        }
    }

    private void extremeSmoothRots(Player player, DataStorage storage, float yawDelta) {
        float yawAccel = (float) storage.yawAccel;
        float pitchAccel = (float) storage.pitchAccel;

        final float deltaYaw = yawDelta % 360F;

        storage.yawAccelSamples.add(yawAccel);
        storage.pitchAccelSamples.add(pitchAccel);

        if (storage.yawAccelSamples.isFull() && storage.pitchAccelSamples.isFull()) {
            final double yawAccelAverage = getAverage(storage.yawAccelSamples);
            final double pitchAccelAverage = getAverage(storage.pitchAccelSamples);

            final double yawAccelDeviation = getVariance(storage.yawAccelSamples);
            final double pitchAccelDeviation = getVariance(storage.pitchAccelSamples);

            final boolean exemptRotation = deltaYaw < 1.5F;
            final boolean averageInvalid = yawAccelAverage < 1 || pitchAccelAverage < 1 && !exemptRotation;
            final boolean deviationInvalid = yawAccelDeviation < (5 * 5) && pitchAccelDeviation > (5 * 5)
                && !exemptRotation;

            if (averageInvalid && deviationInvalid) {
                if (storage.assistA6Threshold++ > 6) {
                    this.markPlayer(player, 1, "Assist", "rotated extreme smooth", "A6");
                }
            } else {
                storage.assistA6Threshold -= storage.assistA6Threshold > 0 ? 1 : 0;
            }
        }
    }

    private void checkDivisor(Player player, DataStorage storage, float deltaPitch, float yaw) {

        final float deltaYaw = yaw % 360F;

        final double divisorYaw = getGcd(
            (long) (deltaYaw * EXPANDER), (long) (storage.lastYawDelta * EXPANDER));
        final double divisorPitch = getGcd(
            (long) (deltaPitch * EXPANDER), (long) (storage.lastPitchDelta * EXPANDER));

        final double constantYaw = divisorYaw / EXPANDER;
        final double constantPitch = divisorPitch / EXPANDER;

        final double currentX = deltaYaw / constantYaw;
        final double currentY = deltaPitch / constantPitch;

        final double previousX = storage.lastYawDelta / constantYaw;
        final double previousY = storage.lastPitchDelta / constantPitch;

        if (deltaYaw > 0.0 && deltaPitch > 0.0 && deltaYaw < 20.f && deltaPitch < 20.f) {
            final double moduloX = currentX % previousX;
            final double moduloY = currentY % previousY;

            final double floorModuloX = Math.abs(Math.floor(moduloX) - moduloX);
            final double floorModuloY = Math.abs(Math.floor(moduloY) - moduloY);

            final boolean invalidX = moduloX > 90.d && floorModuloX > 0.1;
            final boolean invalidY = moduloY > 90.d && floorModuloY > 0.1;

            if (invalidX && invalidY) {
                if (storage.assistA4Threshold++ > 10) {
                    this.markPlayer(player, 1, "Assist", "invalid divisor", "A4");

                }
            } else {
                storage.assistA4Threshold -= storage.assistA4Threshold > 0 ? 1 : 0;
            }
        }

    }

    private void roundedRotation(Player player, DataStorage storage, float deltaPitch, float deltaYaw) {

        final float customFloat = deltaYaw % 360F;

        final boolean invalid = (deltaPitch % 1 == 0 || customFloat % 1 == 0) && deltaPitch != 0 && customFloat != 0;
        if (invalid) {
            if (storage.assistA2Threshold++ > 10) {
                this.markPlayer(player, 1, "Assist", "rounded movement pattern", "A2");
            }
        } else {
            storage.assistA2Threshold -= storage.assistA2Threshold > 0 ? 1 : 0;
        }
    }

    private void irregularMovement(Player player, DataStorage storage, float deltaPitch, float deltaYaw) {

        final float pitch = player.getLocation().getPitch();

        final boolean invalidPitch = deltaPitch < 0.009 && validRotation(deltaYaw);
        final boolean invalidYaw = deltaYaw < 0.009 && validRotation(deltaPitch);

        final boolean invalid = (invalidPitch || invalidYaw) && pitch < 89f;
        if (invalid) {
            if (storage.assistA1Threshold++ > 13) {
                this.markPlayer(player, 1, "Assist", "provocative movement pattern", "A1");
            }
        } else {
            storage.assistA1Threshold -= storage.assistA1Threshold > 0 ? 1 : 0;
        }
    }

    private boolean validRotation(float rotation) {
        return rotation > 2F && rotation < 35F;
    }

    @Override
    public void handleOut(Player player, PacketSendEvent event) {
    }

    @Override
    public boolean isTypeToLookFor(PacketReceiveEvent event) {
        String type = event.getType();
        return type.equals(Packet.Client.LOOK) || type.equals(Packet.Client.POSITION_LOOK)
            || type.equals(Packet.Client.POSITION);
    }

    @Override
    public boolean isTypeToLookFor(PacketSendEvent event) {
        return false;
    }
}
