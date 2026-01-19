package org.oneyz.holoFX.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.enums.TabCompleteType;
import org.oneyz.holoFX.interfaces.commands.CommandInfo;
import org.oneyz.holoFX.interfaces.commands.SubCommand;
import org.oneyz.holoFX.holograms.displays.HologramDisplay;
import org.oneyz.holoFX.holograms.operations.SummonHologramOperation;
import org.oneyz.holoFX.interfaces.tabcomplete.TabComplete;
import org.oneyz.holoFX.interfaces.tabcomplete.TabCompleteConfig;
import org.oneyz.holoFX.tabcomplete.TabCompleteUtil;
import org.oneyz.holoFX.utils.MessageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommandInfo(
        commandName = "summon",
        permission = "holo.summon",
        usage = "holo summon <hologram_name>",
        descriptionPath = "descriptions.summon",
        onlyPlayer = true,
        aliases = {"sum"}
)

@TabComplete({
        @TabCompleteConfig(position = 0, type = TabCompleteType.HOLOGRAM_LIST)
})
public class SummonCommand implements SubCommand {

    private final HoloFX plugin;
    private final TabCompleteUtil tabCompleteUtil;

    public SummonCommand(HoloFX plugin) {
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

        SummonHologramOperation operation = new SummonHologramOperation(plugin, player, hologramName);

        operation.execute();
        return true;
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return tabCompleteUtil.getSubCommandCompletions(sender, this, args);
    }
}

