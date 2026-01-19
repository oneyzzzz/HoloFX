package org.oneyz.holoFX.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.enums.TabCompleteType;
import org.oneyz.holoFX.interfaces.commands.CommandInfo;
import org.oneyz.holoFX.interfaces.commands.SubCommand;
import org.oneyz.holoFX.holograms.displays.HologramDisplayManager;
import org.oneyz.holoFX.interfaces.tabcomplete.TabComplete;
import org.oneyz.holoFX.interfaces.tabcomplete.TabCompleteConfig;
import org.oneyz.holoFX.loader.HologramLoader;
import org.oneyz.holoFX.models.Hologram;
import org.oneyz.holoFX.tabcomplete.TabCompleteUtil;
import org.oneyz.holoFX.utils.Logger;
import org.oneyz.holoFX.utils.MessageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommandInfo(
        commandName = "reload",
        permission = "holo.reload",
        usage = "holo reload <hologram_name>",
        descriptionPath = "descriptions.reload",
        aliases = {"r", "rl"}
)

@TabComplete({
        @TabCompleteConfig(position = 0, type = TabCompleteType.HOLOGRAM_LIST)
})
public class ReloadCommand implements SubCommand {

    private final HoloFX plugin;
    private final TabCompleteUtil tabCompleteUtil;

    public ReloadCommand(HoloFX plugin) {
        this.plugin = plugin;
        this.tabCompleteUtil = new TabCompleteUtil(plugin);

    }

    @Override
    public MessageManager getMessageManager() {
        return plugin.getMessageManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            return reloadSpecificHologram(sender, args[0]);
        }

        return reloadAllHolograms(sender);
    }

    private boolean reloadAllHolograms(CommandSender sender) {
        getMessageManager().sendMessage(sender, "reload.reloading");

        long startTime = System.currentTimeMillis();

        try {
            plugin.getMessageManager().loadMessages();
            getMessageManager().sendMessage(sender, "reload.messages_loaded");
        } catch (Exception e) {
            getMessageManager().sendMessage(sender, "reload.failed", Map.of("error", e.getMessage()));
            Logger.severe("Error reloading messages", e);
        }

        HologramLoader loader = plugin.getHologramLoader();
        HologramDisplayManager displayManager = plugin.getHologramDisplayManager();

        displayManager.despawnAllHolograms();

        loader.reloadAll();

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            loader.validateAllWorlds();
            plugin.getBatchLoader().loadAndSpawnAllHolograms();
        }, 20L);

        long loadTime = System.currentTimeMillis() - startTime;
        int count = loader.getLoadedCount();

        getMessageManager().sendMessage(sender, "reload.holograms_loaded", Map.of("count", String.valueOf(count)));
        getMessageManager().sendMessage(sender, "reload.reloaded", Map.of("ms", String.valueOf(loadTime)));
        return true;
    }

    private boolean reloadSpecificHologram(CommandSender sender, String hologramName) {
        getMessageManager().sendMessage(sender, "reload.reloading");

        try {
            plugin.getMessageManager().loadMessages();
        } catch (Exception e) {
            Logger.warning("Error reloading messages during hologram reload: " + e.getMessage());
        }

        HologramLoader loader = plugin.getHologramLoader();
        HologramDisplayManager displayManager = plugin.getHologramDisplayManager();

        Map<String, Hologram> allHolograms = loader.getAllHolograms();
        Hologram hologram = allHolograms.values().stream()
                .filter(h -> h.getName().equalsIgnoreCase(hologramName))
                .findFirst()
                .orElse(null);

        if (hologram == null) {
            getMessageManager().sendMessage(sender, "reload.hologram_not_found", Map.of("name", hologramName));
            return true;
        }

        long startTime = System.currentTimeMillis();

        if (displayManager.isActive(hologramName)) {
            displayManager.despawnHologram(hologramName);
            getMessageManager().sendMessage(sender, "reload.hologram_despawned", Map.of("name", hologramName));
        }

        loader.reloadAll();

        Hologram reloadedHologram = allHolograms.values().stream()
                .filter(h -> h.getName().equalsIgnoreCase(hologramName))
                .findFirst()
                .orElse(null);

        if (reloadedHologram == null) {
            getMessageManager().sendMessage(sender, "reload.hologram_not_found", Map.of("name", hologramName));
            return true;
        }

        if (!reloadedHologram.hasValidWorld()) {
            getMessageManager().sendMessage(sender, "reload.hologram_world_invalid",
                    Map.of("world", reloadedHologram.getLocation().getWorld(), "name", hologramName));
            return true;
        }

        if (displayManager.spawnHologram(reloadedHologram)) {
            long loadTime = System.currentTimeMillis() - startTime;
            getMessageManager().sendMessage(sender, "reload.hologram_reloaded",
                    Map.of("name", hologramName, "ms", String.valueOf(loadTime)));
            return true;
        } else {
            getMessageManager().sendMessage(sender, "reload.hologram_spawn_failed", Map.of("name", hologramName));
            return true;
        }
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return tabCompleteUtil.getSubCommandCompletions(sender, this, args);
    }

}

