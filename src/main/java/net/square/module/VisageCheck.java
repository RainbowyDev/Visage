package net.square.module;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.square.Visage;
import net.square.exceptions.InvalidModuleException;
import net.square.handler.ConfigHandler;
import net.square.storage.DataStorage;
import net.square.utilities.discord.DiscordHook;
import net.square.utilities.mysql.MySQL;
import net.square.utilities.permission.PermissionHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class VisageCheck implements AtlasListener, Listener {

    @Getter
    private final Visage visage;
    @Getter
    private final Map<Integer, List<String>> thresholds;
    @Getter
    private final Map<String, Object> configEntries;
    @Getter
    private final Map<String, Object> settings;
    @Getter
    private final HashMap<UUID, Integer> violations = Maps.newHashMap();
    @Getter
    private final List<Player> debugList = Lists.newArrayList();
    @Getter
    private final String moduleName;
    @Getter
    private final String moduleDisplay;
    @Getter
    private int violationCount = 0;
    @Getter @Setter
    private boolean enabled;
    @Getter @Setter
    private boolean silent;
    @Getter @Setter
    private boolean alertable;

    @SneakyThrows
    public VisageCheck(Visage visage, String moduleName) {
        this.visage = visage;
        try {
            this.thresholds = visage.getModuleManager().getThresholdFor(moduleName);
            this.configEntries = visage.getModuleManager().getConfigEntriesFor(moduleName);
            this.settings = visage.getModuleManager().getConfigSettingsFor(moduleName);
        } catch (NullPointerException exception) {
            throw new InvalidModuleException("Some values are NULL (thresholds, configEntries or settings section)");
        }

        this.moduleName = moduleName;
        this.moduleDisplay = String.valueOf(configEntries.get("display"));

        setEnabled((boolean) configEntries.get("enabled"));
        setSilent((boolean) configEntries.get("silent"));
        setAlertable((boolean) configEntries.get("alertable"));

        Atlas.getInstance().getEventManager().registerListeners(this, visage);
        visage.getServer().getPluginManager().registerEvents(this, visage);
    }

    public void update() {
        violationCount = visage.getMySQL().getModuleKicks(moduleName);
    }

    public abstract void handleIn(Player player, PacketReceiveEvent event);

    public abstract void handleOut(Player player, PacketSendEvent event);

    public abstract boolean isTypeToLookFor(PacketReceiveEvent event);

    public abstract boolean isTypeToLookFor(PacketSendEvent event);

    public synchronized void markPlayer(Player player, int violationPoints, String moduleName, String comment,
                                        String type) {

        if(player.hasPermission(PermissionHandler.ADMIN_BYPASS.getBukkitPermission())) return;

        UUID uniqueId = player.getUniqueId();

        this.violations.compute(uniqueId, (uuid, integer) -> (integer == null ? 0 : integer) + violationPoints);

        Integer violation = this.violations.get(uniqueId);
        EntityPlayer handle = ((CraftPlayer) player).getHandle();

        ConfigHandler configHandler = this.visage.getConfigHandler();
        String message = configHandler.getString("violation.verbose")
            .replace("%playerName%", player.getName())
            .replace("%playerWorld%", player.getWorld().getName())
            .replace("%comment%", comment)
            .replace("%display%", moduleDisplay)
            .replace("%type%", type)
            .replace("%moduleName%", moduleName)
            .replace("%ping%", String.valueOf(handle.ping))
            .replace("%tps%", String.format("%.3f", MinecraftServer.getServer().recentTps[1]))
            .replace("%vladded%", String.valueOf(violationPoints))
            .replace("%vl%", String.valueOf(violation));

        TextComponent component = new TextComponent(message);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/minecraft:tp " + player.getName()));
        component.setHoverEvent(
            new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                TextComponent.fromLegacyText(
                    "§8* §9Verbose report for §f" + player.getName() + " \n" +
                        " §8» §9Comment §f" + comment + "\n" +
                        " §8» §9Module §f" + moduleName + " (Type " + type + ")\n" +
                        " §8» §9Ping §f" + handle.ping + "ms\n" +
                        " §8» §9TPS §f" + String.format("%.2f", MinecraftServer.getServer().recentTps[1])
                        + "\n" +
                        " §8» §9VL §f" + violationPoints + " -> " + violation + "\n"
                        + "\n"
                        + "§9§oClick to teleport to §f§o" + player.getName()
                )
            ));
        for (UUID uuid : this.visage.getVerboseMode()) {
            Player p = Bukkit.getPlayer(uuid);
            if (visage.getConfig().getBoolean("violation.hover-message")) {
                p.spigot().sendMessage(component);
            } else {
                p.sendMessage(message);
            }
        }

        if (configHandler.isBoolean("module.fastrelog.enabled") &&
            !configHandler.getStringList("module.fastrelog.ignore").contains(moduleName) &&
            isAlertable()) {
            this.visage.getBannedTime().put(player.getAddress().getHostString(), System.currentTimeMillis());
        }

        MySQL mySQL = visage.getMySQL();
        if (visage.isMySQLActive()) {
            visage.getService().submit(() -> mySQL.createLog(
                player.getUniqueId(), player.getName(), moduleName, violation, ((CraftPlayer) player).getHandle().ping,
                comment
            ));
            visage.getService().submit(() -> mySQL.updateModuleData(moduleName));
        }

        if (isAlertable()) {
            Bukkit.getScheduler().runTask(this.visage, () -> {
                for (Map.Entry<Integer, List<String>> entry : this.thresholds.entrySet()) {
                    if (entry.getKey().equals(violation)) {
                        List<String> strings = entry.getValue();
                        for (String s : strings) {
                            String command = replaceMarkingPlaceholders(player, s);
                            if (command.equals("mysql-ban")) {
                                if (visage.isMySQLActive()) {
                                    visage.getService()
                                        .submit(() -> mySQL
                                            .banPlayer(player.getUniqueId(), player.getName(), moduleName));
                                }
                            } else if (command.equalsIgnoreCase("discord-hook")) {
                                if (!visage.getConfig().getBoolean("logging.discord.enabled")) {
                                    continue;
                                }
                                DiscordHook.sendMessageToDiscord(
                                    visage, player, moduleName, comment, String.valueOf(violation));
                            } else if (command.startsWith("kick") || command.startsWith("ban")) {
                                if (visage.getConfig().getBoolean("logging.mysql.use-database-support")
                                    && visage.isMySQLActive()) {
                                    visage.getService().submit(() -> mySQL.updateData(player.getUniqueId()));
                                }
                            }
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                            this.visage.getVisageLogger().fileLog("command", command);
                        }
                    }
                }
            });
        }

        if (!this.configEntries.containsKey("log_vl") || violation == null)
            return;
        int log_vl = (int) this.configEntries.get("log_vl");
        if (log_vl > violation)
            return;
        this.visage.getVisageLogger().fileLog(
            "verbose", String.format(
                "%s in %s %s and failed %s (ping: %d tps: %.3f) | VL: +%d -> %d",
                player.getName(), player.getWorld().getName(), comment, moduleName,
                ((CraftPlayer) player).getHandle().ping,
                MinecraftServer.getServer().recentTps[1],
                violationPoints, violation
            )
        );
    }

    private String replaceMarkingPlaceholders(Player player, String s) {
        return s.replace("&", "§")
            .replace("%player%", player.getName())
            .replace("%prefix%", Visage.prefix)
            .replace("%ping%", String.valueOf(((CraftPlayer) player).getHandle().ping))
            .replace("%tps%", String.format("%.3f", MinecraftServer.getServer().recentTps[1]))
            .replace("%world%", player.getWorld().getName());
    }

    public void debug(Player player, String message) {
        if (this.debugList.contains(player)) {
            player.sendMessage(String.format("%s §8[%s] §7%s", Visage.prefix, this.moduleName.toLowerCase(), message));
        }
    }

    public boolean shouldCancel(Player player) {
        return player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
    }

    public DataStorage getDataStorageOf(Player player) {
        return this.visage.getDataStorageList().get(player.getUniqueId());
    }

    @SuppressWarnings("unused")
    public long now() {
        return System.currentTimeMillis();
    }

    public void cancelEvent(Player player, PacketReceiveEvent event) {
        if (player == null) return;

        if (!isAlertable()) return;

        if(player.hasPermission(PermissionHandler.ADMIN_BYPASS.getBukkitPermission())) return;

        Integer integer = this.violations.get(player.getUniqueId());

        if (integer == null) return;

        if (!isSilent() && integer >= (int) this.configEntries.get("cancel_vl")) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    public boolean isRoughlyEqual(double var, double var2, double sens) {
        return Math.abs(var - var2) < sens;
    }
}