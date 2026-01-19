package org.oneyz.holoFX.holograms.displays;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.oneyz.holoFX.models.Hologram;
import org.oneyz.holoFX.utils.Logger;

import java.util.Objects;

/**
 * Manager for creating and configuring TEXT_DISPLAY entities
 */
public class TextDisplayManager {

    /**
     * Create a TEXT_DISPLAY entity from a hologram configuration
     *
     * @param location The base location for the hologram
     * @param line The line configuration
     * @return The created TextDisplay entity, or null if creation failed
     */
    public static TextDisplay createTextDisplay(Location location, Hologram.Line line) {
        if (location == null || line == null) {
            Logger.warning("Cannot create TextDisplay with null location or line");
            return null;
        }

        try {
            Location displayLocation = location.clone().add(
                    line.getOffset().getX(),
                    line.getOffset().getY(),
                    line.getOffset().getZ()
            );
            TextDisplay textDisplay = (TextDisplay) Objects.requireNonNull(location.getWorld())
                    .spawnEntity(displayLocation, org.bukkit.entity.EntityType.TEXT_DISPLAY);
            applyText(textDisplay, line);
            applyDisplaySettings(textDisplay, line.getDisplaySettings());

            return textDisplay;

        } catch (Exception e) {
            Logger.severe("Failed to create TextDisplay entity", e);
            return null;
        }
    }

    /**
     * Apply text to a TextDisplay entity
     */
    private static void applyText(TextDisplay textDisplay, Hologram.Line line) {
        String text = line.getTextAsString();
        String processedText = processColorCodes(text);

        textDisplay.setText(processedText);
    }

    /**
     * Process Minecraft color codes (&c format) to chat color
     */
    private static String processColorCodes(String text) {
        String processed = text.replace('&', '§');
        return processed;
    }

    /**
     * Apply display settings to a TextDisplay entity
     */
    private static void applyDisplaySettings(TextDisplay textDisplay, Hologram.DisplaySettings settings) {
        if (settings == null) {
            return;
        }
        textDisplay.setTextOpacity((byte) settings.getTextOpacity());
        textDisplay.setLineWidth(settings.getLineWidth());
        try {
            TextDisplay.TextAlignment alignment = TextDisplay.TextAlignment.valueOf(
                    settings.getTextAlignment().toUpperCase()
            );
            textDisplay.setAlignment(alignment);
        } catch (IllegalArgumentException e) {
            Logger.warning("Invalid text alignment: " + settings.getTextAlignment());
            textDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);
        }
        if (settings.getBillboard() != null) {
            Display.Billboard displayBillboard = mapBillboard(settings.getBillboard());
            textDisplay.setBillboard(displayBillboard);
        }
        if (!settings.isDefaultBackground()) {
            int bgColorInt = settings.getBackgroundColor();
            Color bgColor = Color.fromARGB(bgColorInt);
            textDisplay.setBackgroundColor(bgColor);
        } else {
            textDisplay.setDefaultBackground(true);
        }
        textDisplay.setSeeThrough(settings.isSeeThrough());
        textDisplay.setShadowed(settings.isShadow());
        if (settings.getBrightness() != null) {
            int skyBright = (settings.getBrightness() >> 4) & 0x0F;
            int blockBright = settings.getBrightness() & 0x0F;

            Display.Brightness brightness = new Display.Brightness(skyBright, blockBright);
            textDisplay.setBrightness(brightness);
        }
        if (settings.getShadowRadius() != null) {
            textDisplay.setShadowRadius(settings.getShadowRadius().floatValue());
        }
        if (settings.getViewRange() != null) {
            textDisplay.setViewRange(settings.getViewRange());
        }
        applyTransformation(textDisplay, settings);
    }

    /**
     * Apply transformation (scale, rotation, translation) to TextDisplay
     */
    private static void applyTransformation(TextDisplay textDisplay, Hologram.DisplaySettings settings) {
        org.bukkit.util.Transformation transformation = textDisplay.getTransformation();
        if (settings.getTranslation() != null && settings.getTranslation().size() >= 3) {
            org.joml.Vector3f translation = new org.joml.Vector3f(
                    settings.getTranslation().get(0),
                    settings.getTranslation().get(1),
                    settings.getTranslation().get(2)
            );
            transformation = new org.bukkit.util.Transformation(
                    translation,
                    transformation.getLeftRotation(),
                    transformation.getScale(),
                    transformation.getRightRotation()
            );
        }
        if (settings.getRightRotationQuaternion() != null && settings.getRightRotationQuaternion().size() >= 4) {
            org.joml.Quaternionf rightRotation = new org.joml.Quaternionf(
                    settings.getRightRotationQuaternion().get(0),
                    settings.getRightRotationQuaternion().get(1),
                    settings.getRightRotationQuaternion().get(2),
                    settings.getRightRotationQuaternion().get(3)
            );
            transformation = new org.bukkit.util.Transformation(
                    transformation.getTranslation(),
                    transformation.getLeftRotation(),
                    transformation.getScale(),
                    rightRotation
            );
        }
        if (settings.getScale() != null && settings.getScale().size() >= 3) {
            org.joml.Vector3f scale = new org.joml.Vector3f(
                    settings.getScale().get(0),
                    settings.getScale().get(1),
                    settings.getScale().get(2)
            );
            transformation = new org.bukkit.util.Transformation(
                    transformation.getTranslation(),
                    transformation.getLeftRotation(),
                    scale,
                    transformation.getRightRotation()
            );
        }
        if (settings.getLeftRotationQuaternion() != null && settings.getLeftRotationQuaternion().size() >= 4) {
            org.joml.Quaternionf leftRotation = new org.joml.Quaternionf(
                    settings.getLeftRotationQuaternion().get(0),
                    settings.getLeftRotationQuaternion().get(1),
                    settings.getLeftRotationQuaternion().get(2),
                    settings.getLeftRotationQuaternion().get(3)
            );
            transformation = new org.bukkit.util.Transformation(
                    transformation.getTranslation(),
                    leftRotation,
                    transformation.getScale(),
                    transformation.getRightRotation()
            );
        }
        textDisplay.setTransformation(transformation);
    }

    /**
     * Update an existing TextDisplay with new configuration
     */
    public static void updateTextDisplay(TextDisplay textDisplay, Hologram.Line line) {
        if (textDisplay == null || line == null) {
            return;
        }

        try {
            applyText(textDisplay, line);
            applyDisplaySettings(textDisplay, line.getDisplaySettings());
        } catch (Exception e) {
            Logger.severe("Failed to update TextDisplay entity", e);
        }
    }

    /**
     * Remove a TextDisplay entity
     */
    public static void removeTextDisplay(TextDisplay textDisplay) {
        if (textDisplay != null && textDisplay.isValid()) {
            textDisplay.remove();
        }
    }

    /**
     * Map Hologram.Billboard enum to Display.Billboard
     */
    private static Display.Billboard mapBillboard(Hologram.Billboard billboard) {
        if (billboard == null) {
            return Display.Billboard.FIXED;
        }

        return switch (billboard) {
            case FIXED -> Display.Billboard.FIXED;
            case VERTICAL -> Display.Billboard.VERTICAL;
            case HORIZONTAL -> Display.Billboard.HORIZONTAL;
            case CENTER -> Display.Billboard.CENTER;
        };
    }

}

