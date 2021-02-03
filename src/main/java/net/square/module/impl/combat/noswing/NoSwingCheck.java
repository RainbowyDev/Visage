package net.square.module.impl.combat.noswing;

import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.square.Visage;
import net.square.module.VisageCheck;
import net.square.storage.DataStorage;
import org.bukkit.entity.Player;

public class NoSwingCheck extends VisageCheck {

    public NoSwingCheck(Visage visage) {
        super(visage, "NoSwing");
    }

    @Override
    public void handleIn(Player player, PacketReceiveEvent event) {

        if (!this.isEnabled())
            return;

        switch (event.getType()) {
            case Packet.Client.ARM_ANIMATION:

                this.getDataStorageOf(player).swungArm = true;
                this.debug(player, player.getName() + " arm to true");

                break;
            case Packet.Client.FLYING:
                this.getDataStorageOf(player).swungArm = false;

                break;
            case Packet.Client.USE_ENTITY:
                PacketPlayInUseEntity packetPlayInUseEntity = (PacketPlayInUseEntity) event.getPacket();

                DataStorage dataStorage = this.getDataStorageOf(player);

                if (packetPlayInUseEntity.a() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK
                    && !dataStorage.swungArm) {
                    if (dataStorage.swingThreshold++ > (int) getSettings().get("threshold")) {
                        this.markPlayer(player, 1, "NoSwing", "did not swing arm correctly", "N1");
                        this.cancelEvent(player, event);
                    }
                } else {
                    dataStorage.swingThreshold -= dataStorage.swingThreshold > 0 ? 1 : 0;
                }
                this.debug(player, player.getName() + " arm to false");
                dataStorage.swungArm = false;
                break;
        }
    }

    @Override
    public void handleOut(Player player, PacketSendEvent event) {
    }

    @Override
    public boolean isTypeToLookFor(PacketReceiveEvent event) {
        String type = event.getType();
        return type.equals(Packet.Client.ARM_ANIMATION) || type.equals(Packet.Client.FLYING)
            || type.equals(Packet.Client.USE_ENTITY);
    }

    @Override
    public boolean isTypeToLookFor(PacketSendEvent event) {
        return false;
    }
}
