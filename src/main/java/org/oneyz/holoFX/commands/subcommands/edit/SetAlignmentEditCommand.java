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
        @TabCompleteConfig(position = 1, type = TabCompleteType.STATIC, suggestions = {"LEFT", "CENTER", "RIGHT"})
})
public class SetAlignmentEditCommand implements EditSubCommand {

    private final HoloFX plugin;

    public SetAlignmentEditCommand(HoloFX plugin) {
        this.plugin = plugin;
    }

    @Override
    public MessageManager getMessageManager() {
        return plugin.getMessageManager();
    }

    @Override
    public boolean execute(CommandSender sender, String hologramName, String[] args) {
        if (args.length < 1) {
            getMessageManager().sendMessage(sender, "edit.setalignment.usage");
            return true;
        }

        String alignmentStr;
        Integer lineNumber = null;

        String firstArg = args[0];
        try {
            int possibleLineNum = Integer.parseInt(firstArg);
            if (args.length >= 2) {
                lineNumber = possibleLineNum;
                alignmentStr = args[1].toUpperCase();
            } else {
                alignmentStr = firstArg.toUpperCase();
            }
        } catch (NumberFormatException e) {
            alignmentStr = firstArg.toUpperCase();
        }

        if (!alignmentStr.equals("CENTER") && !alignmentStr.equals("LEFT") && !alignmentStr.equals("RIGHT")) {
            getMessageManager().sendMessage(sender, "edit.setalignment.info");
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
                        .textAlignment(alignmentStr)
                        .build();

                Hologram.Line updatedLine = Hologram.Line.builder()
                        .text(line.getText())
                        .offset(line.getOffset())
                        .displaySettings(updatedSettings)
                        .build();

                currentLines.set(lineNumber - 1, updatedLine);

                getMessageManager().sendMessage(sender, "edit.setalignment.set_line", Map.of(
                        "value", alignmentStr,
                        "line", String.valueOf(lineNumber),
                        "name", hologramName
                ));
            } else {
                for (int i = 0; i < currentLines.size(); i++) {
                    Hologram.Line line = currentLines.get(i);
                    Hologram.DisplaySettings oldSettings = line.getDisplaySettings();

                    Hologram.DisplaySettings updatedSettings = copyDisplaySettingsBuilder(oldSettings)
                            .textAlignment(alignmentStr)
                            .build();

                    Hologram.Line updatedLine = Hologram.Line.builder()
                            .text(line.getText())
                            .offset(line.getOffset())
                            .displaySettings(updatedSettings)
                            .build();

                    currentLines.set(i, updatedLine);
                }

                getMessageManager().sendMessage(sender, "edit.setalignment.set", Map.of(
                        "value", alignmentStr,
                        "name", hologramName
                ));
            }

            hologramModel.setLines(currentLines);

            plugin.getHologramDisplayManager().despawnHologram(hologramName);
            if (!plugin.getHologramDisplayManager().spawnHologram(hologramModel)) {
                getMessageManager().sendMessage(sender, "edit.setalignment.failed", Map.of("error", getMessageManager().getGeneralMessage("errors.hologram.spawn_failed", Map.of("world", hologramModel.getWorld().getName(), "name", hologramName))));
                return false;
            }

            HologramLoader loader = plugin.getHologramLoader();
            if (!loader.saveUpdatedHologramToFile(hologramName, currentLines)) {
                getMessageManager().sendMessage(sender, "edit.setalignment.failed", Map.of("error", getMessageManager().getGeneralMessage("errors.files.save_failed", Map.of("name", hologramName))));
                return false;
            }

            return true;

        } catch (Exception e) {
            getMessageManager().sendMessage(sender, "edit.setalignment.failed", Map.of("error", e.getMessage()));
            Logger.severe("Error setting alignment for hologram: " + hologramName, e);
            return false;
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
                .shadowStrength(settings.getShadowStrength())
                .viewRange(settings.getViewRange())
                .translation(settings.getTranslation())
                .rightRotationQuaternion(settings.getRightRotationQuaternion())
                .scale(settings.getScale())
                .leftRotationQuaternion(settings.getLeftRotationQuaternion());
    }

}

