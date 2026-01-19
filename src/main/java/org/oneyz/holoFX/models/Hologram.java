package org.oneyz.holoFX.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class Hologram {

    /**
     * Billboard enum for display orientation
     */
    public enum Billboard {
        FIXED("fixed"),
        VERTICAL("vertical"),
        HORIZONTAL("horizontal"),
        CENTER("center");

        private final String name;

        Billboard(String name) {
            this.name = name;
        }

        public static Billboard fromString(String value) {
            if (value == null) {
                return FIXED;
            }
            try {
                return Billboard.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return FIXED;
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private String name;
    private boolean enabled;
    private Location location;
    private List<Line> lines;

    /**
     * Get the World object from the location
     */
    public World getWorld() {
        return Bukkit.getWorld(location.getWorld());
    }

    /**
     * Check if the hologram has a valid world
     */
    public boolean hasValidWorld() {
        return getWorld() != null;
    }

    /**
     * Location model for hologram
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class Location {
        private String world;
        private double x;
        private double y;
        private double z;
        private float yaw;

        /**
         * Convert to Bukkit Location
         */
        public org.bukkit.Location toBukkitLocation() {
            org.bukkit.Location loc = new org.bukkit.Location(Bukkit.getWorld(world), x, y, z);
            loc.setYaw(yaw);
            return loc;
        }

        /**
         * Get as Vector
         */
        public Vector toVector() {
            return new Vector(x, y, z);
        }
    }

    /**
     * Line model for hologram text display
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class Line {

        /**
         * Text content - can be a single string or list of strings
         */
        private Object text; // String or List<String>

        private Offset offset;
        private DisplaySettings displaySettings;

        /**
         * Get text as a single string (if it's a list, joins with newline)
         */
        public String getTextAsString() {
            if (text instanceof String) {
                return (String) text;
            } else if (text instanceof List) {
                return String.join("\n", (List<String>) text);
            }
            return "";
        }

        /**
         * Get text as a list
         */
        @SuppressWarnings("unchecked")
        public List<String> getTextAsLines() {
            if (text instanceof List) {
                return (List<String>) text;
            } else if (text instanceof String) {
                return List.of((String) text);
            }
            return List.of();
        }

        /**
         * Get text as single line (first line if multiple)
         */
        public String getFirstLine() {
            if (text instanceof String) {
                return (String) text;
            } else if (text instanceof List) {
                List<?> list = (List<?>) text;
                if (!list.isEmpty()) {
                    return list.get(0).toString();
                }
            }
            return "";
        }
    }

    /**
     * Offset model for per-line positioning
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class Offset {
        private double x;
        private double y;
        private double z;

        /**
         * Convert to Vector
         */
        public Vector toVector() {
            return new Vector(x, y, z);
        }

        /**
         * Get offset as Bukkit Location relative to base location
         */
        public org.bukkit.Location toRelativeLocation(org.bukkit.Location baseLocation) {
            return baseLocation.clone().add(x, y, z);
        }
    }

    /**
     * Display settings for TEXT_DISPLAY entity (Minecraft 1.20.2+)
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class DisplaySettings {
        public static final int DEFAULT_TEXT_OPACITY = 255;
        public static final int DEFAULT_LINE_WIDTH = 200;
        public static final String DEFAULT_TEXT_ALIGNMENT = "CENTER";
        public static final boolean DEFAULT_BACKGROUND = true;
        public static final boolean DEFAULT_SEE_THROUGH = false;
        public static final boolean DEFAULT_SHADOW = true;
        public static final Billboard DEFAULT_BILLBOARD = Billboard.FIXED;

        private int textOpacity; // 0-255, default 255 (text opacity)
        private int lineWidth; // default 200
        private String textAlignment; // LEFT, CENTER, RIGHT, default CENTER
        private String background; // RGB hex color like 0x4000FF00, optional
        private boolean defaultBackground; // if true, ignores custom background
        private boolean seeThrough; // default false
        private boolean shadow; // default true
        private Billboard billboard; // default FIXED - display orientation
        private String permission; // optional permission requirement
        private Integer brightness; // optional brightness setting
        private Double shadowRadius; // optional shadow radius
        private Double shadowStrength; // optional shadow strength (opacity of shadow as function of distance to block below)
        private Float viewRange; // optional view range
        private List<Float> translation; // [x, y, z]
        private List<Float> rightRotationQuaternion; // [x, y, z, w]
        private List<Float> scale; // [x, y, z]
        private List<Float> leftRotationQuaternion; // [x, y, z, w]

        /**
         * Get background color as integer (ARGB)
         */
        public int getBackgroundColor() {
            if (background != null && !background.isEmpty()) {
                try {
                    if (background.startsWith("0x") || background.startsWith("0X")) {
                        return (int) Long.parseLong(background.substring(2), 16);
                    } else {
                        return (int) Long.parseLong(background, 16);
                    }
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            return 0;
        }

        /**
         * Check if this line requires permission
         */
        public boolean hasPermissionRequirement() {
            return permission != null && !permission.isEmpty();
        }
    }

}

