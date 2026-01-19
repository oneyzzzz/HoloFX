package org.oneyz.holoFX.commands.subcommands.edit;

import org.bukkit.command.CommandSender;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.enums.TabCompleteType;
import org.oneyz.holoFX.holograms.displays.HologramDisplay;
import org.oneyz.holoFX.interfaces.commands.EditSubCommand;
import org.oneyz.holoFX.interfaces.tabcomplete.TabCompleteConfig;
import org.oneyz.holoFX.interfaces.tabcomplete.TabCompleteEdit;
import org.oneyz.holoFX.loader.HologramLoader;
import org.oneyz.holoFX.models.Hologram;
import org.oneyz.holoFX.utils.Logger;
import org.oneyz.holoFX.utils.MessageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@TabCompleteEdit({
        @TabCompleteConfig(position = 0, type = TabCompleteType.LINE_NUMBERS),
        @TabCompleteConfig(position = 1, type = TabCompleteType.STATIC, suggestions = {"[1.0,0.0,0.0,0.0]", "[0.0,1.0,0.0,0.0]", "[0.0,0.0,1.0,0.0]", "[0.0,0.0,0.0,1.0]"})
})
public class SetRightRotationEditCommand implements EditSubCommand {

    private final HoloFX plugin;

    public SetRightRotationEditCommand(HoloFX plugin) {
        this.plugin = plugin;
    }

    @Override
    public MessageManager getMessageManager() {
        return plugin.getMessageManager();
    }

    @Override
    public boolean execute(CommandSender sender, String hologramName, String[] args) {
        if (args.length < 1) {
            getMessageManager().sendMessage(sender, "edit.setrightrotation.usage");
            getMessageManager().sendMessage(sender, "edit.setrightrotation.example");
            return true;
        }

        Integer lineNumber = null;
        String rotationArg;

        if (args.length >= 2) {
            try {
                lineNumber = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                getMessageManager().sendMessage(sender, "edit.line_must_be_number");
                return true;
            }
            rotationArg = args[1];
        } else {
            rotationArg = args[0];
        }


        List<Float> rotationValues = parseQuaternion(rotationArg);
        if (rotationValues == null || rotationValues.size() != 4) {
            getMessageManager().sendMessage(sender, "edit.setrightrotation.invalid_format");
            getMessageManager().sendMessage(sender, "edit.setrightrotation.example");
            return true;
        }

        HologramDisplay display = plugin.getHologramDisplayManager().getHologram(hologramName);
        if (display == null) {
            getMessageManager().sendMessage(sender, "edit.not_active", Map.of("name", hologramName));
            return true;
        }

        try {
            Hologram hologramModel = display.getHologramConfig();
            List<Hologram.Line> currentLines = new ArrayList<>(hologramModel.getLines());

            if (lineNumber != null) {
                if (lineNumber < 1 || lineNumber > currentLines.size()) {
                    getMessageManager().sendMessage(sender, "edit.line_number_out_of_range", Map.of(
                            "line", String.valueOf(lineNumber),
                            "max_lines", String.valueOf(currentLines.size())
                    ));
                    return true;
                }

                updateLineRightRotation(currentLines, lineNumber - 1, rotationValues);

                hologramModel.setLines(currentLines);

                plugin.getHologramDisplayManager().despawnHologram(hologramName);
                if (!plugin.getHologramDisplayManager().spawnHologram(hologramModel)) {
                    getMessageManager().sendMessage(sender, "edit.setrightrotation.failed", Map.of("error", getMessageManager().getGeneralMessage("errors.hologram.spawn_failed", Map.of("world", hologramModel.getWorld().getName(), "name", hologramName))));
                    return true;
                }

                HologramLoader loader = plugin.getHologramLoader();
                if (!loader.saveUpdatedHologramToFile(hologramName, currentLines)) {
                    getMessageManager().sendMessage(sender, "edit.setrightrotation.failed", Map.of("error", getMessageManager().getGeneralMessage("errors.files.save_failed", Map.of("name", hologramName))));
                    return true;
                }

                getMessageManager().sendMessage(sender, "edit.setrightrotation.set_line", Map.of(
                        "name", hologramName,
                        "line", String.valueOf(lineNumber),
                        "x", rotationValues.get(0).toString(),
                        "y", rotationValues.get(1).toString(),
                        "z", rotationValues.get(2).toString(),
                        "w", rotationValues.get(3).toString()
                ));

            } else {
                for (int i = 0; i < currentLines.size(); i++) {
                    updateLineRightRotation(currentLines, i, rotationValues);
                }

                hologramModel.setLines(currentLines);

                plugin.getHologramDisplayManager().despawnHologram(hologramName);
                if (!plugin.getHologramDisplayManager().spawnHologram(hologramModel)) {
                    getMessageManager().sendMessage(sender, "edit.setrightrotation.failed", Map.of("error", getMessageManager().getGeneralMessage("errors.hologram.spawn_failed", Map.of("world", hologramModel.getWorld().getName(), "name", hologramName))));
                    return true;
                }

                HologramLoader loader = plugin.getHologramLoader();
                if (!loader.saveUpdatedHologramToFile(hologramName, currentLines)) {
                    getMessageManager().sendMessage(sender, "edit.setrightrotation.failed", Map.of("error", getMessageManager().getGeneralMessage("errors.files.save_failed", Map.of("name", hologramName))));
                    return true;
                }

                getMessageManager().sendMessage(sender, "edit.setrightrotation.set", Map.of(
                        "name", hologramName,
                        "x", rotationValues.get(0).toString(),
                        "y", rotationValues.get(1).toString(),
                        "z", rotationValues.get(2).toString(),
                        "w", rotationValues.get(3).toString()
                ));
            }

            return true;

        } catch (Exception e) {
            getMessageManager().sendMessage(sender, "edit.setrightrotation.failed", Map.of("error", e.getMessage()));
            Logger.severe("Error setting right rotation for hologram: " + hologramName, e);
            return true;
        }
    }

    private void updateLineRightRotation(List<Hologram.Line> lines, int index, List<Float> rotationValues) {
        Hologram.Line line = lines.get(index);
        Hologram.DisplaySettings oldSettings = line.getDisplaySettings();

        Hologram.DisplaySettings.DisplaySettingsBuilder builder = copyDisplaySettingsBuilder(oldSettings);

        builder.rightRotationQuaternion(rotationValues);

        Hologram.DisplaySettings updatedSettings = builder.build();

        Hologram.Line updatedLine = Hologram.Line.builder()
                .text(line.getText())
                .offset(line.getOffset())
                .displaySettings(updatedSettings)
                .build();

        lines.set(index, updatedLine);
    }

    private List<Float> parseQuaternion(String input) {
        try {
            if (!input.startsWith("[") || !input.endsWith("]")) {
                return null;
            }

            String content = input.substring(1, input.length() - 1);
            String[] parts = content.split(",");

            if (parts.length != 4) {
                return null;
            }

            List<Float> values = new ArrayList<>();
            for (String part : parts) {
                values.add(Float.parseFloat(part.trim()));
            }

            return values;
        } catch (Exception e) {
            return null;
        }
    }


    private Hologram.DisplaySettings.DisplaySettingsBuilder copyDisplaySettingsBuilder(Hologram.DisplaySettings settings) {
        Hologram.DisplaySettings.DisplaySettingsBuilder b = Hologram.DisplaySettings.builder();
        if (settings == null) {
            return b;
        }

        b.textOpacity(settings.getTextOpacity())
                .lineWidth(settings.getLineWidth())
                .textAlignment(settings.getTextAlignment())
                .background(settings.getBackground())
                .defaultBackground(settings.isDefaultBackground())
                .seeThrough(settings.isSeeThrough())
                .shadow(settings.isShadow())
                .billboard(settings.getBillboard())
                .permission(settings.getPermission())
                .brightness(settings.getBrightness())
                .shadowRadius(settings.getShadowRadius())
                .shadowStrength(settings.getShadowStrength())
                .viewRange(settings.getViewRange())
                .translation(settings.getTranslation())
                .rightRotationQuaternion(settings.getRightRotationQuaternion())
                .scale(settings.getScale())
                .leftRotationQuaternion(settings.getLeftRotationQuaternion());

        return b;
    }

}

