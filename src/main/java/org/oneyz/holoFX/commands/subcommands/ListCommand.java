package org.oneyz.holoFX.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.enums.TabCompleteType;
import org.oneyz.holoFX.interfaces.commands.CommandInfo;
import org.oneyz.holoFX.interfaces.commands.SubCommand;
import org.oneyz.holoFX.interfaces.tabcomplete.TabComplete;
import org.oneyz.holoFX.interfaces.tabcomplete.TabCompleteConfig;
import org.oneyz.holoFX.models.Hologram;
import org.oneyz.holoFX.tabcomplete.TabCompleteUtil;
import org.oneyz.holoFX.utils.MessageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommandInfo(
        commandName = "list",
        permission = "holo.list",
        usage = "holo list <page>",
        descriptionPath = "descriptions.list",
        aliases = {"l", "ls"}
)

@TabComplete({
        @TabCompleteConfig(position = 0, type = TabCompleteType.CUSTOM)
})



public class ListCommand implements SubCommand {

    private final HoloFX plugin;
    private final TabCompleteUtil tabCompleteUtil;

    public ListCommand(HoloFX plugin) {
        this.plugin = plugin;
        this.tabCompleteUtil = new TabCompleteUtil(plugin);
    }

    @Override
    public MessageManager getMessageManager() {
        return plugin.getMessageManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Map<String, Hologram> holograms = plugin.getHologramLoader().getAllHolograms();

        if (holograms.isEmpty()) {
            getMessageManager().sendMessage(sender, "list.empty");
            return true;
        }

        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                getMessageManager().sendMessage(sender, "list.invalid_page");
                return true;
            }
        }

        final int ITEMS_PER_PAGE = 5;
        int totalPages = (int) Math.ceil((double) holograms.size() / ITEMS_PER_PAGE);

        if (page < 1 || page > totalPages) {
            getMessageManager().sendMessage(sender, "list.page_out_of_range",
                    Map.of("page", String.valueOf(page), "max", String.valueOf(totalPages)));
            return true;
        }

        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, holograms.size());

        getMessageManager().sendMessage(sender, "list.header");

        List<Hologram> hologramList = new java.util.ArrayList<>(holograms.values());
        for (int i = startIndex; i < endIndex; i++) {
            Hologram hologram = hologramList.get(i);

            String status = hologram.isEnabled() ?
                getMessageManager().getRawMessage("list.active") :
                getMessageManager().getRawMessage("list.inactive");

            String world = hologram.getLocation().getWorld();

            getMessageManager().sendMessage(sender, "commands.list.entry", Map.of(
                    "name", hologram.getName(),
                    "world", world,
                    "status", status
            ));
        }

        getMessageManager().sendMessage(sender, "list.page_info",
            Map.of("current", String.valueOf(page), "total", String.valueOf(totalPages)));

        if (page < totalPages) {
            getMessageManager().sendMessage(sender, "list.next_page_hint",
                Map.of("command", "/holo list " + (page + 1), "page", String.valueOf(page + 1)));
        }

        getMessageManager().sendMessage(sender, "list.footer");

        return true;
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return getPageNumberCompletions(args[0]);
        }
        return new ArrayList<>();
    }

    private List<String> getPageNumberCompletions(String partial) {
        Map<String, Hologram> holograms = plugin.getHologramLoader().getAllHolograms();

        if (holograms.isEmpty()) {
            return new ArrayList<>();
        }

        final int ITEMS_PER_PAGE = 5;
        int totalPages = (int) Math.ceil((double) holograms.size() / ITEMS_PER_PAGE);

        List<String> suggestions = new ArrayList<>();

        try {
            int partialNum = Integer.parseInt(partial);
            for (int i = Math.max(1, partialNum); i <= Math.min(totalPages, partialNum + 3); i++) {
                suggestions.add(String.valueOf(i));
            }
        } catch (NumberFormatException e) {
            for (int i = 1; i <= Math.min(3, totalPages); i++) {
                suggestions.add(String.valueOf(i));
            }
        }

        return suggestions;
    }
}

