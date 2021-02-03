package net.square.events;

import net.square.Visage;
import net.square.gui.ChecksInventoryBuilder;
import net.square.gui.MainInventoryBuilder;
import net.square.module.VisageCheck;
import net.square.utilities.mysql.MySQL;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryHandler implements Listener {

    private final Visage visage;

    public InventoryHandler(Visage visage) {
        this.visage = visage;
    }

    @EventHandler
    public void handle(InventoryClickEvent event) {

        String inventoryName = event.getInventory().getName();
        if (inventoryName == null) {
            event.setCancelled(false);
            return;
        }

        String prefix = visage.getConfigHandler().getPrefix();
        if (inventoryName.equalsIgnoreCase("§9Visage menu")) {

            if (!(event.getWhoClicked() instanceof Player))
                return;
            if (event.getCurrentItem() == null)
                return;

            Material type = event.getCurrentItem().getType();
            if (type == null)
                return;

            Player player = (Player) event.getWhoClicked();

            if(event.getCurrentItem() == null) return;
            if(event.getCurrentItem().getItemMeta() == null) return;
            if(event.getCurrentItem().getItemMeta().getDisplayName() == null) return;

            String itemName = event.getCurrentItem().getItemMeta().getDisplayName();

            if (type.equals(Material.REDSTONE) && itemName.equalsIgnoreCase("§9Restart visage")) {

                player.sendMessage(String.format("%s §7Try to reload configuration...", prefix));
                visage.getConfigHandler().loadFile();

            } else if (type.equals(Material.PAPER) && itemName.equalsIgnoreCase("§9Visage information")) {

                if (visage.isMySQLActive()) {
                    MySQL mySQL = visage.getMySQL();
                    mySQL.bans = mySQL.getBanCount();
                    mySQL.logs = mySQL.getLogsCount();
                }

                player.sendMessage(String.format("%s §7Updating values...", prefix));
                player.openInventory(new MainInventoryBuilder(visage).build(player));

            } else if (type.equals(Material.BOOK) && itemName.equalsIgnoreCase("§9Checks")) {
                player.openInventory(new ChecksInventoryBuilder(visage).build(player));
            }

            event.setCancelled(true);

        } else if (inventoryName.equalsIgnoreCase("§9Visage checks")) {

            if (!(event.getWhoClicked() instanceof Player))
                return;
            if (event.getCurrentItem() == null) event.setCancelled(true);

            Material type = event.getCurrentItem().getType();
            if (type == null) event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();

            if(event.getCurrentItem() == null) event.setCancelled(true);
            if(event.getCurrentItem().getItemMeta() == null) event.setCancelled(true);
            if(event.getCurrentItem().getItemMeta().getDisplayName() == null) event.setCancelled(true);

            String itemName = event.getCurrentItem().getItemMeta().getDisplayName();

            assert type != null;
            if (type.equals(Material.ARROW) && itemName.equalsIgnoreCase("§cBack")) {

                player.openInventory(new MainInventoryBuilder(visage).build(player));

            } else if (type.equals(Material.PAPER)) {

                String name = itemName.replace("§f", "");
                VisageCheck check = visage.getModuleManager().getModule(name);

                if (event.getClick() == ClickType.LEFT) {
                    check.setEnabled(!check.isEnabled());

                } else if (event.getClick() == ClickType.RIGHT) {
                    check.setSilent(!check.isSilent());

                } else if (event.getClick() == ClickType.MIDDLE) {
                    check.setAlertable(!check.isAlertable());
                }
                player.openInventory(new ChecksInventoryBuilder(visage).build(player));

            }
            event.setCancelled(true);
        }
    }
}