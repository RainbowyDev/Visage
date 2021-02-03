package net.square.events;

import com.google.common.collect.Lists;
import net.square.Visage;
import net.square.exceptions.VisageInternalException;
import net.square.handler.ConfigHandler;
import net.square.utilities.connection.Analyzer;
import net.square.utilities.mysql.MySQL;
import net.square.utilities.permission.PermissionHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerJoinHandler implements Listener {

    private final Visage visage;
    private final List<String> whitelisted = Lists.newArrayList();
    private final List<String> blacklisted = Lists.newArrayList();

    public PlayerJoinHandler(Visage visage) {
        this.visage = visage;
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();
        this.visage.getDataStorageList().put(uniqueId, this.visage.getDataStorageFactory().create(player));

        if (visage.isMySQLActive()) {

            visage.getService().submit(() -> {
                MySQL mySQLConnection = visage.getMySQL();

                if (!mySQLConnection.containsData(uniqueId)) {
                    mySQLConnection.insertData(uniqueId, player.getName());
                }

                if (player.hasPermission(PermissionHandler.COMMAND_VERBOSE.getBukkitPermission())) {
                    if (!mySQLConnection.containsVerbose(uniqueId)) {
                        mySQLConnection.insertToVerbose(uniqueId);
                    }
                    if (mySQLConnection.isVerbose(uniqueId)) {
                        visage.getVerboseMode().add(uniqueId);
                        player.sendMessage(Visage.prefix + " §7You §ajoined §7the verbose-mode via auto-verbose.");
                    }
                }
            });
        }
        if (player.hasPermission(PermissionHandler.ADMIN_NOTIFICATION.getBukkitPermission()) && visage.getConfig()
            .getBoolean("logging.alert-updates")
            && !visage.getUpdater().isLatest()) {
            player.sendMessage(
                visage.getConfigHandler().getPrefix() + " §7A new version of Visage has been released! ("
                    + visage.getUpdater()
                    .getLatestVersion() + ")");
        }
    }

    @EventHandler
    public void onConnect(AsyncPlayerPreLoginEvent event) {
        if (!visage.isMySQLActive()) {
            return;
        }

        long banned = visage.getMySQL().isBanned(event.getUniqueId());

        if (banned == -1) {
            return;
        }

        long bannedTime = visage.getConfig().getLong("module.fastrelog.millis");
        if (System.currentTimeMillis() - banned > bannedTime) {
            visage.getMySQL().unbanPlayer(event.getUniqueId());
        } else {
            long time = TimeUnit.MILLISECONDS.toSeconds((bannedTime - (System.currentTimeMillis() - banned)));

            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, visage
                .getConfig()
                .getString("logging.mysql.kick-message")
                .replace("%seconds%", String.valueOf(time)).replace("&", "§").replace("%prefix%", Visage.prefix));
        }
    }

    @EventHandler
    public void handle(AsyncPlayerPreLoginEvent event) throws VisageInternalException {

        String hostAddress = event.getAddress().getHostAddress();

        ConfigHandler configHandler = this.visage.getConfigHandler();
        if (this.visage.getBannedTime().containsKey(hostAddress)) {
            if (System.currentTimeMillis() - this.visage.getBannedTime().get(hostAddress)
                > configHandler.getInt("module.fastrelog.millis")) {
                this.visage.getBannedTime().remove(hostAddress);
            } else {
                event.setKickMessage(
                    configHandler.getString("module.fastrelog.message"));
                event.setLoginResult(Result.KICK_BANNED);
            }
        }

        if (!configHandler.isBoolean("module.analyzer.enabled")) {
            return;
        }

        if (this.blacklisted.contains(hostAddress)) {
            disconnect(event);
            return;
        }

        try {
            if (!this.whitelisted.contains(hostAddress)) {
                Analyzer analyzer = new Analyzer(hostAddress);
                if (analyzer.checkAddress()) {
                    this.blacklisted.add(hostAddress);
                    disconnect(event);
                } else {
                    this.whitelisted.add(hostAddress);
                }
            }
        } catch (Exception e) {
            throw new VisageInternalException(
                String.format("Cant check address of user '%s/%s'", event.getAddress(), event.getName())
            );
        }
    }

    private void disconnect(AsyncPlayerPreLoginEvent event) {
        event.setKickMessage(
            this.visage.getConfigHandler().getString("module.analyzer.settings.kick-message")
                .replace("%ip%", event.getAddress().getHostAddress())
                .replace("%uuid%", event.getUniqueId().toString())
                .replace("%name%", event.getName())
        );
        event.setLoginResult(Result.KICK_BANNED);
    }
}