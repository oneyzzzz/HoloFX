package org.oneyz.holoFX.tabcomplete;

import org.bukkit.command.CommandSender;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.interfaces.commands.EditSubCommand;
import org.oneyz.holoFX.interfaces.commands.SubCommand;
import org.oneyz.holoFX.interfaces.tabcomplete.TabComplete;
import org.oneyz.holoFX.interfaces.tabcomplete.TabCompleteConfig;
import org.oneyz.holoFX.interfaces.tabcomplete.TabCompleteEdit;

import java.util.*;

/**
 * Utility class for handling tab completions using annotations
 */
public class TabCompleteUtil {

    private final HoloFX plugin;
    private final TabCompleteProvider provider;

    public TabCompleteUtil(HoloFX plugin) {
        this.plugin = plugin;
        this.provider = new TabCompleteProvider(plugin);
    }

    /**
     * Get tab completions for a SubCommand based on @TabComplete annotation
     *
     * @param sender The command sender
     * @param command The SubCommand instance
     * @param args The command arguments
     * @return List of suggestions
     */
    public List<String> getSubCommandCompletions(CommandSender sender, SubCommand command, String[] args) {
        Class<?> commandClass = command.getClass();
        TabComplete tabCompleteAnnotation = commandClass.getAnnotation(TabComplete.class);

        if (tabCompleteAnnotation == null) {
            return Collections.emptyList();
        }

        int currentArgPosition = args.length - 1;

        for (TabCompleteConfig config : tabCompleteAnnotation.value()) {
            if (config.position() == currentArgPosition) {
                return provider.getCompletions(sender, config.type(), config.suggestions(), args);
            }
        }

        return Collections.emptyList();
    }

    /**
     * Get tab completions for an EditSubCommand based on @TabCompleteEdit annotation
     *
     * @param sender The command sender
     * @param command The EditSubCommand instance
     * @param args The edit command arguments (without hologram name and action)
     * @return List of suggestions
     */
    public List<String> getEditCommandCompletions(CommandSender sender, EditSubCommand command, String[] args) {
        return getEditCommandCompletions(sender, command, args, null);
    }

    /**
     * Get tab completions for an EditSubCommand based on @TabCompleteEdit annotation with hologram context
     *
     * @param sender The command sender
     * @param command The EditSubCommand instance
     * @param args The edit command arguments (without hologram name and action)
     * @param hologramName The name of the hologram being edited (for context)
     * @return List of suggestions
     */
    public List<String> getEditCommandCompletions(CommandSender sender, EditSubCommand command, String[] args, String hologramName) {
        Class<?> commandClass = command.getClass();
        TabCompleteEdit tabCompleteAnnotation = commandClass.getAnnotation(TabCompleteEdit.class);

        if (tabCompleteAnnotation == null) {
            return Collections.emptyList();
        }

        int currentArgPosition = args.length - 1;

        for (TabCompleteConfig config : tabCompleteAnnotation.value()) {
            if (config.position() == currentArgPosition) {
                return provider.getCompletions(sender, config.type(), config.suggestions(), args, hologramName);
            }
        }

        return Collections.emptyList();
    }
}

