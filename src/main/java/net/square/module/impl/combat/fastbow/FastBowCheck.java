package net.square.module.impl.combat.fastbow;

import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import net.square.Visage;
import net.square.module.VisageCheck;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class FastBowCheck extends VisageCheck {

    public FastBowCheck(Visage visage) {
        super(visage, "FastBow");
    }

    @EventHandler
    public void handle(EntityShootBowEvent event) {

        if (!isEnabled())
            return;

        if (!(event.getEntity() instanceof Player))
            return;

        final Player player = (Player) event.getEntity();

        double force = event.getForce();
        long lastBowPul = System.currentTimeMillis() - getDataStorageOf(player).lastBowPull;
        double pullBackSpeed = force / (double) lastBowPul;

        debug(player, String.format("pullBackSpeed: %s", pullBackSpeed));

        if ((pullBackSpeed >= 0.01D || pullBackSpeed == Double.POSITIVE_INFINITY)) {
            markPlayer(player, 1, "FastBow",
                       "pulled bow back too fast (" + force + "/" + (int) (lastBowPul / 50L) + ")", "F1"
            );
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handle(PlayerInteractEvent event) {
        if (!isEnabled())
            return;
        Player player = event.getPlayer();
        Action action = event.getAction();
        if ((action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR)) &&
            player.getItemInHand() != null && player.getItemInHand().getType().equals(Material.BOW)) {
            getDataStorageOf(player).lastBowPull = System.currentTimeMillis();
        }
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
