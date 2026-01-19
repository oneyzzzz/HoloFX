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
        @TabCompleteConfig(position = 1, type = TabCompleteType.STATIC, suggestions = {"ffff0000", "ff0000ff", "ff00ff00", "ffffff00", "ffffffff", "ff000000", "or custom"})
})
public class SetBackgroundEditCommand implements EditSubCommand {

    private final HoloFX plugin;

    public SetBackgroundEditCommand(HoloFX plugin) {
        this.plugin = plugin;
    }

    @Override
    public MessageManager getMessageManager() {
        return plugin.getMessageManager();
    }

    @Override
    public boolean execute(CommandSender sender, String hologramName, String[] args) {
        if (args.length < 1) {
            getMessageManager().sendMessage(sender, "edit.setbackground.usage");
            getMessageManager().sendMessage(sender, "edit.setbackground.info");
            return true;
        }

        String backgroundColor;
        Integer lineNumber = null;
        String colorArg;

        String firstArg = args[0];
        try {
            int possibleLineNum = Integer.parseInt(firstArg);
            if (args.length >= 2) {
                lineNumber = possibleLineNum;
                colorArg = args[1];
            } else {
                colorArg = firstArg;
            }
        } catch (NumberFormatException e) {
            colorArg = firstArg;
        }

        backgroundColor = parseBackgroundColor(colorArg);
        if (backgroundColor == null) {
            getMessageManager().sendMessage(sender, "edit.setbackground.invalid_color");
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
                    getMessageManager().sendMessage(sender, "edit.line_number_out_of_range", Map.of("line", String.valueOf(lineNumber), "max_lines", String.valueOf(currentLines.size())));
                    return true;
                }

                Hologram.Line line = currentLines.get(lineNumber - 1);
                Hologram.DisplaySettings oldSettings = line.getDisplaySettings();

                Hologram.DisplaySettings updatedSettings = copyDisplaySettingsBuilder(oldSettings)
                        .background(backgroundColor)
                        .defaultBackground(false)
                        .build();

                Hologram.Line updatedLine = Hologram.Line.builder()
                        .text(line.getText())
                        .offset(line.getOffset())
                        .displaySettings(updatedSettings)
                        .build();

                currentLines.set(lineNumber - 1, updatedLine);

                getMessageManager().sendMessage(sender, "edit.setbackground.set_line", Map.of(
                        "value", backgroundColor,
                        "line", String.valueOf(lineNumber),
                        "name", hologramName
                ));
            } else {
                for (int i = 0; i < currentLines.size(); i++) {
                    Hologram.Line line = currentLines.get(i);
                    Hologram.DisplaySettings oldSettings = line.getDisplaySettings();

                    Hologram.DisplaySettings updatedSettings = copyDisplaySettingsBuilder(oldSettings)
                            .background(backgroundColor)
                            .defaultBackground(false)
                            .build();

                    Hologram.Line updatedLine = Hologram.Line.builder()
                            .text(line.getText())
                            .offset(line.getOffset())
                            .displaySettings(updatedSettings)
                            .build();

                    currentLines.set(i, updatedLine);
                }

                getMessageManager().sendMessage(sender, "edit.setbackground.set", Map.of(
                        "value", backgroundColor,
                        "name", hologramName
                ));
            }

            hologramModel.setLines(currentLines);

            plugin.getHologramDisplayManager().despawnHologram(hologramName);
            if (!plugin.getHologramDisplayManager().spawnHologram(hologramModel)) {
                getMessageManager().sendMessage(sender, "edit.setbackground.failed", Map.of("error", getMessageManager().getGeneralMessage("errors.hologram.spawn_failed", Map.of("world", hologramModel.getWorld().getName(), "name", hologramName))));
                return true;
            }

            HologramLoader loader = plugin.getHologramLoader();
            if (!loader.saveUpdatedHologramToFile(hologramName, currentLines)) {
                getMessageManager().sendMessage(sender, "edit.setbackground.failed", Map.of("error", getMessageManager().getGeneralMessage("errors.files.save_failed", Map.of("name", hologramName))));
                return true;
            }

            return true;

        } catch (Exception e) {
            getMessageManager().sendMessage(sender, "edit.setbackground.failed", Map.of("error", e.getMessage()));
            Logger.severe("Error setting background for hologram: " + hologramName, e);
            return true;
        }
    }


    private String parseBackgroundColor(String colorArg) {
        try {
            String cleaned = colorArg.trim().toLowerCase();
            if (cleaned.startsWith("0x")) {
                cleaned = cleaned.substring(2);
            }

            if (cleaned.matches("[0-9a-f]{8}")) {
                return "0x" + cleaned;
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Hologram.DisplaySettings.DisplaySettingsBuilder copyDisplaySettingsBuilder(Hologram.DisplaySettings settings) {
        return Hologram.DisplaySettings.builder()
                .textOpacity(settings.getTextOpacity())
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
                .viewRange(settings.getViewRange())
                .translation(settings.getTranslation())
                .rightRotationQuaternion(settings.getRightRotationQuaternion())
                .scale(settings.getScale())
                .leftRotationQuaternion(settings.getLeftRotationQuaternion());
    }

}

