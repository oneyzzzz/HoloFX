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
        @TabCompleteConfig(position = 1, type = TabCompleteType.STATIC, suggestions = {"Example new text"})
})
public class EditLineEditCommand implements EditSubCommand {

    private final HoloFX plugin;

    public EditLineEditCommand(HoloFX plugin) {
        this.plugin = plugin;
    }

    @Override
    public MessageManager getMessageManager() {
        return plugin.getMessageManager();
    }

    @Override
    public boolean execute(CommandSender sender, String hologramName, String[] args) {
        if (args.length < 2) {
            getMessageManager().sendMessage(sender, "edit.editline.usage");
            return true;
        }

        String lineNumStr = args[0];
        int lineNumber;

        try {
            lineNumber = Integer.parseInt(lineNumStr);
        } catch (NumberFormatException e) {
            getMessageManager().sendMessage(sender, "edit.line_must_be_number");
            return true;
        }

        String newText = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        HologramDisplay display = plugin.getHologramDisplayManager().getHologram(hologramName);
        if (display == null) {
            getMessageManager().sendMessage(sender, "edit.not_active", Map.of("name", hologramName));
            return true;
        }

        try {
            Hologram hologramModel = display.getHologramConfig();
            List<Hologram.Line> currentLines = new ArrayList<>(hologramModel.getLines());

            if (lineNumber < 1 || lineNumber > currentLines.size()) {
                getMessageManager().sendMessage(sender, "edit.line_number_out_of_range", Map.of("line", String.valueOf(lineNumber), "max_lines", String.valueOf(currentLines.size())));
                return true;
            }

            Hologram.Line lineToEdit = currentLines.get(lineNumber - 1);

            String oldText = lineToEdit.getTextAsString();
            if (oldText.equals(newText)) {
                getMessageManager().sendMessage(sender, "edit.same_text");
                return true;
            }

            Hologram.Line updatedLine = Hologram.Line.builder()
                    .text(newText)
                    .offset(lineToEdit.getOffset())
                    .displaySettings(lineToEdit.getDisplaySettings())
                    .build();

            currentLines.set(lineNumber - 1, updatedLine);

            hologramModel.setLines(currentLines);

            plugin.getHologramDisplayManager().despawnHologram(hologramName);
            if (!plugin.getHologramDisplayManager().spawnHologram(hologramModel)) {
                getMessageManager().sendMessage(sender, "edit.editline.failed", Map.of("error", getMessageManager().getGeneralMessage("errors.hologram.spawn_failed", Map.of("world", hologramModel.getWorld().getName(), "name", hologramName))));
                return true;
            }

            HologramLoader loader = plugin.getHologramLoader();
            if (!loader.saveUpdatedHologramToFile(hologramName, currentLines)) {
                getMessageManager().sendMessage(sender, "edit.editline.failed", Map.of("error", getMessageManager().getGeneralMessage("errors.files.save_failed", Map.of("name", hologramName))));
                return true;
            }

            getMessageManager().sendMessage(sender, "edit.editline.edited", Map.of("name", hologramName, "line", lineNumStr));
            getMessageManager().sendMessage(sender, "edit.old_text", Map.of("old_text", oldText));
            getMessageManager().sendMessage(sender, "edit.new_text", Map.of("new_text", newText));
            return true;

        } catch (Exception e) {
            getMessageManager().sendMessage(sender, "edit.editline.failed", Map.of("error", e.getMessage()));
            Logger.severe("Error editing line in hologram: " + hologramName, e);
            return true;
        }
    }
}

