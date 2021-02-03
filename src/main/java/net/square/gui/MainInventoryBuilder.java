package net.square.gui;

import com.google.common.collect.Lists;
import net.square.Visage;
import net.square.module.VisageCheck;
import net.square.utilities.items.ItemAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainInventoryBuilder {
    private static final ItemStack BLACK_GLASS_PANE = ItemAPI.getItem(Material.STAINED_GLASS_PANE, 15);
    private static final ItemStack GRAY_GLASS_PANE = ItemAPI.getItem(Material.STAINED_GLASS_PANE, 7);

    private final Visage visage;

    public MainInventoryBuilder(Visage visage) {
        this.visage = visage;
    }

    public Inventory build(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 9 * 3, "§9Visage menu");

        //  0  1  2  3  4  5  6  7  8
        //  9 10 11 12 13 14 15 16 17
        // 18 19 20 21 22 23 24 25 26

        visage.getService()
            .submit(() -> visage.getModuleManager().modules.forEach((s, visageCheck) -> visageCheck.update()));

        inventory.setItem(0, BLACK_GLASS_PANE);
        inventory.setItem(1, BLACK_GLASS_PANE);
        inventory.setItem(9, BLACK_GLASS_PANE);
        inventory.setItem(18, BLACK_GLASS_PANE);
        inventory.setItem(19, BLACK_GLASS_PANE);

        inventory.setItem(7, BLACK_GLASS_PANE);
        inventory.setItem(8, BLACK_GLASS_PANE);
        inventory.setItem(17, BLACK_GLASS_PANE);
        inventory.setItem(26, BLACK_GLASS_PANE);
        inventory.setItem(25, BLACK_GLASS_PANE);

        inventory.setItem(20, GRAY_GLASS_PANE);
        inventory.setItem(21, GRAY_GLASS_PANE);
        inventory.setItem(22, GRAY_GLASS_PANE);
        inventory.setItem(23, GRAY_GLASS_PANE);
        inventory.setItem(24, GRAY_GLASS_PANE);

        inventory.setItem(2, GRAY_GLASS_PANE);
        inventory.setItem(3, GRAY_GLASS_PANE);
        inventory.setItem(4, GRAY_GLASS_PANE);
        inventory.setItem(5, GRAY_GLASS_PANE);
        inventory.setItem(6, GRAY_GLASS_PANE);

        List<String> checks = Lists.newArrayList();
        checks.add("");
        checks.add(String.format("§9Total checks §f%d", visage.getModuleManager().modules.size()));
        checks.add("");

        AtomicInteger i = new AtomicInteger();
        visage.getModuleManager().modules.forEach((s, visageCheck) -> {
            checks.add(String.format("§f%s §7(ID: %s)", visageCheck.getModuleName(), i.get()));
            i.getAndIncrement();
        });
        i.set(0);

        checks.add("");
        checks.add("§7Click to open Checks menu");
        inventory.setItem(11, ItemAPI.getItem(Material.BOOK, "§9Checks", 1, checks));

        List<String> information = Lists.newArrayList();
        information.add("");
        information.add("§9Type §fDefault");
        information.add("§9Build §f" + visage.getDescription().getVersion());
        information.add("§9Implementation §f" + visage.getServer().getVersion());
        if (visage.isMySQLActive()) {
            information.add("§9Storage §fMYSQL");
            information.add("");
            information.add(String.format("§9Total Bans §f%d", visage.getMySQL().bans));
            information.add(String.format("§9Total Logs §f%d", visage.getMySQL().logs));
            information.add("");
            information.add("§7Click to Refresh");
        } else {
            information.add("§9Storage §fLOCAL");
            information.add("");
            information.add("§9Total Bans §fNULL");
            information.add("§9Total Logs §fNULL");
            information.add("");
            information.add("§7Click to Refresh");
        }
        inventory.setItem(13, ItemAPI.getItem(Material.PAPER, "§9Visage Information", 1, information));
        inventory.setItem(15, ItemAPI.getItem(Material.REDSTONE, "§9Restart visage", 1));

        return inventory;
    }
}