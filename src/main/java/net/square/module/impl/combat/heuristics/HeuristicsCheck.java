package net.square.module.impl.combat.heuristics;

import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import net.square.Visage;
import net.square.module.VisageCheck;
import net.square.storage.DataStorage;
import net.square.utilities.math.MathUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class HeuristicsCheck extends VisageCheck {

    public HeuristicsCheck(Visage visage) {
        super(visage, "Heuristics");
    }

    @EventHandler
    public void handle(PlayerMoveEvent event) {
        if (!this.isEnabled())
            return;

        final DataStorage storage = getDataStorageOf(event.getPlayer());

        if (System.currentTimeMillis() - storage.lastAttackTime > 2000L)
            return;

        if (storage.lastEntity == null)
            return;

        this.checkSprintBehaviour(event, storage);
        this.checkYawDifference(event, storage);
        this.checkDeltaCombat(event, storage);
        this.checkCombatPitch(event, storage);
        this.checkHeuristics(event, storage);
    }

    public void checkHeuristics(final PlayerMoveEvent event, DataStorage storage) {
        ++storage.totalMoves;
        Location to = event.getTo();
        Location from = event.getFrom();
        //todo: use Location#distanceSquared to reduce Math.sqrt() calls
        if (to.getYaw() == from.getYaw() && to.getPitch() == from.getPitch() && to.distance(from) > 0.0) {
            ++storage.totalAimMoves;
        }
        //todo: use Location#distanceSquared to reduce Math.sqrt() calls
        if ((to.getYaw() != from.getYaw() || to.getPitch() != from.getPitch()) && to.distance(from) > 0.0) {
            ++storage.totalAimPosLook;
        }
        if (storage.totalMoves == 50) {
            if (storage.totalAimMoves >= 25 && storage.totalAimPosLook <= 35) {
                if (++storage.heuristicsA6Threshold > 4) {
                    markPlayer(event.getPlayer(), 1, "Heuristics", "moved invalid", "H6");
                }
            } else {
                storage.heuristicsA6Threshold -= ((storage.heuristicsA6Threshold > 0) ? 1 : 0);
            }
            storage.totalMoves = 0;
            storage.totalAimMoves = 0;
            storage.totalAimPosLook = 0;
        }
    }

    public void checkCombatPitch(final PlayerMoveEvent event, final DataStorage storage) {
        final float deltaYaw = event.getTo().getYaw() % 360.0f - event.getFrom().getYaw() % 360.0f;
        final float deltaPitch = Math.abs(event.getTo().getPitch() - event.getFrom().getPitch());
        final double expander = Math.pow(2.0, 24.0);
        if (deltaYaw == 0.0f || deltaPitch == 0.0f) {
            return;
        }
        final float gcd = (float) MathUtils.getVictim(
            (long) (deltaPitch * expander), (long) (storage.lastAimPitch * expander));
        if (gcd < 131072.0f) {
            if (++storage.heuristicsA5Threshold > 20) {
                storage.heuristicsA5Threshold = 0;
                markPlayer(event.getPlayer(), 1, "Heuristics",
                           "rotated invalid (gcd=" + gcd + ")", "H5"
                );
            }
        } else if (storage.heuristicsA5Threshold > 0) {
            --storage.heuristicsA5Threshold;
        }
        storage.lastAimPitch = deltaPitch;
    }

    public void checkDeltaCombat(final PlayerMoveEvent event, DataStorage storage) {
        final float deltaYaw = event.getTo().getYaw() % 360.0f - event.getFrom().getYaw() % 360.0f;
        final float deltaPitch = Math.abs(event.getTo().getPitch() - event.getFrom().getPitch());
        if (deltaPitch < 0.1 && deltaYaw > 3.5) {
            if (++storage.heuristicsA4Threshold > 10) {
                storage.heuristicsA4Threshold = 0;
                debug(event.getPlayer(), String.format("deltaYaw=%.2f, deltaPitch=%.2f", deltaYaw, deltaPitch));
                markPlayer(event.getPlayer(), 1, "Heuristics", "moved like pattern", "H4");
            }
        } else {
            storage.heuristicsA4Threshold = ((storage.heuristicsA4Threshold > 0) ? 1 : 0);
        }
    }

    public void checkYawDifference(final PlayerMoveEvent event, DataStorage storage) {
        final float deltaYaw = event.getTo().getYaw() % 360.0f - event.getFrom().getYaw() % 360.0f;
        final float deltaPitch = Math.abs(event.getTo().getPitch() - event.getFrom().getPitch());
        if (deltaYaw > 15.0f || deltaPitch > 15.0f) {
            if (++storage.heuristicsA3Threshold > 5) {
                debug(event.getPlayer(), "dy=" + deltaYaw);
                markPlayer(event.getPlayer(), 1, "Heuristics", "moved like pattern", "H3");
            }
        } else if (storage.heuristicsA3Threshold > 0) {
            --storage.heuristicsA3Threshold;
        }
    }

    private void checkSprintBehaviour(PlayerMoveEvent event, DataStorage storage) {
        if (System.currentTimeMillis() - storage.lastAttackTime > 100L) {
            return;
        }
        if (!(storage.lastEntity instanceof Player)) {
            return;
        }

        final double movement = Math.hypot(
            event.getTo().getX() - event.getFrom().getX(), event.getTo().getZ() - event.getFrom().getZ());
        final double accel = Math.abs(movement - storage.lastMovement);
        if ((event.getPlayer().isSprinting() || movement > 0.27) && accel < 0.01) {
            if (++storage.heuristicsA1Threshold > 15) {
                debug(event.getPlayer(), "accel=" + accel);
                markPlayer(event.getPlayer(), 1, "Heuristics",
                           "moved invalid (accel=" + accel + ")", "H1"
                );
            }
        } else if (storage.heuristicsA1Threshold > 0) {
            storage.heuristicsA1Threshold -= 3;
        }
        storage.lastMovement = movement;
    }

    @EventHandler
    public void handle(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();

        final DataStorage storage = getDataStorageOf(player);

        storage.lastEntity = event.getEntity();
        storage.lastAttackTime = System.currentTimeMillis();
    }

    @Override
    public void handleIn(Player player, PacketReceiveEvent event) {
    }

    @Override
    public void handleOut(Player player, PacketSendEvent event) {
    }

    @Override
    public boolean isTypeToLookFor(PacketReceiveEvent event) {
        return false;
    }

    @Override
    public boolean isTypeToLookFor(PacketSendEvent event) {
        return false;
    }
}
