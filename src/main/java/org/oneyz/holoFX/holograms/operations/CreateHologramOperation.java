package org.oneyz.holoFX.holograms.operations;

import org.bukkit.entity.Player;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.interfaces.hologram.HologramOperation;
import org.oneyz.holoFX.loader.HologramLoader;
import org.oneyz.holoFX.models.Hologram;
import org.oneyz.holoFX.utils.Logger;
import org.oneyz.holoFX.utils.MessageManager;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Operation for creating a new hologram at a player's location
 */
public class CreateHologramOperation implements HologramOperation {

    private final HoloFX plugin;
    private final Player player;
    private final String hologramName;
    private final String hologramText;
    private final MessageManager messageManager;


    /**
     * Create a new hologram creation operation
     *
     * @param plugin The plugin instance
     * @param player The player creating the hologram
     * @param hologramName The name of the hologram
     * @param hologramText The text to display on the hologram
     */
    public CreateHologramOperation(HoloFX plugin, Player player, String hologramName, String hologramText) {
        this.plugin = plugin;
        this.player = player;
        this.hologramName = hologramName;
        this.hologramText = hologramText;
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean execute() {
        if (!validateInputs()) {
            return false;
        }

        if (hologramExists()) {
            messageManager.sendMessage(player, "create.already_exists", Map.of("name", hologramName));
            return false;
        }

        try {
            File hologramFile = createHologramFile();
            if (hologramFile == null) {
                messageManager.sendMessage(player, "create.creation_failed", Map.of("error", "Failed to create file"));
                return false;
            }

            if (!writeHologramConfig(hologramFile)) {
                hologramFile.delete();
                return false;
            }

            HologramLoader loader = plugin.getHologramLoader();
            loader.reloadAll();

            Hologram newHologram = loader.getHologram(hologramName);
            if (newHologram != null) {
                if (plugin.getHologramDisplayManager().spawnHologram(newHologram)) {
                    messageManager.sendMessage(player, "create.created", Map.of("name", hologramName));
                } else {
                    messageManager.sendMessage(player, "create.creation_failed", Map.of("error", "Failed to spawn"));
                }
            } else {
                messageManager.sendMessage(player, "create.creation_failed", Map.of("error", "Failed to load hologram"));
                return false;
            }

            return true;

        } catch (Exception e) {
            messageManager.sendMessage(player, "create.creation_failed", Map.of("error", e.getMessage()));
            Logger.severe("Error creating hologram: " + hologramName, e);
            return false;
        }
    }

    /**
     * Validate input parameters
     */
    private boolean validateInputs() {
        if (hologramName == null || hologramName.trim().isEmpty()) {
            messageManager.sendMessage(player, "create.invalid_name");
            return false;
        }

        if (!hologramName.matches("[a-zA-Z0-9_-]+")) {
            messageManager.sendMessage(player, "create.invalid_name");
            return false;
        }

        if (hologramText == null || hologramText.trim().isEmpty()) {
            messageManager.sendMessage(player, "create.creation_failed", Map.of("error", "Text cannot be empty"));
            return false;
        }

        if (hologramText.length() > 255) {
            messageManager.sendMessage(player, "create.creation_failed", Map.of("error", "Text too long"));
            return false;
        }

        return true;
    }

    /**
     * Check if a hologram with this name already exists
     */
    private boolean hologramExists() {
        Map<String, Hologram> allHolograms = plugin.getHologramLoader().getAllHolograms();
        return allHolograms.values().stream()
                .anyMatch(h -> h.getName().equalsIgnoreCase(hologramName));
    }

    /**
     * Create the hologram YAML file
     */
    private File createHologramFile() {
        File dataFolder = new File(plugin.getDataFolder(), "holograms");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File hologramFile = new File(dataFolder, hologramName + ".yml");
        if (hologramFile.exists()) {
            return null;
        }

        try {
            hologramFile.createNewFile();
            return hologramFile;
        } catch (IOException e) {
            Logger.severe("Failed to create hologram file: " + hologramName, e);
            return null;
        }
    }

    /**
     * Write the hologram configuration to the YAML file
     */
    private boolean writeHologramConfig(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            Map<String, Object> config = buildConfigMap();
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            options.setIndent(2);

            Yaml yaml = new Yaml(options);
            yaml.dump(config, writer);
            return true;
        } catch (IOException e) {
            messageManager.sendMessage(player, "create.creation_failed", Map.of("error", e.getMessage()));
            Logger.severe("Error writing hologram config for: " + hologramName, e);
            return false;
        }
    }

    /**
     * Build the configuration map for the YAML file
     */
    private Map<String, Object> buildConfigMap() {
        Map<String, Object> config = new LinkedHashMap<>();

        config.put("enabled", true);

        Map<String, Object> location = new LinkedHashMap<>();
        location.put("world", player.getWorld().getName());
        location.put("x", player.getLocation().getX());
        location.put("y", player.getLocation().getY() + 1.5); // Slightly above player
        location.put("z", player.getLocation().getZ());
        location.put("yaw", player.getLocation().getYaw());
        config.put("location", location);

        List<Map<String, Object>> lines = new ArrayList<>();
        Map<String, Object> line = buildDefaultLine();
        lines.add(line);
        config.put("lines", lines);

        return config;
    }

    /**
     * Build a default line configuration
     */
    private Map<String, Object> buildDefaultLine() {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("text", hologramText);
        Map<String, Object> offset = new LinkedHashMap<>();
        offset.put("x", 0.0);
        offset.put("y", 0.0);
        offset.put("z", 0.0);
        line.put("offset", offset);
        Map<String, Object> displaySettings = new LinkedHashMap<>();
        displaySettings.put("text_opacity", 255);
        displaySettings.put("line_width", 200);
        displaySettings.put("text_alignment", "CENTER");
        displaySettings.put("default_background", true);
        displaySettings.put("see_through", false);
        displaySettings.put("shadow", true);
        displaySettings.put("billboard", false);
        line.put("display_settings", displaySettings);

        return line;
    }

}

