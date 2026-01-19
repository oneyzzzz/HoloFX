package org.oneyz.holoFX.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.enums.TabCompleteType;
import org.oneyz.holoFX.interfaces.commands.CommandInfo;
import org.oneyz.holoFX.interfaces.commands.SubCommand;
import org.oneyz.holoFX.holograms.operations.RemoveHologramOperation;
import org.oneyz.holoFX.interfaces.tabcomplete.TabComplete;
import org.oneyz.holoFX.interfaces.tabcomplete.TabCompleteConfig;
import org.oneyz.holoFX.tabcomplete.TabCompleteUtil;
import org.oneyz.holoFX.utils.MessageManager;

import java.util.List;

@CommandInfo(
        commandName = "remove",
        permission = "holo.remove",
        usage = "holo remove <holo_name>",
        descriptionPath = "descriptions.remove",
        aliases = {"rm", "delete", "del"}
)

@TabComplete({
        @TabCompleteConfig(position = 0, type = TabCompleteType.HOLOGRAM_LIST)
})
public class RemoveCommand implements SubCommand {

    private final HoloFX plugin;
    private final TabCompleteUtil tabCompleteUtil;

    public RemoveCommand(HoloFX plugin) {
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

        String hologramName = args[0];

        RemoveHologramOperation operation = new RemoveHologramOperation(plugin, sender, hologramName);

        operation.execute();
        return true;
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return tabCompleteUtil.getSubCommandCompletions(sender, this, args);
    }

}

