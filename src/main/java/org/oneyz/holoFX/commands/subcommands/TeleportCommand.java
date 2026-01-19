package org.oneyz.holoFX.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.enums.TabCompleteType;
import org.oneyz.holoFX.interfaces.commands.CommandInfo;
import org.oneyz.holoFX.interfaces.commands.SubCommand;
import org.oneyz.holoFX.holograms.displays.HologramDisplay;
import org.oneyz.holoFX.interfaces.tabcomplete.TabComplete;
import org.oneyz.holoFX.interfaces.tabcomplete.TabCompleteConfig;
import org.oneyz.holoFX.tabcomplete.TabCompleteUtil;
import org.oneyz.holoFX.utils.Logger;
import org.oneyz.holoFX.utils.MessageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommandInfo(
        commandName = "tp",
        permission = "holo.tp",
        usage = "holo tp <hologram_name>",
        descriptionPath = "descriptions.teleport",
        onlyPlayer = true,
        aliases = {"teleport"}
)

@TabComplete({
        @TabCompleteConfig(position = 0, type = TabCompleteType.HOLOGRAM_LIST),
})
public class TeleportCommand implements SubCommand {

    private final HoloFX plugin;
    private final TabCompleteUtil tabCompleteUtil;

    public TeleportCommand(HoloFX plugin) {
        this.plugin = plugin;
        this.tabCompleteUtil = new TabCompleteUtil(plugin);
    }

    @Override
    public MessageManager getMessageManager() {
        return plugin.getMessageManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            return false;
        }

        Player player = (Player) sender;
        String hologramName = args[0];

        HologramDisplay display = plugin.getHologramDisplayManager().getHologram(hologramName);

        if (display == null) {
            getMessageManager().sendMessage(sender, "edit.not_active", Map.of("name", hologramName));
            return true;
        }

        try {
            player.teleport(display.getBaseLocation());
            getMessageManager().sendMessage(sender, "teleport.teleported", Map.of("name", hologramName));
            return true;
        } catch (Exception e) {
            getMessageManager().sendMessage(sender, "teleport.teleport_failed", Map.of("error", e.getMessage()));
            Logger.severe("Error teleporting to hologram: " + hologramName, e);
            return true;
        }
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return tabCompleteUtil.getSubCommandCompletions(sender, this, args);
    }
}

