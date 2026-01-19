package org.oneyz.holoFX.holograms.operations;

import org.bukkit.command.CommandSender;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.interfaces.hologram.HologramOperation;
import org.oneyz.holoFX.loader.HologramLoader;
import org.oneyz.holoFX.models.Hologram;
import org.oneyz.holoFX.utils.Logger;
import org.oneyz.holoFX.utils.MessageManager;

import java.io.File;
import java.util.Map;

public class RemoveHologramOperation implements HologramOperation {

    private final HoloFX plugin;
    private final CommandSender sender;
    private final String hologramName;
    private final MessageManager messageManager;


    /**
     * Create a new hologram removal operation
     *
     * @param plugin The plugin instance
     * @param sender The command sender
     * @param hologramName The name of the hologram to remove
     */
    public RemoveHologramOperation(HoloFX plugin, CommandSender sender, String hologramName) {
        this.plugin = plugin;
        this.sender = sender;
        this.hologramName = hologramName;
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean execute() {
        if (hologramName == null || hologramName.trim().isEmpty()) {
            messageManager.sendMessage(sender, "remove.not_found", Map.of("name", hologramName));
            return false;
        }

        try {
            HologramLoader loader = plugin.getHologramLoader();
            Hologram hologram = null;
            String fileKey = null;

            for (String key : loader.getAllHolograms().keySet()) {
                Hologram h = loader.getHologram(key);
                if (h != null && h.getName().equalsIgnoreCase(hologramName)) {
                    hologram = h;
                    fileKey = key;
                    break;
                }
            }

            if (hologram == null) {
                messageManager.sendMessage(sender, "remove.not_found", Map.of("name", hologramName));
                return false;
            }

            if (plugin.getHologramDisplayManager().isActive(hologramName)) {
                if (!plugin.getHologramDisplayManager().despawnHologram(hologramName)) {
                    Logger.warning("Failed to despawn active hologram: " + hologramName);
                }
            }

            if (!deleteHologramFile(fileKey)) {
                messageManager.sendMessage(sender, "remove.remove_failed", Map.of("error", "Cannot delete file"));
                return false;
            }

            clearHologramCache();

            loader.reloadAll();

            messageManager.sendMessage(sender, "remove.removed", Map.of("name", hologramName));
            return true;

        } catch (Exception e) {
            messageManager.sendMessage(sender, "remove.remove_failed", Map.of("error", e.getMessage()));
            Logger.severe("Error removing hologram: " + hologramName, e);
            return false;
        }
    }

    /**
     * Clear all cached data related to this hologram
     */
    private void clearHologramCache() {
        try {
            plugin.getHologramDisplayManager().getAllActiveHolograms().remove(hologramName);

            Logger.info("Cleared cache for hologram: " + hologramName);
        } catch (Exception e) {
            Logger.warning("Error clearing hologram cache: " + e.getMessage());
        }
    }

    /**
     * Delete the hologram configuration file
     */
    private boolean deleteHologramFile(String fileKey) {
        File dataFolder = new File(plugin.getDataFolder(), "holograms");
        File hologramFile = new File(dataFolder, fileKey + ".yml");

        if (!hologramFile.exists()) {
            Logger.warning("Hologram file does not exist: " + hologramFile.getAbsolutePath());
            return false;
        }

        try {
            boolean deleted = hologramFile.delete();
            if (deleted) {
                Logger.info("Deleted hologram file: " + hologramFile.getName());
            }
            return deleted;
        } catch (Exception e) {
            Logger.severe("Failed to delete hologram file: " + hologramFile.getAbsolutePath(), e);
            return false;
        }
    }


}

