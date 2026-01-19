package org.oneyz.holoFX.holograms.displays;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.holograms.visibility.HologramVisibilityManager;
import org.oneyz.holoFX.models.Hologram;
import org.oneyz.holoFX.utils.HologramEditQueueManager;
import org.oneyz.holoFX.utils.HologramPDCManager;
import org.oneyz.holoFX.utils.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global manager for all active holograms on the server
 */
@Getter
public class HologramDisplayManager {

    private final HoloFX plugin;
    private final Map<String, HologramDisplay> activeHolograms;
    private final HologramVisibilityManager visibilityManager;
    private final HologramPDCManager pdcManager;
    private final HologramEditQueueManager editQueueManager;

    public HologramDisplayManager(HoloFX plugin) {
        this.plugin = plugin;
        this.activeHolograms = new ConcurrentHashMap<>();
        this.visibilityManager = new HologramVisibilityManager(plugin);
        this.pdcManager = new HologramPDCManager(plugin);
        this.editQueueManager = new HologramEditQueueManager();
    }

    /**
     * Create and spawn a new hologram
     *
     * @param hologramConfig The hologram configuration
     * @return true if hologram was successfully created and spawned
     */
    public boolean spawnHologram(Hologram hologramConfig) {
        String hologramName = hologramConfig.getName();

        if (activeHolograms.containsKey(hologramName)) {
            Logger.warning("Hologram '" + hologramName + "' is already active!");
            return false;
        }

        try {
            HologramDisplay display = new HologramDisplay(hologramConfig, pdcManager);

            if (display.spawn()) {
                activeHolograms.put(hologramName, display);

                visibilityManager.updateVisibilityAfterSpawn(display, Bukkit.getOnlinePlayers());

                return true;
            }

            return false;

        } catch (Exception e) {
            Logger.severe("Error spawning hologram: " + hologramName, e);
            return false;
        }
    }

    /**
     * Despawn and remove a hologram
     *
     * @param hologramName The name of the hologram
     * @return true if hologram was successfully despawned
     */
    public boolean despawnHologram(String hologramName) {
        HologramDisplay display = activeHolograms.get(hologramName);

        if (display == null) {
            Logger.warning("Hologram '" + hologramName + "' is not active!");
            return false;
        }

        try {
            display.despawn();

            pdcManager.removeAllDisplaysForHologram(hologramName);

            editQueueManager.clearQueue(hologramName);

            activeHolograms.remove(hologramName);
            Logger.info("Despawned hologram: " + hologramName);
            return true;
        } catch (Exception e) {
            Logger.severe("Error despawning hologram: " + hologramName, e);
            return false;
        }
    }

    /**
     * Despawn all active holograms
     */
    public void despawnAllHolograms() {
        List<String> hologramNames = new ArrayList<>(activeHolograms.keySet());
        for (String name : hologramNames) {
            despawnHologram(name);
        }

        pdcManager.removeAllHologramDisplays();

        Logger.info("Despawned all holograms");
    }

    /**
     * Get an active hologram by name
     */
    public HologramDisplay getHologram(String hologramName) {
        return activeHolograms.get(hologramName);
    }

    /**
     * Check if a hologram is active
     */
    public boolean isActive(String hologramName) {
        HologramDisplay display = activeHolograms.get(hologramName);
        return display != null && display.isActive();
    }

    /**
     * Get all active holograms
     */
    public Map<String, HologramDisplay> getAllActiveHolograms() {
        return new HashMap<>(activeHolograms);
    }

    /**
     * Get count of active holograms
     */
    public int getActiveCount() {
        return activeHolograms.size();
    }

    /**
     * Reload holograms - despawn all and reload from configuration
     * This should be called after reloading the configuration files
     */
    public void reloadAllHolograms() {
        despawnAllHolograms();
        Logger.info("All holograms have been reloaded");
    }

}

