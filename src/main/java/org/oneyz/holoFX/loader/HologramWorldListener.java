package org.oneyz.holoFX.loader;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.oneyz.holoFX.utils.Logger;

/**
 * Listener for world-related events to handle hologram loading
 */
public class HologramWorldListener implements Listener {

    private final HologramBatchLoader batchLoader;
    private volatile boolean hasInitialLoadCompleted = false;

    public HologramWorldListener(HologramBatchLoader batchLoader) {
        this.batchLoader = batchLoader;
    }

    /**
     * Listen for world load events and spawn holograms if needed
     */
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        String worldName = event.getWorld().getName();
        Logger.info("World '" + worldName + "' has been loaded. Spawning holograms...");
        batchLoader.loadAndSpawnHologramsFromWorld(worldName);
    }

    /**
     * Mark initial load as completed
     */
    public void setInitialLoadCompleted() {
        hasInitialLoadCompleted = true;
    }

    /**
     * Check if initial load has completed
     */
    public boolean hasInitialLoadCompleted() {
        return hasInitialLoadCompleted;
    }
}

