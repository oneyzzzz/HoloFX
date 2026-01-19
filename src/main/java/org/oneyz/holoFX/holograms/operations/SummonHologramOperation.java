package org.oneyz.holoFX.holograms.operations;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.holograms.displays.HologramDisplay;
import org.oneyz.holoFX.interfaces.hologram.HologramOperation;
import org.oneyz.holoFX.models.Hologram;
import org.oneyz.holoFX.utils.Logger;
import org.oneyz.holoFX.utils.MessageManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SummonHologramOperation implements HologramOperation {

    private final HoloFX plugin;
    private final Player player;
    private final String hologramName;
    private final MessageManager messageManager;


    public SummonHologramOperation(HoloFX plugin, Player player, String hologramName) {
        this.plugin = plugin;
        this.player = player;
        this.hologramName = hologramName;
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean execute() {
        HologramDisplay display = plugin.getHologramDisplayManager().getHologram(hologramName);

        if (display == null) {
            messageManager.sendMessage(player, "summon.not_loaded", Map.of("name", hologramName));
            return false;
        }

        try {
            Hologram hologramConfig = display.getHologramConfig();
            hologramConfig.getLocation().setWorld(player.getWorld().getName());
            hologramConfig.getLocation().setX(player.getLocation().getX());
            hologramConfig.getLocation().setY(player.getLocation().getY() + 1.5);
            hologramConfig.getLocation().setZ(player.getLocation().getZ());
            hologramConfig.getLocation().setYaw(player.getLocation().getYaw());

            if (!saveHologramToFile(hologramConfig)) {
                messageManager.sendMessage(player, "summon.summon_failed", Map.of("error", "Failed to save"));
                return false;
            }

            plugin.getHologramDisplayManager().despawnHologram(hologramName);
            plugin.getHologramLoader().reloadAll();

            Hologram reloadedHologram = plugin.getHologramLoader().getAllHolograms().values().stream()
                    .filter(h -> h.getName().equalsIgnoreCase(hologramName))
                    .findFirst()
                    .orElse(null);

            if (reloadedHologram == null) {
                messageManager.sendMessage(player, "summon.summon_failed", Map.of("error", "Failed to reload"));
                return false;
            }

            if (plugin.getHologramDisplayManager().spawnHologram(reloadedHologram)) {
                messageManager.sendMessage(player, "summon.summoned", Map.of("name", hologramName));
                return true;
            } else {
                messageManager.sendMessage(player, "summon.summon_failed", Map.of("error", "Failed to spawn"));
                return false;
            }

        } catch (Exception e) {
            messageManager.sendMessage(player, "summon.summon_failed", Map.of("error", e.getMessage()));
            Logger.severe("Error summoning hologram: " + hologramName, e);
            return false;
        }
    }

    /**
     * Save the hologram configuration to file
     */
    private boolean saveHologramToFile(Hologram hologram) {
        try {
            File dataFolder = new File(plugin.getDataFolder(), "holograms");
            File hologramFile = new File(dataFolder, hologramName + ".yml");

            if (!hologramFile.exists()) {
                messageManager.sendMessage(player, "summon.summon_failed", Map.of("error", "File not found"));
                return false;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(hologramFile);

            config.set("location.world", hologram.getLocation().getWorld());
            config.set("location.x", hologram.getLocation().getX());
            config.set("location.y", hologram.getLocation().getY());
            config.set("location.z", hologram.getLocation().getZ());
            config.set("location.yaw", hologram.getLocation().getYaw());

            config.save(hologramFile);
            return true;

        } catch (IOException e) {
            Logger.severe("Error saving hologram configuration: " + hologramName, e);
            return false;
        }
    }


}

