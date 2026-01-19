package org.oneyz.holoFX.holograms.visibility;

import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.holograms.displays.HologramDisplay;
import org.oneyz.holoFX.models.Hologram;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for handling hologram line visibility based on player permissions
 */
public class HologramVisibilityManager {

    private final HoloFX plugin;

    /**
     * Cache for player visibility permissions: player UUID -> (hologram name -> set of visible line indices)
     */
    private final Map<String, Map<String, Set<Integer>>> playerVisibilityCache = new ConcurrentHashMap<>();

    public HologramVisibilityManager(HoloFX plugin) {
        this.plugin = plugin;
    }

    /**
     * Update visibility of all holograms for a specific player
     * Call this on player join or when permissions change
     *
     * @param player The player to update visibility for
     * @param allHolograms Map of all hologram displays
     */
    public void updateVisibilityForPlayer(Player player, Map<String, HologramDisplay> allHolograms) {
        String playerUuid = player.getUniqueId().toString();
        playerVisibilityCache.remove(playerUuid);

        Map<String, Set<Integer>> playerVisibility = new ConcurrentHashMap<>();
        for (Map.Entry<String, HologramDisplay> entry : allHolograms.entrySet()) {
            String hologramName = entry.getKey();
            HologramDisplay display = entry.getValue();

            if (!display.isActive()) {
                continue;
            }
            Set<Integer> visibleLines = new HashSet<>();
            List<Hologram.Line> lines = display.getHologramConfig().getLines();

            for (int i = 0; i < lines.size(); i++) {
                Hologram.Line line = lines.get(i);
                Hologram.DisplaySettings settings = line.getDisplaySettings();
                if (settings.hasPermissionRequirement()) {
                    String permission = settings.getPermission();
                    if (player.hasPermission(permission)) {
                        visibleLines.add(i);
                        showLineToPlayer(player, display, i);
                    } else {
                        hideLineFromPlayer(player, display, i);
                    }
                } else {
                    visibleLines.add(i);
                    showLineToPlayer(player, display, i);
                }
            }

            playerVisibility.put(hologramName, visibleLines);
        }

        playerVisibilityCache.put(playerUuid, playerVisibility);
    }

    /**
     * Show a specific hologram line to a player
     */
    private void showLineToPlayer(Player player, HologramDisplay display, int lineIndex) {
        TextDisplay textDisplay = display.getDisplay(lineIndex);
        if (textDisplay != null && textDisplay.isValid()) {
            player.showEntity(plugin, textDisplay);
        }
    }

    /**
     * Hide a specific hologram line from a player
     */
    private void hideLineFromPlayer(Player player, HologramDisplay display, int lineIndex) {
        TextDisplay textDisplay = display.getDisplay(lineIndex);
        if (textDisplay != null && textDisplay.isValid()) {
            player.hideEntity(plugin, textDisplay);
        }
    }

    /**
     * Hide a TextDisplay from a player
     */
    public void hideEntityFromPlayer(Player player, TextDisplay entity) {
        if (entity != null && entity.isValid()) {
            player.hideEntity(plugin, entity);
        }
    }

    /**
     * Show a TextDisplay to a player
     */
    public void showEntityToPlayer(Player player, TextDisplay entity) {
        if (entity != null && entity.isValid()) {
            player.showEntity(plugin, entity);
        }
    }

    /**
     * Get visible lines for a player in a specific hologram
     */
    public Set<Integer> getVisibleLines(Player player, String hologramName) {
        String playerUuid = player.getUniqueId().toString();
        Map<String, Set<Integer>> playerVisibility = playerVisibilityCache.get(playerUuid);

        if (playerVisibility == null) {
            return new HashSet<>();
        }

        return playerVisibility.getOrDefault(hologramName, new HashSet<>());
    }

    /**
     * Check if a specific line is visible to a player
     */
    public boolean isLineVisibleToPlayer(Player player, String hologramName, int lineIndex) {
        Set<Integer> visibleLines = getVisibleLines(player, hologramName);
        return visibleLines.contains(lineIndex);
    }

    /**
     * Clear cache for a player (call this on player leave)
     */
    public void clearPlayerCache(Player player) {
        playerVisibilityCache.remove(player.getUniqueId().toString());
    }

    /**
     * Clear all cache
     */
    public void clearAllCache() {
        playerVisibilityCache.clear();
    }

    /**
     * Update visibility when a hologram is spawned
     * Hide lines with permission requirements from players who don't have permission
     */
    public void updateVisibilityAfterSpawn(HologramDisplay display, Collection<? extends Player> onlinePlayers) {
        List<Hologram.Line> lines = display.getHologramConfig().getLines();

        for (int i = 0; i < lines.size(); i++) {
            Hologram.Line line = lines.get(i);
            Hologram.DisplaySettings settings = line.getDisplaySettings();
            if (settings.hasPermissionRequirement()) {
                String permission = settings.getPermission();
                TextDisplay textDisplay = display.getDisplay(i);

                if (textDisplay != null && textDisplay.isValid()) {
                    for (Player player : onlinePlayers) {
                        if (!player.hasPermission(permission)) {
                            hideLineFromPlayer(player, display, i);
                        }
                    }
                }
            }
        }
    }
}

