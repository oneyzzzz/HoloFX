package org.oneyz.holoFX.utils;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.oneyz.holoFX.HoloFX;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HologramPDCManager {

    private static final String HOLOGRAM_PDC_KEY = "hologram";
    private static final String HOLOGRAM_NAME_KEY = "hologram_name";
    private static final String HOLOGRAM_LINE_INDEX_KEY = "line_index";
    private static final String HOLOGRAM_UUID_KEY = "hologram_uuid";

    private final HoloFX plugin;
    private final NamespacedKey hologramKey;
    private final NamespacedKey hologramNameKey;
    private final NamespacedKey lineIndexKey;
    private final NamespacedKey hologramUuidKey;

    public HologramPDCManager(HoloFX plugin) {
        this.plugin = plugin;
        this.hologramKey = new NamespacedKey(plugin, HOLOGRAM_PDC_KEY);
        this.hologramNameKey = new NamespacedKey(plugin, HOLOGRAM_NAME_KEY);
        this.lineIndexKey = new NamespacedKey(plugin, HOLOGRAM_LINE_INDEX_KEY);
        this.hologramUuidKey = new NamespacedKey(plugin, HOLOGRAM_UUID_KEY);
    }

    /**
     * Mark a TextDisplay entity as belonging to a hologram
     *
     * @param display The TextDisplay entity
     * @param hologramName The name of the hologram
     * @param lineIndex The line index (0-based)
     * @param hologramUuid Unique identifier for this hologram instance
     */
    public void markAsHologram(TextDisplay display, String hologramName, int lineIndex, UUID hologramUuid) {
        try {
            display.getPersistentDataContainer().set(hologramKey, PersistentDataType.BYTE, (byte) 1);
            display.getPersistentDataContainer().set(hologramNameKey, PersistentDataType.STRING, hologramName);
            display.getPersistentDataContainer().set(lineIndexKey, PersistentDataType.INTEGER, lineIndex);
            display.getPersistentDataContainer().set(hologramUuidKey, PersistentDataType.STRING, hologramUuid.toString());
        } catch (Exception e) {
            Logger.warning("Failed to mark TextDisplay as hologram: " + e.getMessage());
        }
    }

    /**
     * Check if an entity is a hologram TextDisplay
     */
    public boolean isHologramDisplay(Entity entity) {
        if (!(entity instanceof TextDisplay)) {
            return false;
        }

        try {
            return entity.getPersistentDataContainer().has(hologramKey, PersistentDataType.BYTE);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the hologram name from a TextDisplay entity
     */
    public String getHologramName(TextDisplay display) {
        try {
            return display.getPersistentDataContainer().get(hologramNameKey, PersistentDataType.STRING);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the line index from a TextDisplay entity
     */
    public Integer getLineIndex(TextDisplay display) {
        try {
            return display.getPersistentDataContainer().get(lineIndexKey, PersistentDataType.INTEGER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the hologram UUID from a TextDisplay entity
     */
    public UUID getHologramUuid(TextDisplay display) {
        try {
            String uuidStr = display.getPersistentDataContainer().get(hologramUuidKey, PersistentDataType.STRING);
            return uuidStr != null ? UUID.fromString(uuidStr) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Remove a hologram TextDisplay entity
     */
    public void removeHologramDisplay(TextDisplay display) {
        try {
            display.remove();
        } catch (Exception e) {
            Logger.warning("Failed to remove hologram display: " + e.getMessage());
        }
    }

    /**
     * Remove all TextDisplay entities for a specific hologram from all worlds
     *
     * @param hologramName The name of the hologram
     * @return Number of displays removed
     */
    public int removeAllDisplaysForHologram(String hologramName) {
        int removedCount = 0;

        try {
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (isHologramDisplay(entity)) {
                        TextDisplay display = (TextDisplay) entity;
                        String displayHologramName = getHologramName(display);

                        if (displayHologramName != null && displayHologramName.equalsIgnoreCase(hologramName)) {
                            removeHologramDisplay(display);
                            removedCount++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.warning("Error removing displays for hologram '" + hologramName + "': " + e.getMessage());
        }

        if (removedCount > 0) {
            Logger.info("Removed " + removedCount + " hologram display(s) for: " + hologramName);
        }

        return removedCount;
    }

    /**
     * Remove all TextDisplay entities for a specific hologram UUID
     *
     * @param hologramUuid The UUID of the hologram instance
     * @return Number of displays removed
     */
    public int removeAllDisplaysForHologramUuid(UUID hologramUuid) {
        int removedCount = 0;

        try {
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (isHologramDisplay(entity)) {
                        TextDisplay display = (TextDisplay) entity;
                        UUID displayUuid = getHologramUuid(display);

                        if (displayUuid != null && displayUuid.equals(hologramUuid)) {
                            removeHologramDisplay(display);
                            removedCount++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.warning("Error removing displays for hologram UUID '" + hologramUuid + "': " + e.getMessage());
        }

        return removedCount;
    }

    /**
     * Get all TextDisplay entities for a specific hologram
     *
     * @param hologramName The name of the hologram
     * @return Set of TextDisplay entities
     */
    public Set<TextDisplay> getAllDisplaysForHologram(String hologramName) {
        Set<TextDisplay> displays = new HashSet<>();

        try {
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (isHologramDisplay(entity)) {
                        TextDisplay display = (TextDisplay) entity;
                        String displayHologramName = getHologramName(display);

                        if (displayHologramName != null && displayHologramName.equalsIgnoreCase(hologramName)) {
                            displays.add(display);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.warning("Error getting displays for hologram '" + hologramName + "': " + e.getMessage());
        }

        return displays;
    }

    /**
     * Remove all hologram displays from all worlds
     * Use this during plugin shutdown
     */
    public void removeAllHologramDisplays() {
        int removedCount = 0;

        try {
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (isHologramDisplay(entity)) {
                        removeHologramDisplay((TextDisplay) entity);
                        removedCount++;
                    }
                }
            }
        } catch (Exception e) {
            Logger.warning("Error removing all hologram displays: " + e.getMessage());
        }

        Logger.info("Removed " + removedCount + " total hologram display(s)");
    }
}


