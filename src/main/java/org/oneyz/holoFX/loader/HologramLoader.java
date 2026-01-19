package org.oneyz.holoFX.loader;

import org.bukkit.configuration.file.YamlConfiguration;
import org.oneyz.holoFX.models.Hologram;
import org.oneyz.holoFX.utils.Logger;

import java.io.File;
import java.util.*;

/**
 * Optimized loader for hologram configuration files.
 * Handles multiple YAML files efficiently and provides validation.
 */
public class HologramLoader {

    private final File dataFolder;
    private final Map<String, Hologram> loadedHolograms;

    /**
     * Create a new HologramLoader with a data folder
     */
    public HologramLoader(File dataFolder) {
        this.dataFolder = dataFolder;
        this.loadedHolograms = new HashMap<>();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
            Logger.info("Created data folder at: " + dataFolder.getAbsolutePath());
        }
    }

    /**
     * Load all hologram configuration files from the data folder.
     * Validates for duplicate names and other issues.
     *
     * @return Map of loaded holograms (filename without extension -> Hologram)
     */
    public Map<String, Hologram> loadAllHolograms() {
        loadedHolograms.clear();
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files == null || files.length == 0) {
            Logger.warning("No hologram configuration files found in: " + dataFolder.getAbsolutePath());
            return loadedHolograms;
        }

        Logger.info("Found " + files.length + " hologram configuration file(s). Loading...");

        Set<String> processedNames = new HashSet<>();
        int successCount = 0;

        for (File file : files) {
            try {
                String fileKey = file.getName().replace(".yml", "");
                Hologram hologram = loadHologramFromFile(file, fileKey);

                if (hologram != null) {
                    if (processedNames.contains(hologram.getName())) {
                        Logger.severe("Duplicate hologram name detected: '" + hologram.getName() +
                                "' in file: " + file.getName() + ". Skipping this file.");
                        continue;
                    }

                    processedNames.add(hologram.getName());
                    loadedHolograms.put(fileKey, hologram);
                    successCount++;
                    Logger.info("Loaded hologram '" + hologram.getName() + "' from: " + file.getName());
                }
            } catch (Exception e) {
                Logger.severe("Failed to load hologram from file: " + file.getName(), e);
            }
        }

        Logger.info("Successfully loaded " + successCount + " out of " + files.length + " hologram configuration(s).");
        return loadedHolograms;
    }

    /**
     * Load a single hologram from a YAML file
     *
     * @param file The YAML file to load
     * @param fileKey The key to identify this hologram
     * @return Hologram object or null if loading failed
     */
    private Hologram loadHologramFromFile(File file, String fileKey) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!config.contains("location")) {
            throw new IllegalArgumentException("Missing 'location' section in " + file.getName());
        }

        if (!config.contains("lines")) {
            throw new IllegalArgumentException("Missing 'lines' section in " + file.getName());
        }

        String hologramName = config.getString("name", fileKey);

        Hologram.Location location = loadLocation(config.getConfigurationSection("location"));

        boolean enabled = config.getBoolean("enabled", true);

        List<Hologram.Line> lines = loadLines(config.getList("lines"));

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("No lines found in hologram: " + fileKey);
        }

        Hologram hologram = new Hologram(hologramName, enabled, location, lines);

        if (!hologram.hasValidWorld()) {
            Logger.fine("World '" + location.getWorld() + "' is not yet loaded for hologram: " + hologramName +
                    ". It will be validated when the hologram is first used.");
        }

        return hologram;
    }

    /**
     * Load location configuration
     */
    private Hologram.Location loadLocation(org.bukkit.configuration.ConfigurationSection locationSection) {
        if (locationSection == null) {
            throw new IllegalArgumentException("Location section is null");
        }

        String world = locationSection.getString("world", "world");
        double x = locationSection.getDouble("x", 0.0);
        double y = locationSection.getDouble("y", 0.0);
        double z = locationSection.getDouble("z", 0.0);
        float yaw = (float) locationSection.getDouble("yaw", 0.0);

        return Hologram.Location.builder()
                .world(world)
                .x(x)
                .y(y)
                .z(z)
                .yaw(yaw)
                .build();
    }

    /**
     * Load all lines from the lines list
     * If a line has multiple texts, each text becomes a separate TextDisplay line
     */
    @SuppressWarnings("unchecked")
    private List<Hologram.Line> loadLines(List<?> linesList) {
        List<Hologram.Line> lines = new ArrayList<>();

        if (linesList == null) {
            return lines;
        }

        int lineCounter = 0;
        for (int i = 0; i < linesList.size(); i++) {
            Object lineObj = linesList.get(i);

            if (!(lineObj instanceof Map)) {
                Logger.warning("Line " + (i + 1) + " is not a valid map structure. Skipping.");
                continue;
            }

            try {
                Map<String, Object> lineMap = (Map<String, Object>) lineObj;
                List<Hologram.Line> expandedLines = loadLineExpanded(lineMap, lineCounter + 1);
                lines.addAll(expandedLines);
                lineCounter += expandedLines.size();
            } catch (Exception e) {
                Logger.warning("Failed to load line " + (i + 1) + ": " + e.getMessage());
            }
        }

        return lines;
    }

    /**
     * Load a line and expand it if it contains multiple texts.
     * Each text becomes a separate Line with inherited offset and display settings.
     */
    @SuppressWarnings("unchecked")
    private List<Hologram.Line> loadLineExpanded(Map<String, Object> lineMap, int lineNumber) {
        List<Hologram.Line> expandedLines = new ArrayList<>();

        Object text = lineMap.get("text");
        if (text == null) {
            throw new IllegalArgumentException("Missing 'text' in line " + lineNumber);
        }
        Hologram.Offset offset = Hologram.Offset.builder()
                .x(0.0)
                .y(0.0)
                .z(0.0)
                .build();

        if (lineMap.containsKey("offset")) {
            Map<String, Object> offsetMap = (Map<String, Object>) lineMap.get("offset");
            if (offsetMap != null) {
                offset = Hologram.Offset.builder()
                        .x(getDouble(offsetMap, "x", 0.0))
                        .y(getDouble(offsetMap, "y", 0.0))
                        .z(getDouble(offsetMap, "z", 0.0))
                        .build();
            }
        }

        Hologram.DisplaySettings displaySettings = Hologram.DisplaySettings.builder()
                .textOpacity(255)
                .lineWidth(200)
                .textAlignment("CENTER")
                .defaultBackground(true)
                .seeThrough(false)
                .shadow(true)
                .build();

        if (lineMap.containsKey("display_settings")) {
            Map<String, Object> displayMap = (Map<String, Object>) lineMap.get("display_settings");
            if (displayMap != null) {
                displaySettings = loadDisplaySettings(displayMap);
            }
        }
        if (text instanceof String) {
            expandedLines.add(Hologram.Line.builder()
                    .text(text)
                    .offset(offset)
                    .displaySettings(displaySettings)
                    .build());
        } else if (text instanceof List) {
            List<String> textList = (List<String>) text;
            double baseY = offset.getY();

            for (int i = 0; i < textList.size(); i++) {
                String singleText = textList.get(i);
                double lineYOffset = baseY - (i * 0.25); // 0.25 units between lines

                Hologram.Offset expandedOffset = Hologram.Offset.builder()
                        .x(offset.getX())
                        .y(lineYOffset)
                        .z(offset.getZ())
                        .build();

                expandedLines.add(Hologram.Line.builder()
                        .text(singleText)
                        .offset(expandedOffset)
                        .displaySettings(displaySettings)
                        .build());
            }
        } else {
            throw new IllegalArgumentException("Text in line " + lineNumber + " must be a string or list of strings");
        }

        return expandedLines;
    }

    /**
     * Load display settings for a line
     */
    @SuppressWarnings("unchecked")
    private Hologram.DisplaySettings loadDisplaySettings(Map<String, Object> displayMap) {
        List<Float> translation = null;
        List<Float> rightRotation = null;
        List<Float> scale = null;
        List<Float> leftRotation = null;

        if (displayMap.containsKey("translation")) {
            translation = convertListToFloats((List<?>) displayMap.get("translation"));
        }

        if (displayMap.containsKey("right_rotation")) {
            rightRotation = convertListToFloats((List<?>) displayMap.get("right_rotation"));
        }

        if (displayMap.containsKey("scale")) {
            scale = convertListToFloats((List<?>) displayMap.get("scale"));
        }

        if (displayMap.containsKey("left_rotation")) {
            leftRotation = convertListToFloats((List<?>) displayMap.get("left_rotation"));
        }
        Hologram.Billboard billboard = Hologram.Billboard.FIXED;
        if (displayMap.containsKey("billboard")) {
            String billboardStr = getString(displayMap, "billboard", "fixed");
            billboard = Hologram.Billboard.fromString(billboardStr);
        }

        return Hologram.DisplaySettings.builder()
                .textOpacity(getInt(displayMap, "text_opacity", 255))
                .lineWidth(getInt(displayMap, "line_width", 200))
                .textAlignment(getString(displayMap, "text_alignment", "CENTER"))
                .background(getString(displayMap, "background", null))
                .defaultBackground(getBoolean(displayMap, "default_background", true))
                .seeThrough(getBoolean(displayMap, "see_through", false))
                .shadow(getBoolean(displayMap, "shadow", true))
                .billboard(billboard)
                .permission(getString(displayMap, "permission", null))
                .brightness(loadBrightness(displayMap))
                .shadowRadius(displayMap.containsKey("shadow_radius") ? getDouble(displayMap, "shadow_radius", 0.0) : null)
                .viewRange(displayMap.containsKey("view_range") ? getFloat(displayMap, "view_range", 1.0f) : null)
                .translation(translation)
                .rightRotationQuaternion(rightRotation)
                .scale(scale)
                .leftRotationQuaternion(leftRotation)
                .build();
    }

    /**
     * Convert list to float list
     */
    @SuppressWarnings("unchecked")
    private List<Float> convertListToFloats(List<?> list) {
        if (list == null) {
            return null;
        }
        List<Float> floats = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof Number) {
                floats.add(((Number) obj).floatValue());
            }
        }
        return floats.isEmpty() ? null : floats;
    }

    /**
     * Utility method to safely get integer from map
     */
    private int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    /**
     * Utility method to safely get double from map
     */
    private double getDouble(Map<String, Object> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    /**
     * Load brightness from YAML format
     * Supports two formats:
     * 1. brightness: 255 (direct integer value where (sky << 4) | block)
     * 2. brightness:
     *      sky: 15
     *      block: 15
     * Returns combined value (sky << 4) | block, or null if not specified
     */
    @SuppressWarnings("unchecked")
    private Integer loadBrightness(Map<String, Object> displayMap) {
        if (!displayMap.containsKey("brightness")) {
            return null;
        }

        Object brightnessObj = displayMap.get("brightness");
        if (brightnessObj instanceof Number) {
            return ((Number) brightnessObj).intValue();
        }
        if (brightnessObj instanceof Map) {
            Map<String, Object> brightnessMap = (Map<String, Object>) brightnessObj;
            int sky = getInt(brightnessMap, "sky", 0);
            int block = getInt(brightnessMap, "block", 0);
            if (sky < 0 || sky > 15 || block < 0 || block > 15) {
                Logger.warning("Brightness values out of range (0-15). Using defaults.");
                return null;
            }

            return (sky << 4) | block;
        }

        return null;
    }


    /**
     * Utility method to safely get float from map
     */
    private float getFloat(Map<String, Object> map, String key, float defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return defaultValue;
    }

    /**
     * Utility method to safely get string from map
     */
    private String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }

    /**
     * Utility method to safely get boolean from map
     */
    private boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    /**
     * Get a loaded hologram by its file key
     */
    public Hologram getHologram(String fileKey) {
        return loadedHolograms.get(fileKey);
    }

    /**
     * Get all loaded holograms
     */
    public Map<String, Hologram> getAllHolograms() {
        return new HashMap<>(loadedHolograms);
    }

    /**
     * Check if a hologram with the given file key is loaded
     */
    public boolean isLoaded(String fileKey) {
        return loadedHolograms.containsKey(fileKey);
    }

    /**
     * Get count of loaded holograms
     */
    public int getLoadedCount() {
        return loadedHolograms.size();
    }

    /**
     * Reload all holograms
     */
    public void reloadAll() {
        Logger.info("Reloading all holograms...");
        loadAllHolograms();
    }

    /**
     * Validate that all loaded holograms have their required worlds available.
     * Call this after all worlds have loaded (e.g., in onEnable after a server tick delay)
     */
    public void validateAllWorlds() {
        int validCount = 0;
        int invalidCount = 0;

        for (Map.Entry<String, Hologram> entry : loadedHolograms.entrySet()) {
            Hologram hologram = entry.getValue();
            if (hologram.hasValidWorld()) {
                validCount++;
            } else {
                invalidCount++;
                Logger.warning("Hologram '" + hologram.getName() + "' requires world '" +
                        hologram.getLocation().getWorld() + "' which does not exist!");
            }
        }

        if (invalidCount > 0) {
            Logger.warning("§c" + invalidCount + " hologram(s) have invalid worlds. " + validCount + " are valid.");
        } else {
            Logger.info("§aAll " + validCount + " hologram(s) have valid worlds!");
        }
    }

    /**
     * Utility method to properly serialize display settings for YAML
     * Converts Billboard enum to string representation
     * Only serializes non-default values to keep YAML clean
     */
    public static Map<String, Object> serializeDisplaySettings(Hologram.DisplaySettings settings) {
        Map<String, Object> displaySettings = new LinkedHashMap<>();
        if (settings.getTextOpacity() != Hologram.DisplaySettings.DEFAULT_TEXT_OPACITY) {
            displaySettings.put("text_opacity", settings.getTextOpacity());
        }
        if (settings.getLineWidth() != Hologram.DisplaySettings.DEFAULT_LINE_WIDTH) {
            displaySettings.put("line_width", settings.getLineWidth());
        }
        if (!settings.getTextAlignment().equals(Hologram.DisplaySettings.DEFAULT_TEXT_ALIGNMENT)) {
            displaySettings.put("text_alignment", settings.getTextAlignment());
        }
        if (settings.isDefaultBackground() != Hologram.DisplaySettings.DEFAULT_BACKGROUND) {
            displaySettings.put("default_background", settings.isDefaultBackground());
        }
        if (settings.isSeeThrough() != Hologram.DisplaySettings.DEFAULT_SEE_THROUGH) {
            displaySettings.put("see_through", settings.isSeeThrough());
        }
        if (settings.isShadow() != Hologram.DisplaySettings.DEFAULT_SHADOW) {
            displaySettings.put("shadow", settings.isShadow());
        }
        if (settings.getBillboard() != Hologram.DisplaySettings.DEFAULT_BILLBOARD) {
            displaySettings.put("billboard", settings.getBillboard().toString());
        }
        if (settings.getBackground() != null) {
            displaySettings.put("background", settings.getBackground());
        }

        if (settings.getPermission() != null) {
            displaySettings.put("permission", settings.getPermission());
        }

        if (settings.getBrightness() != null) {
            int brightness = settings.getBrightness();
            int sky = (brightness >> 4) & 0x0F;
            int block = brightness & 0x0F;

            Map<String, Object> brightnessMap = new LinkedHashMap<>();
            brightnessMap.put("sky", sky);
            brightnessMap.put("block", block);
            displaySettings.put("brightness", brightnessMap);
        }

        if (settings.getShadowRadius() != null) {
            displaySettings.put("shadow_radius", settings.getShadowRadius());
        }

        if (settings.getViewRange() != null) {
            displaySettings.put("view_range", settings.getViewRange());
        }
        if (settings.getTranslation() != null) {
            displaySettings.put("translation", settings.getTranslation());
        }

        if (settings.getRightRotationQuaternion() != null) {
            displaySettings.put("right_rotation", settings.getRightRotationQuaternion());
        }

        if (settings.getScale() != null) {
            displaySettings.put("scale", settings.getScale());
        }

        if (settings.getLeftRotationQuaternion() != null) {
            displaySettings.put("left_rotation", settings.getLeftRotationQuaternion());
        }

        return displaySettings;
    }

    /**
     * Load hologram from file and update selected lines, preserving all original data
     * This is safer than using in-memory config as it ensures all fields are preserved
     */
    public boolean saveUpdatedHologramToFile(String hologramName, List<Hologram.Line> updatedLines) {
        try {
            File dataFolder = this.dataFolder;
            File hologramFile = new File(dataFolder, hologramName + ".yml");

            if (!hologramFile.exists()) {
                Logger.warning("Hologram file not found: " + hologramFile.getName());
                return false;
            }
            YamlConfiguration config = YamlConfiguration.loadConfiguration(hologramFile);
            List<Map<String, Object>> linesList = new ArrayList<>();
            for (Hologram.Line line : updatedLines) {
                Map<String, Object> lineMap = new LinkedHashMap<>();

                lineMap.put("text", line.getText());

                Map<String, Object> offset = new LinkedHashMap<>();
                offset.put("x", line.getOffset().getX());
                offset.put("y", line.getOffset().getY());
                offset.put("z", line.getOffset().getZ());
                lineMap.put("offset", offset);
                Map<String, Object> displaySettings = serializeDisplaySettings(line.getDisplaySettings());

                lineMap.put("display_settings", displaySettings);
                linesList.add(lineMap);
            }
            config.set("lines", linesList);
            config.save(hologramFile);
            return true;

        } catch (Exception e) {
            Logger.severe("Error saving hologram file: " + hologramName, e);
            return false;
        }
    }

    /**
     * Save hologram location to file
     */
    public boolean saveHologramLocation(String hologramName, Hologram.Location newLocation) {
        try {
            File dataFolder = this.dataFolder;
            File hologramFile = new File(dataFolder, hologramName + ".yml");

            if (!hologramFile.exists()) {
                Logger.warning("Hologram file not found: " + hologramFile.getName());
                return false;
            }
            YamlConfiguration config = YamlConfiguration.loadConfiguration(hologramFile);
            Map<String, Object> locationMap = new LinkedHashMap<>();
            locationMap.put("world", newLocation.getWorld());
            locationMap.put("x", newLocation.getX());
            locationMap.put("y", newLocation.getY());
            locationMap.put("z", newLocation.getZ());
            locationMap.put("yaw", newLocation.getYaw());

            config.set("location", locationMap);
            config.save(hologramFile);
            Hologram loadedHologram = loadedHolograms.values().stream()
                    .filter(h -> h.getName().equalsIgnoreCase(hologramName))
                    .findFirst()
                    .orElse(null);

            if (loadedHologram != null) {
                loadedHologram.getLocation().setX(newLocation.getX());
                loadedHologram.getLocation().setY(newLocation.getY());
                loadedHologram.getLocation().setZ(newLocation.getZ());
                loadedHologram.getLocation().setYaw(newLocation.getYaw());
            }

            return true;

        } catch (Exception e) {
            Logger.severe("Error saving hologram location: " + hologramName, e);
            return false;
        }
    }

}


