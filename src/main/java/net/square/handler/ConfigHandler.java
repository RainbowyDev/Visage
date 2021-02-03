package net.square.handler;

import net.square.Visage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ConfigHandler {

    private final Visage visage;
    private YamlConfiguration yamlConfiguration;

    public ConfigHandler(Visage visage) {
        this.visage = visage;
        loadFile();
    }

    public void loadFile() {
        File file = new File("plugins/" + visage.getDescription().getName() + "/config.yml");

        if (!file.exists())
            visage.saveResource("config.yml", false);

        try {
            this.yamlConfiguration = YamlConfiguration.loadConfiguration(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getPrefix() {
        return this.yamlConfiguration.getString("violation.prefix")
            .replace("&", "§");
    }

    public String getString(String path) {
        return this.yamlConfiguration.getString(path)
            .replace("%prefix%", Visage.prefix)
            .replace("&", "§");
    }

    public boolean isBoolean(String path) {
        return this.yamlConfiguration.getBoolean(path);
    }

    public int getInt(String path) {
        return this.yamlConfiguration.getInt(path);
    }

    public List<String> getStringList(String path) {
        return this.yamlConfiguration.getStringList(path);
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return this.yamlConfiguration.getConfigurationSection(path);
    }
}