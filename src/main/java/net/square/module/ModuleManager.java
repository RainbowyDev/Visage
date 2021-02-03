package net.square.module;

import net.square.Visage;
import net.square.exceptions.InvalidModuleException;
import net.square.module.impl.combat.assist.AssistCheck;
import net.square.module.impl.combat.attackraytrace.AttackRaytraceCheck;
import net.square.module.impl.combat.combatanalytics.CombatAnalyticsCheck;
import net.square.module.impl.combat.fastbow.FastBowCheck;
import net.square.module.impl.combat.heuristics.HeuristicsCheck;
import net.square.module.impl.combat.killaura.KillAuraCheck;
import net.square.module.impl.combat.noswing.NoSwingCheck;
import net.square.module.impl.combat.reach.ReachCheck;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.fusesource.jansi.Ansi;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ModuleManager {

    private final Visage visage;

    public final Map<String, VisageCheck> modules = new HashMap<>();

    public ModuleManager(Visage visage) {
        this.visage = visage;
    }

    public void loadModules() {
        try {
            // Combat
            this.addCheck(new ReachCheck(this.visage));
            this.addCheck(new HeuristicsCheck(this.visage));
            this.addCheck(new KillAuraCheck(this.visage));
            this.addCheck(new AssistCheck(this.visage));
            this.addCheck(new CombatAnalyticsCheck(this.visage));
            this.addCheck(new AttackRaytraceCheck(this.visage));
            this.addCheck(new FastBowCheck(this.visage));
            this.addCheck(new NoSwingCheck(this.visage));

        } catch (InvalidModuleException exception) {
            Bukkit.getLogger().log(Level.SEVERE, "Cant load modules: " + exception.getMessage());
            exception.printStackTrace();
        }
        this.visage.getVisageLogger().consoleLog(String.format("Successfully loaded %d modules!", this.modules.size()));
    }

    public void addCheck(VisageCheck check) throws InvalidModuleException {
        String moduleName = check.getModuleName();
        this.modules.put(moduleName, check);
        if (visage.isMySQLActive()) {
            visage.getService().submit(() -> {
                if (!visage.getMySQL().containsModuleData(moduleName)) {
                    visage.getMySQL().insertModuleData(moduleName);
                }
            });
        }
    }

    public VisageCheck getModule(String moduleName) {
        return this.modules.get(moduleName);
    }

    public Map<String, Object> getConfigEntriesFor(String moduleName) {
        moduleName = moduleName.toLowerCase();
        String path = "module." + moduleName;
        ConfigurationSection moduleSection =
            this.visage.getConfigHandler().getConfigurationSection(path);
        FileConfiguration config = this.visage.getConfig();
        Map<String, Object> map = new HashMap<>();
        for (String s : moduleSection.getKeys(false)) {
            if (!s.equals("thresholds")) {
                if (map.put(s, config.get(path + "." + s)) != null) {
                    throw new IllegalStateException("Duplicate key");
                }
            }
        }
        return map;
    }

    public Map<String, Object> getConfigSettingsFor(String moduleName) {
        moduleName = moduleName.toLowerCase();
        String path = "module." + moduleName + ".settings";
        ConfigurationSection moduleSection =
            this.visage.getConfigHandler().getConfigurationSection(path);
        FileConfiguration config = this.visage.getConfig();
        Map<String, Object> map = new HashMap<>();
        for (String s : moduleSection.getKeys(false)) {
            if (map.put(s, config.get(path + "." + s)) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }
        return map;
    }

    public Map<Integer, List<String>> getThresholdFor(String moduleName) {
        moduleName = moduleName.toLowerCase();
        Map<Integer, List<String>> thresholds = new ConcurrentHashMap<>();
        String path = "module." + moduleName;
        ConfigurationSection thresholdSection =
            this.visage.getConfigHandler().getConfigurationSection(path + ".thresholds");

        thresholdSection.getKeys(false).forEach(s -> {
            int key;
            try {
                key = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return;
            }
            Object value = this.visage.getConfig().get(path + ".thresholds." + key);

            if (value instanceof String) {
                thresholds.put(key, Collections.singletonList((String) value));
            } else if (value instanceof List) {
                //noinspection unchecked
                thresholds.put(key, (List<String>) value);
            } else {
                Bukkit.getLogger().log(
                    Level.SEVERE,
                    String.format(
                        "%sEntry on key %d is not a string!%s",
                        Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString(),
                        key, Ansi.ansi().reset().toString()
                    )
                );
            }
        });
        return thresholds;
    }
}