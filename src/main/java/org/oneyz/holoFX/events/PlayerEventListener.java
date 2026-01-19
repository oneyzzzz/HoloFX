package org.oneyz.holoFX.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.holograms.displays.HologramDisplayManager;
import org.oneyz.holoFX.holograms.visibility.HologramVisibilityManager;

/**
 * Event listener for player-related events
 */
public class PlayerEventListener implements Listener {

    private final HoloFX plugin;
    private final HologramDisplayManager displayManager;
    private final HologramVisibilityManager visibilityManager;

    public PlayerEventListener(HoloFX plugin, HologramDisplayManager displayManager, HologramVisibilityManager visibilityManager) {
        this.plugin = plugin;
        this.displayManager = displayManager;
        this.visibilityManager = visibilityManager;
    }

    /**
     * Handle player join - update hologram visibility
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                visibilityManager.updateVisibilityForPlayer(
                        event.getPlayer(),
                        displayManager.getAllActiveHolograms()
                ), 1L);
    }

    /**
     * Handle player quit - clear visibility cache and stop any active move sessions
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        visibilityManager.clearPlayerCache(event.getPlayer());
    }
}

