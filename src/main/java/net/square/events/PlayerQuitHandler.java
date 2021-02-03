package net.square.events;

import net.square.Visage;
import net.square.storage.DataStorage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerQuitHandler implements Listener {

    private final Visage visage;

    public PlayerQuitHandler(Visage visage) {
        this.visage = visage;
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        invalidate(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void handle(PlayerKickEvent event) {
        invalidate(event.getPlayer().getUniqueId());
    }

    private void invalidate(UUID uniqueId) {
        DataStorage dataStorage = this.visage.getDataStorageList().get(uniqueId);
        if (dataStorage != null)
            dataStorage.entityPastLocations.previousLocations.clear();
        this.visage.getDataStorageList().remove(uniqueId);
        this.visage.getModuleManager().modules.values()
            .forEach(module -> module.getViolations().remove(uniqueId));
    }
}