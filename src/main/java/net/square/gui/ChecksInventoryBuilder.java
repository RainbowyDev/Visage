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

public class ChecksInventoryBuilder {

    private static final ItemStack GRAY_GLASS_PANE = ItemAPI.getItem(Material.STAINED_GLASS_PANE, 7);

    private final Visage visage;

    public ChecksInventoryBuilder(Visage visage) {
        this.visage = visage;
    }

    public Inventory build(Player player) {

        Inventory inventory = Bukkit.createInventory(player, 45, "§9Visage checks");

        for (int i = 0; i < 8; i++) {
            inventory.setItem(i, GRAY_GLASS_PANE);
        }

        inventory.setItem(8, ItemAPI.getItem(Material.ARROW, "§cBack", 1));

        AtomicInteger i = new AtomicInteger(9);

        visage.getModuleManager().modules.forEach((s, visageCheck) -> {
            int andAdd = i.getAndAdd(1);
            visage.getService().submit(() -> setModuleOnPoint(andAdd, inventory, visageCheck.getModuleName()));
        });
        return inventory;
    }

    private void setModuleOnPoint(int point, Inventory inventory, String input) {

        List<String> module = Lists.newArrayList();
        VisageCheck visageCheck = this.visage.getModuleManager().getModule(input);
        int kicks = visage.getModuleManager().getModule(input).getViolationCount();
        module.add("");
        module.add(String.format("§9Total violations §f%d", kicks));
        module.add(String.format("§9Display §f%s", visageCheck.getConfigEntries().get("display")));
        module.add("");
        if (visageCheck.isEnabled()) {
            module.add("§aEnabled");
        } else {
            module.add("§cEnabled");
        }
        if (visageCheck.isSilent()) {
            module.add("§aSilent");
        } else {
            module.add("§cSilent");
        }
        if (visageCheck.isAlertable()) {
            module.add("§aAlertable");
        } else {
            module.add("§cAlertable");
        }
        module.add("");
        module.add("§7Left click toggle status");
        module.add("§7Right click toggle silent-status");
        module.add("§7Middle click toggle alertable-status");
        module.add("");

        inventory.setItem(
            point, ItemAPI.getItem(Material.PAPER, "§f" + input, 1, module));
    }
}
