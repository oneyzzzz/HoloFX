package org.oneyz.holoFX.loader;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.holograms.displays.HologramDisplayManager;
import org.oneyz.holoFX.models.Hologram;
import org.oneyz.holoFX.utils.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Batch processor for loading holograms asynchronously and spawning them efficiently.
 * Handles large amounts of holograms without blocking the main thread.
 */
public class HologramBatchLoader {

    private final HoloFX plugin;
    private final HologramLoader hologramLoader;
    private final HologramDisplayManager displayManager;
    private final int batchSize;
    private final long delayBetweenBatchesTicks;

    private BukkitTask currentBatchTask;
    private volatile boolean isProcessing = false;
    private final Map<String, BatchProgress> batchProgress = new ConcurrentHashMap<>();

    /**
     * Create a new HologramBatchLoader
     *
     * @param plugin The plugin instance
     * @param hologramLoader The hologram loader
     * @param displayManager The display manager
     * @param batchSize Number of holograms to process per batch (default: 5)
     * @param delayBetweenBatchesTicks Delay in ticks between batches (default: 10)
     */
    public HologramBatchLoader(HoloFX plugin, HologramLoader hologramLoader,
                               HologramDisplayManager displayManager, int batchSize,
                               long delayBetweenBatchesTicks) {
        this.plugin = plugin;
        this.hologramLoader = hologramLoader;
        this.displayManager = displayManager;
        this.batchSize = batchSize > 0 ? batchSize : 5;
        this.delayBetweenBatchesTicks = delayBetweenBatchesTicks > 0 ? delayBetweenBatchesTicks : 10;
    }

    /**
     * Create a new HologramBatchLoader with default batch settings
     */
    public HologramBatchLoader(HoloFX plugin, HologramLoader hologramLoader,
                               HologramDisplayManager displayManager) {
        this(plugin, hologramLoader, displayManager, 5, 10);
    }

    /**
     * Load and spawn all holograms that are enabled and have valid worlds.
     * Uses batch processing to avoid server lag.
     */
    public void loadAndSpawnAllHolograms() {
        if (isProcessing) {
            Logger.warning("A batch loading operation is already in progress!");
            return;
        }

        Map<String, Hologram> holograms = hologramLoader.getAllHolograms();

        if (holograms.isEmpty()) {
            Logger.info("No holograms to spawn");
            return;
        }
        List<Hologram> validHolograms = new ArrayList<>();
        for (Hologram hologram : holograms.values()) {
            if (hologram.isEnabled() && hologram.hasValidWorld()) {
                validHolograms.add(hologram);
            }
        }

        if (validHolograms.isEmpty()) {
            Logger.warning("No valid holograms found to spawn (check if worlds exist)");
            return;
        }

        Logger.info("Starting batch spawn of " + validHolograms.size() + " hologram(s)");
        startBatchProcessing(validHolograms);
    }

    /**
     * Load and spawn holograms from a specific world
     */
    public void loadAndSpawnHologramsFromWorld(String worldName) {
        if (isProcessing) {
            Logger.warning("A batch loading operation is already in progress!");
            return;
        }

        Map<String, Hologram> holograms = hologramLoader.getAllHolograms();

        List<Hologram> validHolograms = new ArrayList<>();
        for (Hologram hologram : holograms.values()) {
            if (hologram.isEnabled() &&
                    hologram.hasValidWorld() &&
                    hologram.getLocation().getWorld().equals(worldName)) {
                validHolograms.add(hologram);
            }
        }

        if (validHolograms.isEmpty()) {
            Logger.warning("No valid holograms found in world: " + worldName);
            return;
        }

        Logger.info("Starting batch spawn of " + validHolograms.size() +
                " hologram(s) from world: " + worldName);
        startBatchProcessing(validHolograms);
    }

    /**
     * Start processing holograms in batches
     */
    private void startBatchProcessing(List<Hologram> holograms) {
        isProcessing = true;
        String batchId = UUID.randomUUID().toString().substring(0, 8);
        batchProgress.put(batchId, new BatchProgress(holograms.size()));

        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Hologram> hologramsList = new ArrayList<>(holograms);

        currentBatchTask = Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> processBatch(hologramsList, processedCount, successCount, batchId),
                0L,
                delayBetweenBatchesTicks
        );
    }

    /**
     * Process a single batch of holograms
     */
    private void processBatch(List<Hologram> holograms, AtomicInteger processedCount,
                             AtomicInteger successCount, String batchId) {
        int startIndex = processedCount.get();
        int endIndex = Math.min(startIndex + batchSize, holograms.size());
        for (int i = startIndex; i < endIndex; i++) {
            Hologram hologram = holograms.get(i);

            try {
                if (displayManager.spawnHologram(hologram)) {
                    successCount.incrementAndGet();
                    Logger.fine("Spawned hologram: " + hologram.getName());
                } else {
                    Logger.warning("Failed to spawn hologram: " + hologram.getName());
                }
            } catch (Exception e) {
                Logger.severe("Error spawning hologram: " + hologram.getName(), e);
            }

            processedCount.incrementAndGet();
        }

        BatchProgress progress = batchProgress.get(batchId);
        if (progress != null) {
            progress.processed = processedCount.get();
        }
        if (processedCount.get() >= holograms.size()) {
            isProcessing = false;
            currentBatchTask.cancel();

            Logger.info("§aCompleted spawning " + successCount.get() + " out of " +
                    holograms.size() + " hologram(s)");

            batchProgress.remove(batchId);
        }
    }

    /**
     * Cancel current batch processing
     */
    public void cancelBatchProcessing() {
        if (currentBatchTask != null) {
            currentBatchTask.cancel();
        }
        isProcessing = false;
        batchProgress.clear();
        Logger.info("Batch processing cancelled");
    }

    /**
     * Check if a batch is currently processing
     */
    public boolean isProcessing() {
        return isProcessing;
    }

    /**
     * Get progress of current batch operation
     */
    public BatchProgress getCurrentProgress() {
        if (batchProgress.isEmpty()) {
            return null;
        }
        return batchProgress.values().iterator().next();
    }

    /**
     * Inner class to track batch progress
     */
    public static class BatchProgress {
        public final int total;
        public volatile int processed;

        public BatchProgress(int total) {
            this.total = total;
            this.processed = 0;
        }

        public int getPercentage() {
            if (total == 0) return 0;
            return (int) ((processed / (double) total) * 100);
        }

        @Override
        public String toString() {
            return processed + "/" + total + " (" + getPercentage() + "%)";
        }
    }
}

