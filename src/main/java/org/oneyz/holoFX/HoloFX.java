package org.oneyz.holoFX;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.oneyz.holoFX.commands.CommandManager;
import org.oneyz.holoFX.commands.subcommands.*;
import org.oneyz.holoFX.events.PlayerEventListener;
import org.oneyz.holoFX.holograms.displays.HologramDisplayManager;
import org.oneyz.holoFX.loader.HologramBatchLoader;
import org.oneyz.holoFX.loader.HologramLoader;
import org.oneyz.holoFX.loader.HologramWorldListener;
import org.oneyz.holoFX.models.Hologram;
import org.oneyz.holoFX.utils.Logger;
import org.oneyz.holoFX.utils.MessageManager;

import java.io.File;
import java.util.Map;
import java.util.Objects;

public final class HoloFX extends JavaPlugin {

    @Getter
    private HologramLoader hologramLoader;
    @Getter
    private HologramBatchLoader batchLoader;
    @Getter
    private HologramWorldListener worldListener;
    @Getter
    private CommandManager commandManager;
    @Getter
    private HologramDisplayManager hologramDisplayManager;
    @Getter
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        Logger.init(this);
        Logger.info("§6HoloFX v" + getDescription().getVersion() + " is loading...");
        this.messageManager = new MessageManager(this);

        File dataFolder = new File(getDataFolder(), "holograms");
        this.hologramLoader = new HologramLoader(dataFolder);

        Map<String, Hologram> holograms = hologramLoader.loadAllHolograms();
        Logger.info("§aHoloFX successfully loaded " + holograms.size() + " hologram(s) from configuration!");
        this.hologramDisplayManager = new HologramDisplayManager(this);
        getServer().getPluginManager().registerEvents(
                new PlayerEventListener(this, hologramDisplayManager, hologramDisplayManager.getVisibilityManager()),
                this
        );
        this.batchLoader = new HologramBatchLoader(this, hologramLoader, hologramDisplayManager, 5, 10);
        this.worldListener = new HologramWorldListener(batchLoader);
        getServer().getPluginManager().registerEvents(worldListener, this);
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            Logger.info("All worlds should be loaded. Spawning configured holograms...");
            hologramLoader.validateAllWorlds();
            batchLoader.loadAndSpawnAllHolograms();
            worldListener.setInitialLoadCompleted();
        }, 20L);


        this.commandManager = new CommandManager(this);
        registerCommands();

        Logger.info("§aHoloFX enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (hologramDisplayManager != null) {
            hologramDisplayManager.despawnAllHolograms();
        }
        if (hologramDisplayManager != null && hologramDisplayManager.getVisibilityManager() != null) {
            hologramDisplayManager.getVisibilityManager().clearAllCache();
        }

        Logger.info("§cHoloFX is shutting down...");
    }

    /**
     * Register all subcommands
     */
    private void registerCommands() {
        commandManager.registerSubCommands(
                new CreateCommand(this),
                new ListCommand(this),
                new RemoveCommand(this),
                new ReloadCommand(this),
                new EditCommand(this),
                new TeleportCommand(this),
                new SummonCommand(this),
                new SettingsCommand(this)
        );

        if (getCommand("holo") != null) {
            Objects.requireNonNull(getCommand("holo")).setExecutor(commandManager);
            Objects.requireNonNull(getCommand("holo")).setTabCompleter(commandManager);
            Logger.info("Registered 'holo' command with " + commandManager.getSubCommandCount() + " subcommand(s)");
        } else {
            Logger.severe("Could not register 'holo' command! Check plugin.yml");
        }
    }


}
