package net.square.module.impl.combat.combatanalytics;

import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.square.Visage;
import net.square.module.VisageCheck;
import net.square.storage.DataStorage;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CombatAnalyticsCheck extends VisageCheck {

    public CombatAnalyticsCheck(Visage visage) {
        super(visage, "CombatAnalytics");
    }

    @Override
    public void handleIn(Player player, PacketReceiveEvent event) {
        if (!this.isEnabled())
            return;

        PacketPlayInUseEntity packetPlayInUseEntity = (PacketPlayInUseEntity) event.getPacket();
        if (packetPlayInUseEntity.a() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK) {

            final Entity entity = packetPlayInUseEntity.a(((CraftWorld) player.getWorld()).getHandle()).getBukkitEntity();

            if (!(entity instanceof LivingEntity)) {
                return;
            }
            Vector vec = entity.getLocation().clone().toVector().setY(0.0)
                .subtract(player.getEyeLocation().clone().toVector().setY(0.0));
            float angle = player.getEyeLocation().getDirection().angle(vec);
            DataStorage dataStorage = getDataStorageOf(player);

            this.debug(player, String.format("angle: %s", angle));

            if (angle > (double) getSettings().get("angle")) {
                if (dataStorage.combatAnalyticsThreshold++ > (int) getSettings().get("threshold")) {
                    markPlayer(player, 1, "CombatAnalytics",
                               "hits a player without looking at him", "C1"
                    );
                }
            } else
                dataStorage.combatAnalyticsThreshold -= dataStorage.combatAnalyticsThreshold > 0 ? 1 : 0;
        }
    }

    @Override
    public void handleOut(Player player, PacketSendEvent event) {
    }

    @Override
    public boolean isTypeToLookFor(PacketReceiveEvent event) {
        return event.getType().equals(Packet.Client.USE_ENTITY);
    }

    @Override
    public boolean isTypeToLookFor(PacketSendEvent event) {
        return false;
    }
}