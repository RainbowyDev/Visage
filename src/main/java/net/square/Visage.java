package net.square;

import cc.funkemunky.api.Atlas;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import net.square.commands.VisageCommand;
import net.square.events.InventoryHandler;
import net.square.events.PlayerJoinHandler;
import net.square.events.PlayerQuitHandler;
import net.square.gui.MainInventoryBuilder;
import net.square.handler.ConfigHandler;
import net.square.module.ModuleManager;
import net.square.processor.PacketProcessor;
import net.square.storage.DataStorage;
import net.square.storage.DataStorageFactory;
import net.square.utilities.logger.VisageLogger;
import net.square.utilities.metrics.Metrics;
import net.square.utilities.mysql.MySQL;
import net.square.utilities.updater.Updater;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Visage extends JavaPlugin {

    public static String prefix;

    @Getter
    private ConfigHandler configHandler;
    @Getter
    private ModuleManager moduleManager;
    @Getter
    private VisageLogger visageLogger;
    @Getter
    private PacketProcessor processor;
    @Getter
    private DataStorageFactory dataStorageFactory;
    @Getter
    private MySQL mySQL;
    @Getter
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    @Getter
    private final MainInventoryBuilder mainInventoryBuilder = new MainInventoryBuilder(this);

    @Getter
    private final Map<UUID, DataStorage> dataStorageList = Maps.newHashMap();
    @Getter
    private final List<UUID> verboseMode = Lists.newArrayList();
    @Getter
    private final Map<String, Long> bannedTime = Maps.newHashMap();
    @Getter
    private Updater updater;

    @Override
    public void onEnable() {
        long now = System.currentTimeMillis();
        this.visageLogger = new VisageLogger(this);
        this.visageLogger.consoleLog("Trying to start visage...");

        if (getConfig().getBoolean("logging.mysql.use-database-support")) {
            mySQL = new MySQL(
                getConfig().getString("logging.mysql.address"),
                getConfig().getString("logging.mysql.database"),
                getConfig().getString("logging.mysql.password"),
                getConfig().getString("logging.mysql.username"),
                getConfig().getInt("logging.mysql.port")
            );

            if (mySQL.isConnected()) {
                this.visageLogger.consoleLog("MySQL successfully connected");
            } else {
                this.visageLogger.consoleLog("Failed to establish a connection to the MySQL database");
            }
        }
        if (getConfig().getBoolean("logging.discord.enabled")) {
            this.visageLogger.consoleLog("Enabled Discord webhook");
        }

        this.moduleManager = new ModuleManager(this);
        this.processor = new PacketProcessor(this.moduleManager);
        Atlas.getInstance().getEventManager().registerListeners(this.processor, this);
        this.dataStorageFactory = new DataStorageFactory();
        this.dataStorageFactory.startStorageCycle(this);

        this.visageLogger.consoleLog("Setup configuration file");

        prefix = this.configHandler.getPrefix();

        for (Player player : this.getServer().getOnlinePlayers()) {
            this.dataStorageList.put(player.getUniqueId(), this.dataStorageFactory.create(player));
        }

        this.moduleManager.loadModules();

        new Metrics(this, 9705);
        updater = new Updater("86757", this);

        String latestFormatted = updater.isLatest() ? "(LATEST VERSION)" : "(OLD VERSION)";

        //do bukkit specific stuff in the end
        this.getServer().getPluginManager().registerEvents(new PlayerJoinHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerQuitHandler(this), this);
        this.getServer().getPluginManager().registerEvents(new InventoryHandler(this), this);
        this.getCommand("visage").setExecutor(new VisageCommand(this));

        this.visageLogger.consoleLog(
            String.format("Started visage successfully after %dms! %s", System.currentTimeMillis() - now, latestFormatted));
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        this.processor.shutdown();
        this.dataStorageFactory.shutdown();
        if (this.isMySQLActive()) {
            this.mySQL.close();
        }
    }

    @Override
    public void onLoad() {
        this.configHandler = new ConfigHandler(this);
    }

    public boolean isMySQLActive() {
        return this.mySQL != null && this.mySQL.isConnected();
    }
}