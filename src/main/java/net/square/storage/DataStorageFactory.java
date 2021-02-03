package net.square.storage;

import net.square.Visage;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class DataStorageFactory {

    private final List<DataStorage> activeDataStorages = new ArrayList<>();
    private BukkitTask storageCycleTask = null;

    public DataStorage create(Player player) {
        DataStorage dataStorage = new DataStorage(player);
        this.activeDataStorages.add(dataStorage);
        return dataStorage;
    }

    public void startStorageCycle(Visage visage) {
        this.storageCycleTask = new BukkitRunnable() {
            public void run() {
                for (DataStorage dataStorage : activeDataStorages) {
                    if (dataStorage.livingEntity != null) {
                        dataStorage.entityPastLocations.addLocation(dataStorage.livingEntity.getLocation());
                    }
                }
            }
        }.runTaskTimerAsynchronously(visage, 0L, 1L);
    }

    public void shutdown() {
        this.storageCycleTask.cancel();
        this.activeDataStorages.clear();
    }
}