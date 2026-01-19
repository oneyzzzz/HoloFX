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
        @TabCompleteConfig(position = 0, type = TabCompleteType.STATIC, suggestions = {"sky", "block"}),
        @TabCompleteConfig(position = 1, type = TabCompleteType.LINE_NUMBERS),
        @TabCompleteConfig(position = 2, type = TabCompleteType.STATIC, suggestions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"})
})
public class SetBrightnessEditCommand implements EditSubCommand {

    private final HoloFX plugin;

    public SetBrightnessEditCommand(HoloFX plugin) {
        this.plugin = plugin;
    }

    @Override
    public MessageManager getMessageManager() {
        return plugin.getMessageManager();
    }

    @Override
    public boolean execute(CommandSender sender, String hologramName, String[] args) {

        if (args.length < 2) {
            getMessageManager().sendMessage(sender, "edit.setbrightness.usage");
            return true;
        }

        String type = args[0].toLowerCase();
        Integer lineNumber = null;
        String brightnessArg;

        if (!type.equals("sky") && !type.equals("block")) {
            getMessageManager().sendMessage(sender, "edit.setbrightness.invalid_type");
            return true;
        }

        if (args.length >= 3) {
            try {
                lineNumber = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                getMessageManager().sendMessage(sender, "edit.line_must_be_number");
                return true;
            }
            brightnessArg = args[2];
        } else {
            brightnessArg = args[1];
        }

        int brightnessValue;
        try {
            brightnessValue = Integer.parseInt(brightnessArg);
            if (brightnessValue < 0 || brightnessValue > 15) {
                getMessageManager().sendMessage(sender, "edit.setbrightness.invalid_brightness");
                return true;
            }
        } catch (NumberFormatException e) {
            getMessageManager().sendMessage(sender, "edit.setbrightness.brightness_not_number");
            return true;
        }

        HologramDisplay display = plugin.getHologramDisplayManager().getHologram(hologramName);
        if (display == null) {
            getMessageManager().sendMessage(sender, "edit.not_active", Map.of("name", hologramName));
            return true;
        }

        try {
            Hologram hologramModel = display.getHologramConfig();
            List<Hologram.Line> lines = new ArrayList<>(hologramModel.getLines());

            if (lineNumber != null) {
                if (lineNumber < 1 || lineNumber > lines.size()) {
                    getMessageManager().sendMessage(sender, "edit.line_number_out_of_range", Map.of(
                            "line", String.valueOf(lineNumber),
                            "max_lines", String.valueOf(lines.size())
                    ));
                    return true;
                }

                updateLineBrightness(lines, lineNumber - 1, type, brightnessValue);

                getMessageManager().sendMessage(sender, "edit.setbrightness.set_line", Map.of(
                        "type", type,
                        "value", String.valueOf(brightnessValue),
                        "line", String.valueOf(lineNumber),
                        "name", hologramName
                ));
            } else {
                for (int i = 0; i < lines.size(); i++) {
                    updateLineBrightness(lines, i, type, brightnessValue);
                }

                getMessageManager().sendMessage(sender, "edit.setbrightness.set", Map.of(
                        "type", type,
                        "value", String.valueOf(brightnessValue),
                        "name", hologramName
                ));
            }

            hologramModel.setLines(lines);

            plugin.getHologramDisplayManager().despawnHologram(hologramName);
            if (!plugin.getHologramDisplayManager().spawnHologram(hologramModel)) {
                getMessageManager().sendMessage(sender, "edit.setbrightness.failed", Map.of("error", getMessageManager().getGeneralMessage("errors.hologram.spawn_failed", Map.of("world", hologramModel.getWorld().getName(), "name", hologramName))));
                return true;
            }

            HologramLoader loader = plugin.getHologramLoader();
            if (!loader.saveUpdatedHologramToFile(hologramName, lines)) {
                getMessageManager().sendMessage(sender, "edit.setbrightness.failed", Map.of("error", getMessageManager().getGeneralMessage("errors.files.save_failed", Map.of("name", hologramName))));
                return true;
            }

            return true;

        } catch (Exception e) {
            getMessageManager().sendMessage(sender, "edit.setbrightness.failed", Map.of("error", e.getMessage()));
            Logger.severe("Error setting brightness for hologram: " + hologramName, e);
            return true;
        }
    }

    private void updateLineBrightness(List<Hologram.Line> lines, int index, String type, int value) {
        Hologram.Line line = lines.get(index);
        Hologram.DisplaySettings oldSettings = line.getDisplaySettings();

        int sky = 0;
        int block = 0;

        if (oldSettings != null && oldSettings.getBrightness() != null) {
            sky = (oldSettings.getBrightness() >> 4) & 0x0F;
            block = oldSettings.getBrightness() & 0x0F;
        }

        if (type.equals("sky")) {
            sky = value;
        } else {
            block = value;
        }

        int brightness = (sky << 4) | block;

        Hologram.DisplaySettings newSettings = copyDisplaySettingsBuilder(oldSettings)
                .brightness(brightness)
                .build();

        lines.set(index, Hologram.Line.builder()
                .text(line.getText())
                .offset(line.getOffset())
                .displaySettings(newSettings)
                .build());
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
