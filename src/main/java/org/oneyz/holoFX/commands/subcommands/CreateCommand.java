package org.oneyz.holoFX.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.enums.TabCompleteType;
import org.oneyz.holoFX.interfaces.commands.CommandInfo;
import org.oneyz.holoFX.interfaces.commands.SubCommand;
import org.oneyz.holoFX.holograms.operations.CreateHologramOperation;
import org.oneyz.holoFX.interfaces.tabcomplete.TabComplete;
import org.oneyz.holoFX.interfaces.tabcomplete.TabCompleteConfig;
import org.oneyz.holoFX.tabcomplete.TabCompleteUtil;
import org.oneyz.holoFX.utils.MessageManager;

import java.util.List;

@CommandInfo(
        commandName = "create",
        permission = "holo.create",
        usage = "holo create <holo_name> <text>",
        descriptionPath = "descriptions.create",
        onlyPlayer = true,
        aliases = {"c", "new"}
)

@TabComplete({
        @TabCompleteConfig(position = 0, type = TabCompleteType.STATIC,
                suggestions = {"<hologram_name>"}),
        @TabCompleteConfig(position = 1, type = TabCompleteType.STATIC,
                suggestions = {"<text>"})
})
public class CreateCommand implements SubCommand {

    private final HoloFX plugin;
    private final TabCompleteUtil tabCompleteUtil;

    public CreateCommand(HoloFX plugin) {
        this.plugin = plugin;
        this.tabCompleteUtil = new TabCompleteUtil(plugin);
    }

    @Override
    public MessageManager getMessageManager() {
        return plugin.getMessageManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return false;
        }

        Player player = (Player) sender;
        String holoName = args[0];
        String holoText = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        CreateHologramOperation operation = new CreateHologramOperation(plugin, player, holoName, holoText);

        operation.execute();
        return true;
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return tabCompleteUtil.getSubCommandCompletions(sender, this, args);
    }
}

