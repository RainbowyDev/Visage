package net.square.commands;

import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.square.Visage;
import net.square.module.VisageCheck;
import net.square.utilities.hastebin.Hastebin;
import net.square.utilities.mysql.MySQL;
import net.square.utilities.permission.PermissionHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class VisageCommand implements CommandExecutor {
    private final Visage visage;

    public VisageCommand(Visage visage) {
        this.visage = visage;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if (!commandSender.isOp()) {
            commandSender.sendMessage(
                String.format(
                    "%s §7Running Visage anticheat by SquareCode (%s)", Visage.prefix,
                    visage.getDescription().getVersion()
                ));
            commandSender.sendMessage(Visage.prefix + " §7This is an open source solution against cheating");
            commandSender.sendMessage(Visage.prefix + " §7Find out more: https://git.squarecode.de/SquareCode/Visage");
            return true;
        }

        if (args.length == 0) {
            sendHelpOverview(commandSender);
            return true;
        }
        String argument = args[0];
        if (argument.equalsIgnoreCase("tps")) {

            if(!hasPermission(commandSender, PermissionHandler.COMMAND_TPS)) return true;

            double[] recentTps = MinecraftServer.getServer().recentTps;
            for (double tps : recentTps) {
                commandSender.sendMessage(Visage.prefix + " §7Ticks: §f" + tps);
            }
        } else {
            boolean mySQLActive = visage.isMySQLActive();
            MySQL mySQL = visage.getMySQL();
            if (argument.equalsIgnoreCase("kicks")) {

                if(!hasPermission(commandSender, PermissionHandler.COMMAND_KICKS)) return true;

                if (args.length < 2) {
                    return true;
                }

                if (!mySQLActive) {
                    commandSender.sendMessage(Visage.prefix + " §cNo mysql connection");
                    return true;
                }

                int var = mySQL.getPlayerKicks(args[1]);

                if (var == -1) {
                    commandSender.sendMessage(Visage.prefix + " §cEmpty result");
                    return true;
                }

                commandSender.sendMessage(String.format(
                    "%s §7The player §f%s §7was kicked from the server §9%d §7times in total",
                    Visage.prefix, args[1], var
                ));

            } else if (argument.equalsIgnoreCase("logs")) {

                if(!hasPermission(commandSender, PermissionHandler.COMMAND_LOGS)) return true;

                if (args.length < 3) {
                    commandSender.sendMessage(Visage.prefix + " §cMissing arguments");
                    return true;
                }

                if (!mySQLActive) {
                    commandSender.sendMessage(Visage.prefix + " §cNo mysql connection");
                    return true;
                }

                int i1 = Integer.parseInt(args[2]);

                if (i1 < 0) {
                    commandSender.sendMessage(Visage.prefix + " §cThe number could not be processed.");
                    commandSender.sendMessage(Visage.prefix + " §cUse used number must be greater than 0");
                    return true;
                }

                List<String> informationOfPlayer = mySQL.getInformationOfPlayer(
                    args[1], Integer.parseInt(args[2]));

                if (informationOfPlayer.isEmpty()) {
                    commandSender.sendMessage(Visage.prefix + " §cEmpty result");
                    return true;
                }

                StringBuilder builder = new StringBuilder();

                informationOfPlayer.forEach(s1 -> builder.append(s1).append("\n"));

                commandSender.sendMessage(Visage.prefix + " §7Try to upload the document...");

                try {
                    commandSender.sendMessage(String.format("%s §9Result §f%s", Visage.prefix,
                                                            Hastebin.post(builder.toString())
                    ));
                } catch (IOException e) {
                    commandSender.sendMessage(
                        String.format("%s Error while uploading (%s)", Visage.prefix, e.getMessage()));
                }

            } else if (argument.equalsIgnoreCase("enabled")) {

                if(!hasPermission(commandSender, PermissionHandler.COMMAND_ENABLED)) return true;

                commandSender.sendMessage(Visage.prefix + " §7All enabled modules:");
                this.visage.getModuleManager().modules.values()
                    .stream()
                    .filter(VisageCheck::isEnabled)
                    .map(VisageCheck::getModuleName)
                    .map(moduleName -> String.format(" §8- §7%s", moduleName))
                    .forEach(commandSender::sendMessage);

            } else if (argument.equalsIgnoreCase("check") ||
                argument.equalsIgnoreCase("analyze")) {

                if(!hasPermission(commandSender, PermissionHandler.COMMAND_CHECK)) return true;

                if (args.length < 2) {
                    commandSender.sendMessage(String.format("%s §cUnknown syntax", Visage.prefix));
                    return true;
                }

                String name = args[1];
                commandSender.sendMessage(String.format("%s §7Information about §f%s", Visage.prefix, name));
                UUID uniqueId = Bukkit.getPlayer(name).getUniqueId();

                if (uniqueId == null) {
                    commandSender.sendMessage(String.format("%s §cPlayer is null", Visage.prefix));
                    return true;
                }

                Map<String, Integer> violationPoints = this.visage.getModuleManager().modules
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().getViolations().containsKey(uniqueId))
                    .collect(
                        Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getViolations().get(uniqueId)));

                if (violationPoints.isEmpty()) {
                    commandSender.sendMessage(Visage.prefix + " §cNo violation points found!");
                    return true;
                }
                violationPoints.entrySet()
                    .stream()
                    .map(entry -> String.format(
                        Visage.prefix + " §8- §7Module §f%s §7Violations §f%d",
                        entry.getKey(), entry.getValue()
                    ))
                    .forEach(commandSender::sendMessage);
            } else if (argument.equalsIgnoreCase("gui")) {

                if(!hasPermission(commandSender, PermissionHandler.COMMAND_GUI)) return true;

                if (!(commandSender instanceof Player)) {
                    commandSender.sendMessage("You cannot use this command as console!");
                    return true;
                }
                Player player = (Player) commandSender;
                player.openInventory(this.visage.getMainInventoryBuilder().build(player));
            } else if (argument.equalsIgnoreCase("debug")) {

                if(!hasPermission(commandSender, PermissionHandler.COMMAND_DEBUG)) return true;

                if (args.length < 2) {
                    commandSender.sendMessage(String.format("%s §cUnknown syntax", Visage.prefix));
                    return true;
                }

                if (!(commandSender instanceof Player))
                    return true;
                Player player = (Player) commandSender;
                String moduleName = args[1];

                Map<String, VisageCheck> modules = this.visage.getModuleManager().modules;
                if (!modules.containsKey(moduleName)) {
                    player.sendMessage(Visage.prefix + " §cUnknown module");
                    return true;
                }
                List<Player> debugList = modules.get(moduleName).getDebugList();
                if (debugList.contains(player)) {
                    debugList.remove(player);
                    player.sendMessage(String.format(
                        Visage.prefix + " §7You §cleft §7debug for module §f%s", moduleName
                    ));
                } else {
                    debugList.add(player);
                    player.sendMessage(String.format(
                        Visage.prefix + " §7You §ajoined §7debug for module §f%s", moduleName
                    ));
                }
            } else if (argument.equalsIgnoreCase("verbose") ||
                argument.equalsIgnoreCase("alerts")) {

                if(!hasPermission(commandSender, PermissionHandler.COMMAND_VERBOSE)) return true;

                if (!(commandSender instanceof Player))
                    return true;
                UUID uniqueId = ((Player) commandSender).getUniqueId();
                List<UUID> verboseMode = this.visage.getVerboseMode();
                if (verboseMode.contains(uniqueId)) {
                    verboseMode.remove(uniqueId);
                    if (mySQLActive) {
                        mySQL.updateVerbose(((Player) commandSender).getUniqueId(), false);
                    }
                    commandSender.sendMessage(Visage.prefix + " §7You §cleft §7the verbose-mode.");

                } else {
                    verboseMode.add(uniqueId);
                    if (mySQLActive) {
                        mySQL.updateVerbose(((Player) commandSender).getUniqueId(), true);
                    }
                    commandSender.sendMessage(Visage.prefix + " §7You §ajoined §7the verbose-mode.");
                }
            } else if (argument.equalsIgnoreCase("notify")) {

                if(!hasPermission(commandSender, PermissionHandler.COMMAND_NOTIFY)) return true;

                if (args.length < 2) {
                    commandSender.sendMessage(String.format("%s §cUnknown syntax", Visage.prefix));
                    return true;
                }

                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 1; i < args.length; ++i) {
                    stringBuilder.append(args[i]).append(" ");
                }
                String message = this.visage
                    .getConfigHandler()
                    .getString("violation.info")
                    .replace("%text%", stringBuilder.toString());
                Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(player -> player.hasPermission(PermissionHandler.ADMIN_NOTIFICATION.getBukkitPermission()))
                    .forEach(player -> player.sendMessage(message));

            } else if (argument.equalsIgnoreCase("broadcast")) {

                if(!hasPermission(commandSender, PermissionHandler.COMMAND_BROADCAST)) return true;

                Bukkit.broadcastMessage(
                    this.visage.getConfigHandler().getString("violation.kick").replace("%name%", args[1])
                );
            } else sendHelpOverview(commandSender);
        }
        return true;
    }

    private boolean hasPermission(CommandSender sender, PermissionHandler handler) {
        if (sender.hasPermission(handler.getBukkitPermission())) {
            return true;
        } else {
            sender.sendMessage(
                String.format("%s §cYou have no permissions to execute this command. (%s)", Visage.prefix, handler.getBukkitPermission()));
            return false;
        }
    }

    private void sendFormattedMessage(CommandSender sender, PermissionHandler handler, String message) {
        if (sender.hasPermission(handler.getBukkitPermission())) {
            sender.sendMessage(message.replace("»", "§8»§7"));
        } else {
            sender.sendMessage(message.replace("»", "§8»§7§m"));
        }
    }

    private void sendHelpOverview(CommandSender sender) {
        sender.sendMessage(String.format("%s §7Command help overview for §9Visage", Visage.prefix));
        sendFormattedMessage(sender, PermissionHandler.COMMAND_GUI, "» /visage <gui>");
        sendFormattedMessage(sender, PermissionHandler.COMMAND_TPS, "» /visage <tps>");
        sendFormattedMessage(sender, PermissionHandler.COMMAND_ENABLED, "» /visage <enabled>");
        sendFormattedMessage(sender, PermissionHandler.COMMAND_VERBOSE, "» /visage <verbose>");
        sendFormattedMessage(sender, PermissionHandler.COMMAND_NOTIFY, "» /visage <notify> <message>");
        sendFormattedMessage(sender, PermissionHandler.COMMAND_DEBUG, "» /visage <debug> <moduleName>");
        sendFormattedMessage(sender, PermissionHandler.COMMAND_KICKS, "» /visage <kicks> <playerName>");
        sendFormattedMessage(sender, PermissionHandler.COMMAND_CHECK, "» /visage <check> <playerName>");
        sendFormattedMessage(sender, PermissionHandler.COMMAND_BROADCAST, "» /visage <broadcast> <message>");
        sendFormattedMessage(sender, PermissionHandler.COMMAND_LOGS, "» /visage <logs> <playerName> <length>");
    }
}