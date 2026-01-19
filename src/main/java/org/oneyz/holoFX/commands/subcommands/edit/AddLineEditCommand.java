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
        @TabCompleteConfig(position = 0, type = TabCompleteType.STATIC, suggestions = {"Example Text"})
})
public class AddLineEditCommand implements EditSubCommand {

    private final HoloFX plugin;

    public AddLineEditCommand(HoloFX plugin) {
        this.plugin = plugin;
    }

    @Override
    public MessageManager getMessageManager() {
        return plugin.getMessageManager();
    }

    @Override
    public boolean execute(CommandSender sender, String hologramName, String[] args) {
        if (args.length < 1) {
            getMessageManager().sendMessage(sender, "edit.addline.usage");
            return true;
        }

        String newText = String.join(" ", args);

        HologramDisplay display = plugin.getHologramDisplayManager().getHologram(hologramName);
        if (display == null) {
            getMessageManager().sendMessage(sender, "edit.not_active");
            return true;
        }

        try {
            Hologram hologramModel = display.getHologramConfig();
            List<Hologram.Line> currentLines = new ArrayList<>(hologramModel.getLines());

            Hologram.Line newLine = Hologram.Line.builder()
                    .text(newText)
                    .offset(Hologram.Offset.builder()
                            .x(0.0)
                            .y(-currentLines.size() * 0.25)
                            .z(0.0)
                            .build())
                    .displaySettings(Hologram.DisplaySettings.builder()
                            .textOpacity(Hologram.DisplaySettings.DEFAULT_TEXT_OPACITY)
                            .lineWidth(Hologram.DisplaySettings.DEFAULT_LINE_WIDTH)
                            .textAlignment(Hologram.DisplaySettings.DEFAULT_TEXT_ALIGNMENT)
                            .defaultBackground(Hologram.DisplaySettings.DEFAULT_BACKGROUND)
                            .seeThrough(Hologram.DisplaySettings.DEFAULT_SEE_THROUGH)
                            .shadow(Hologram.DisplaySettings.DEFAULT_SHADOW)
                            .billboard(Hologram.DisplaySettings.DEFAULT_BILLBOARD)
                            .build())
                    .build();

            currentLines.add(newLine);

            hologramModel.setLines(currentLines);

            plugin.getHologramDisplayManager().despawnHologram(hologramName);
            if (!plugin.getHologramDisplayManager().spawnHologram(hologramModel)) {
                getMessageManager().sendMessage(sender, "edit.addline.failed", Map.of("error", getMessageManager().getGeneralMessage("errors.hologram.spawn_failed", Map.of("world", hologramModel.getWorld().getName(), "name", hologramName))));
                return true;
            }

            HologramLoader loader = plugin.getHologramLoader();
            if (!loader.saveUpdatedHologramToFile(hologramName, currentLines)) {
                getMessageManager().sendMessage(sender, "edit.addline.failed", Map.of("error", getMessageManager().getGeneralMessage("errors.files.save_failed", Map.of("name", hologramName))));
                return true;
            }

            getMessageManager().sendMessage(sender, "edit.addline.added", Map.of("name", hologramName));

            return true;

        } catch (Exception e) {
            getMessageManager().sendMessage(sender, "edit.addline.failed", Map.of("error", e.getMessage()));
            Logger.severe("Error adding line to hologram: " + hologramName, e);
            return true;
        }
    }

}

