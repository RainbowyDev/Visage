package net.square.utilities.updater;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class Updater {

    private final String id;
    private final JavaPlugin plugin;

    @Getter
    private boolean latest;
    @Getter
    private String latestVersion;

    public Updater(String id, JavaPlugin plugin) {
        this.id = id;
        this.plugin = plugin;

        checkLatest();
    }

    public void checkLatest() {
        try {
            String URL = "https://api.spigotmc.org/legacy/update.php?resource=";

            String anObject = new BufferedReader(new InputStreamReader(new URL(URL + getId()).openStream())).readLine();

            latest = getPlugin().getDescription().getVersion().equals(anObject);
            latestVersion = anObject;

        } catch (IOException ignored) { }
    }

    public String getId() {
        return id;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }
}
