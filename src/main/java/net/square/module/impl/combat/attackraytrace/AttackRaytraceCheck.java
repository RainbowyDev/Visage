package net.square.module.impl.combat.attackraytrace;

import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.square.Visage;
import net.square.module.VisageCheck;
import net.square.storage.DataStorage;
import net.square.utilities.raytracing.AABB;
import net.square.utilities.raytracing.Ray;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class AttackRaytraceCheck extends VisageCheck {

    public AttackRaytraceCheck(Visage visage) {
        super(visage, "AttackRaytrace");
    }

    @Override
    public void handleIn(Player player, PacketReceiveEvent event) {

        if (!this.isEnabled())
            return;

        String type = event.getType();
        if (type.equals(Packet.Client.USE_ENTITY)) {

            PacketPlayInUseEntity packetPlayInUseEntity = (PacketPlayInUseEntity) event.getPacket();
            Entity entity = packetPlayInUseEntity.a(((CraftWorld) player.getWorld()).getHandle()).getBukkitEntity();
            if (!(entity instanceof LivingEntity)) {
                return;
            }

            DataStorage dataStorage = getDataStorageOf(player);
            dataStorage.lastCombatEntity = (LivingEntity) entity;
            dataStorage.inCombat = true;

        } else if (type.equals(Packet.Client.POSITION_LOOK)) {

            DataStorage storage = getDataStorageOf(player);

            if (!storage.inCombat)
                return;
            if (storage.distances.size() >= 5) {
                storage.distances.remove(0);
            }
            if (storage.elapsed(System.nanoTime() / 1000000, storage.lastFlying) <= 500) {
                storage.inCombat = false;

                Ray ray = Ray.from(player);
                double dist = AABB.from(storage.lastCombatEntity).collidesD(ray, 0, 10);

                if (dist != -1) {
                    storage.distances.add(dist);
                }

                if (storage.distances.size() >= 5) {
                    double total = 0;
                    double avgReach = 0;

                    for (int i = 0; i < storage.distances.size(); i++) {
                        total += storage.distances.get(i);
                        avgReach = total / storage.distances.size();
                    }

                    debug(player, String.format("avgReach: %s dist: %s", avgReach, dist));

                    if (avgReach >= (double) getSettings().get("avgReach") &&
                        dist >= (double) getSettings().get("dist")) {
                        if (++storage.attackRaytraceThreshold > (int) getSettings().get("threshold")) {
                            markPlayer(player, 1, "AttackRaytrace",
                                       "hitting farther than possible d: " + avgReach, "A1"
                            );
                        }
                    } else
                        storage.attackRaytraceThreshold = 0;
                }
            }
            storage.lastFlying = System.nanoTime() / 1000000;
        }
    }

    @Override
    public void handleOut(Player player, PacketSendEvent event) {
    }

    @Override
    public boolean isTypeToLookFor(PacketReceiveEvent event) {
        String type = event.getType();
        return type.equals(Packet.Client.USE_ENTITY) || type.equals(Packet.Client.POSITION_LOOK);
    }

    @Override
    public boolean isTypeToLookFor(PacketSendEvent event) {
        return false;
    }
}
