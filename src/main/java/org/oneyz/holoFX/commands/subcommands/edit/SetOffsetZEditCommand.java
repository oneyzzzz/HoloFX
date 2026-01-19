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
        @TabCompleteConfig(position = 0, type = TabCompleteType.LINE_NUMBERS)
})
public class SetOffsetZEditCommand implements EditSubCommand {

    private final HoloFX plugin;

    public SetOffsetZEditCommand(HoloFX plugin) {
        this.plugin = plugin;
    }

    @Override
    public MessageManager getMessageManager() {
        return plugin.getMessageManager();
    }

    @Override
    public boolean execute(CommandSender sender, String hologramName, String[] args) {
        if (args.length < 1) {
            getMessageManager().sendMessage(sender, "edit.setoffsetz.usage");
            return true;
        }

        double offsetValue;
        Integer lineNumber = null;
        String valueStr;
        String firstArg = args[0];
        try {
            int possibleLineNum = Integer.parseInt(firstArg);
            if (args.length >= 2) {
                lineNumber = possibleLineNum;
                valueStr = args[1];
            } else {
                valueStr = firstArg;
            }
        } catch (NumberFormatException e) {
            valueStr = firstArg;
        }

        try {
            offsetValue = Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            getMessageManager().sendMessage(sender, "edit.setoffsetz.invalid_number");
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
                Hologram.Offset oldOffset = line.getOffset();

                Hologram.Offset updatedOffset = Hologram.Offset.builder()
                        .x(oldOffset.getX())
                        .y(oldOffset.getY())
                        .z(offsetValue)
                        .build();

                Hologram.Line updatedLine = Hologram.Line.builder()
                        .text(line.getText())
                        .offset(updatedOffset)
                        .displaySettings(line.getDisplaySettings())
                        .build();

                currentLines.set(lineNumber - 1, updatedLine);
                getMessageManager().sendMessage(sender, "edit.setoffsetz.set_line", Map.of(
                        "value", formatDouble(offsetValue),
                        "line", String.valueOf(lineNumber),
                        "name", hologramName
                ));
            } else {
                for (int i = 0; i < currentLines.size(); i++) {
                    Hologram.Line line = currentLines.get(i);
                    Hologram.Offset oldOffset = line.getOffset();

                    Hologram.Offset updatedOffset = Hologram.Offset.builder()
                            .x(oldOffset.getX())
                            .y(oldOffset.getY())
                            .z(offsetValue)
                            .build();

                    Hologram.Line updatedLine = Hologram.Line.builder()
                            .text(line.getText())
                            .offset(updatedOffset)
                            .displaySettings(line.getDisplaySettings())
                            .build();

                    currentLines.set(i, updatedLine);
                }
                getMessageManager().sendMessage(sender, "edit.setoffsetz.set", Map.of(
                        "value", formatDouble(offsetValue),
                        "name", hologramName
                ));
            }

            hologramModel.setLines(currentLines);

            plugin.getHologramDisplayManager().despawnHologram(hologramName);
            if (!plugin.getHologramDisplayManager().spawnHologram(hologramModel)) {
                getMessageManager().sendMessage(sender, "edit.setoffsetz.failed", Map.of("error", getMessageManager().getGeneralMessage("errors.hologram.spawn_failed", Map.of("world", hologramModel.getWorld().getName(), "name", hologramName))));
                return true;
            }

            HologramLoader loader = plugin.getHologramLoader();
            if (!loader.saveUpdatedHologramToFile(hologramName, currentLines)) {
                getMessageManager().sendMessage(sender, "edit.setoffsetz.failed", Map.of("error", getMessageManager().getGeneralMessage("errors.files.save_failed", Map.of("name", hologramName))));
                return true;
            }

            return true;

        } catch (Exception e) {
            getMessageManager().sendMessage(sender, "edit.setoffsetz.failed", Map.of("error", e.getMessage()));
            Logger.severe("Error setting offset Z for hologram: " + hologramName, e);
            return true;
        }
    }

    private String formatDouble(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        } else {
            return String.format("%.2f", value);
        }
    }
}

