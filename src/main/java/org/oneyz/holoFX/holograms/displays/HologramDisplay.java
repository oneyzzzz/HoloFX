package org.oneyz.holoFX.holograms.displays;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.TextDisplay;
import org.oneyz.holoFX.models.Hologram;
import org.oneyz.holoFX.utils.HologramPDCManager;
import org.oneyz.holoFX.utils.Logger;

import java.util.*;
import java.util.UUID;

/**
 * Manages a complete hologram consisting of multiple TextDisplay entities
 */
@Getter
public class HologramDisplay {

    private final String hologramName;
    private final Hologram hologramConfig;
    private final Map<Integer, TextDisplay> displayLines; // Line index -> TextDisplay entity
    private final UUID hologramUuid; // Unique identifier for this hologram instance
    private Location baseLocation;

    private boolean isActive;
    private HologramPDCManager pdcManager;

    /**
     * Create a new HologramDisplay
     *
     * @param hologramConfig The hologram configuration
     * @param pdcManager The PDC manager for marking entities
     */
    public HologramDisplay(Hologram hologramConfig, HologramPDCManager pdcManager) {
        this.hologramName = hologramConfig.getName();
        this.hologramConfig = hologramConfig;
        this.baseLocation = hologramConfig.getLocation().toBukkitLocation();
        this.displayLines = new HashMap<>();
        this.isActive = false;
        this.hologramUuid = UUID.randomUUID();
        this.pdcManager = pdcManager;
    }

    /**
     * Spawn all TEXT_DISPLAY entities for this hologram
     *
     * @return true if all displays were spawned successfully
     */
    public boolean spawn() {
        if (isActive) {
            Logger.warning("Hologram '" + hologramName + "' is already active!");
            return false;
        }

        if (!hologramConfig.isEnabled()) {
            Logger.fine("Hologram '" + hologramName + "' is disabled, skipping spawn");
            return false;
        }

        if (!hologramConfig.hasValidWorld()) {
            Logger.severe("Hologram '" + hologramName + "' has invalid world!");
            return false;
        }

        try {
            List<Hologram.Line> lines = hologramConfig.getLines();

            for (int i = 0; i < lines.size(); i++) {
                Hologram.Line line = lines.get(i);
                TextDisplay display = TextDisplayManager.createTextDisplay(baseLocation, line);

                if (display == null) {
                    Logger.warning("Failed to create TextDisplay for line " + (i + 1) +
                            " in hologram: " + hologramName);
                    continue;
                }
                if (pdcManager != null) {
                    pdcManager.markAsHologram(display, hologramName, i, hologramUuid);
                }

                displayLines.put(i, display);
            }

            if (displayLines.isEmpty()) {
                Logger.severe("No TextDisplay entities were created for hologram: " + hologramName);
                return false;
            }

            this.isActive = true;
            Logger.info("Spawned hologram '" + hologramName + "' with " + displayLines.size() + " line(s)");
            return true;

        } catch (Exception e) {
            Logger.severe("Error spawning hologram: " + hologramName, e);
            despawn();
            return false;
        }
    }

    /**
     * Despawn all TEXT_DISPLAY entities for this hologram
     */
    public void despawn() {
        for (TextDisplay display : displayLines.values()) {
            TextDisplayManager.removeTextDisplay(display);
        }
        displayLines.clear();
        this.isActive = false;
        Logger.fine("Despawned hologram: " + hologramName);
    }

    /**
     * Update a specific line in the hologram
     *
     * @param lineIndex The index of the line (0-based)
     * @param newLine The new line configuration
     * @return true if update was successful
     */
    public boolean updateLine(int lineIndex, Hologram.Line newLine) {
        if (!isActive) {
            Logger.warning("Cannot update line on inactive hologram: " + hologramName);
            return false;
        }

        if (lineIndex < 0 || lineIndex >= displayLines.size()) {
            Logger.warning("Invalid line index: " + lineIndex + " for hologram: " + hologramName);
            return false;
        }

        TextDisplay display = displayLines.get(lineIndex);
        if (display == null || !display.isValid()) {
            Logger.warning("TextDisplay entity is no longer valid for line: " + lineIndex);
            return respawnLine(lineIndex);
        }

        try {
            TextDisplayManager.updateTextDisplay(display, newLine);
            return true;
        } catch (Exception e) {
            Logger.severe("Error updating line " + lineIndex + " in hologram: " + hologramName, e);
            return false;
        }
    }

    /**
     * Respawn a single line (if it got deleted or broken)
     */
    private boolean respawnLine(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= hologramConfig.getLines().size()) {
            return false;
        }

        try {
            TextDisplay oldDisplay = displayLines.get(lineIndex);
            if (oldDisplay != null) {
                TextDisplayManager.removeTextDisplay(oldDisplay);
            }
            Hologram.Line line = hologramConfig.getLines().get(lineIndex);
            TextDisplay newDisplay = TextDisplayManager.createTextDisplay(baseLocation, line);

            if (newDisplay != null) {
                displayLines.put(lineIndex, newDisplay);
                Logger.info("Respawned line " + (lineIndex + 1) + " in hologram: " + hologramName);
                return true;
            }

            return false;
        } catch (Exception e) {
            Logger.severe("Error respawning line " + lineIndex + " in hologram: " + hologramName, e);
            return false;
        }
    }

    /**
     * Move the hologram to a new location
     *
     * @param newLocation The new location
     * @return true if move was successful
     */
    public boolean moveTo(Location newLocation) {
        if (!isActive) {
            Logger.warning("Cannot move inactive hologram: " + hologramName);
            return false;
        }

        try {
            for (int i = 0; i < hologramConfig.getLines().size(); i++) {
                TextDisplay display = displayLines.get(i);
                if (display != null && display.isValid()) {
                    Hologram.Line line = hologramConfig.getLines().get(i);
                    Location newDisplayLocation = newLocation.clone().add(
                            line.getOffset().getX(),
                            line.getOffset().getY(),
                            line.getOffset().getZ()
                    );
                    display.teleport(newDisplayLocation);
                }
            }

            this.baseLocation = newLocation.clone();

            return true;
        } catch (Exception e) {
            Logger.severe("Error moving hologram: " + hologramName, e);
            return false;
        }
    }

    /**
     * Get the count of active TextDisplay entities
     */
    public int getActiveLineCount() {
        return displayLines.size();
    }

    /**
     * Check if a specific line is spawned
     */
    public boolean isLineSpawned(int lineIndex) {
        TextDisplay display = displayLines.get(lineIndex);
        return display != null && display.isValid();
    }

    /**
     * Get a specific TextDisplay entity
     */
    public TextDisplay getDisplay(int lineIndex) {
        return displayLines.get(lineIndex);
    }

}

