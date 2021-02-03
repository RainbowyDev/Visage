package net.square.module.impl.combat.killaura;

import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import net.square.Visage;
import net.square.module.VisageCheck;
import net.square.storage.DataStorage;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class KillAuraCheck extends VisageCheck {

    public KillAuraCheck(Visage visage) {
        super(visage, "KillAura");
    }

    @Override
    public void handleIn(Player player, PacketReceiveEvent event) {

        if (!this.isEnabled())
            return;

        DataStorage storage = getDataStorageOf(player);

        switch (event.getType()) {
            case Packet.Client.ARM_ANIMATION:

                this.checkHitMiss(player, storage);

                break;
            case Packet.Client.USE_ENTITY:

                storage.hits++;
                this.checkWallHit(player, storage, event);

                break;
        }
    }

    private void checkHitMiss(Player player, DataStorage storage) {
        if (++storage.swings >= 100) {
            debug(player, String.format("§9§l%d%% of attempted hits were landed.", storage.hits));
            if (storage.hits >= 97) {
                this.markPlayer(player, 1, "KillAura",
                                "landed " + storage.hits + "% hits in 100 ticks", "A3"
                );
            }
            storage.swings = 0;
            storage.hits = 0;
        }
    }

    private void checkWallHit(Player player, DataStorage storage, PacketReceiveEvent event) {
        final Entity target = storage.lastAuraEntity;

        if (target == null || player == null)
            return;

        final Location attackerLocation = player.getLocation();

        final float yaw = player.getLocation().getYaw() % 360F;
        final float pitch = player.getLocation().getPitch();

        if (storage.lastAttackLocation != null) {
            final boolean check = yaw != storage.lastAuraYaw &&
                pitch != storage.lastAuraPitch &&
                //todo: Location#distanceSquared
                attackerLocation.distance(storage.lastAttackLocation) > 0.1;

            if (check && !player.hasLineOfSight(target)) {
                if (storage.auraA2Threshold++ > 10) {
                    this.markPlayer(player, 1, "KillAura",
                                    "hits entity out of his line of sight", "A2"
                    );
                }
            } else {
                storage.auraA2Threshold -= storage.auraA2Threshold > 0 ? 1 : 0;
            }
        }

        storage.lastAttackLocation = player.getLocation();

        storage.lastAuraYaw = yaw;
        storage.lastAuraPitch = pitch;
    }

    @EventHandler
    public void handle(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();

        final DataStorage storage = getDataStorageOf(player);

        storage.lastAuraEntity = event.getEntity();
        storage.lastEntityHit = System.currentTimeMillis();
    }

    @Override
    public void handleOut(Player player, PacketSendEvent event) {
    }

    @Override
    public boolean isTypeToLookFor(PacketReceiveEvent event) {
        String type = event.getType();
        return type.equals(Packet.Client.USE_ENTITY) || type.equals(Packet.Client.ARM_ANIMATION);
    }

    @Override
    public boolean isTypeToLookFor(PacketSendEvent event) {
        return false;
    }
}
