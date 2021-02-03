package net.square.module.impl.combat.reach;

import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.MathUtils;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.square.Visage;
import net.square.module.VisageCheck;
import net.square.storage.DataStorage;
import net.square.utilities.location.CustomLocation;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ReachCheck extends VisageCheck {

    public ReachCheck(Visage visage) {
        super(visage, "Reach");
    }

    @Override
    public void handleIn(Player player, PacketReceiveEvent event) {
        if (!this.isEnabled())
            return;
        if (this.shouldCancel(player))
            return;

        String type = event.getType();
        if (type.equals(Packet.Client.KEEP_ALIVE)) {

            DataStorage dataStorage = getDataStorageOf(player);
            dataStorage.ping = System.currentTimeMillis() - dataStorage.lastServerKeepAlive;

        } else if (type.equals(Packet.Client.USE_ENTITY)) {
            PacketPlayInUseEntity packetPlayInUseEntity = (PacketPlayInUseEntity) event.getPacket();

            if (packetPlayInUseEntity.a() != PacketPlayInUseEntity.EnumEntityUseAction.ATTACK) {
                return;
            }

            Entity entity = packetPlayInUseEntity.a(((CraftWorld) player.getWorld()).getHandle()).getBukkitEntity();

            if (!(entity instanceof LivingEntity)) {
                return;
            }

            DataStorage dataStorage = getDataStorageOf(player);
            dataStorage.livingEntity = (LivingEntity) entity;

            Location origin = player.getLocation();

            List<Vector> pastLocations = new ArrayList<>();
            for (CustomLocation pastLocation : dataStorage.entityPastLocations
                .getEstimatedLocation(dataStorage.ping, 150)) {
                pastLocations.add(pastLocation.toVector());
            }

            boolean seen = false;
            double best = 0;
            for (Vector vec : pastLocations) {
                //todo: Location#distanceSquared
                double v = vec.clone().setY(0).distance(origin.toVector().clone().setY(0)) - 0.3f;
                if (!seen || Double.compare(v, best) < 0) {
                    seen = true;
                    best = v;
                }
            }
            double distance = seen ? best : 0;

            double max = (double) getSettings().get("max");
            debug(player, String.format(
                "max: %s dist: %s threshold: %d",
                max, MathUtils.round(distance, 4), dataStorage.reachThreshold
            ));

            if (distance > max) {
                if (dataStorage.reachThreshold++ > (int) getSettings().get("threshold")) {
                    markPlayer(
                        player, 1, "Reach", String.format("hits over too great a distance d: %s > %s", distance, max),
                        "R1"
                    );
                    dataStorage.reachThreshold = 0;
                }
                cancelEvent(event.getPlayer(), event);
            } else {
                dataStorage.reachThreshold -= dataStorage.reachThreshold > 0 ? 1 : 0;
            }
        }
    }

    @Override
    public void handleOut(Player player, PacketSendEvent event) {
        getDataStorageOf(event.getPlayer()).lastServerKeepAlive = System.currentTimeMillis();
    }

    @Override
    public boolean isTypeToLookFor(PacketReceiveEvent event) {
        String type = event.getType();
        return type.equals(Packet.Client.KEEP_ALIVE) || type.equals(Packet.Client.USE_ENTITY);
    }

    @Override
    public boolean isTypeToLookFor(PacketSendEvent event) {
        return event.getType().equals(Packet.Server.KEEP_ALIVE);
    }
}