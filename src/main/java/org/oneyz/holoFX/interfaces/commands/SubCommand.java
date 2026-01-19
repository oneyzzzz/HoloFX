package org.oneyz.holoFX.interfaces.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.oneyz.holoFX.utils.MessageManager;

import java.util.List;

/**
 * Base interface for all subcommands
 */
public interface SubCommand {

    /**
     * Execute the subcommand
     * @param sender The command sender
     * @param args The command arguments
     * @return true if command was executed successfully
     */
    boolean execute(CommandSender sender, String[] args);

    /**
     * Get tab completion suggestions for this subcommand
     * @param sender The command sender
     * @param args The command arguments so far
     * @return List of suggestions
     */
    default List<String> getTabCompletions(CommandSender sender, String[] args) {
        return List.of();
    }

    /**
     * Get the CommandInfo annotation for this subcommand
     */
    default CommandInfo getCommandInfo() {
        CommandInfo info = this.getClass().getAnnotation(CommandInfo.class);
        if (info == null) {
            throw new IllegalStateException("SubCommand " + this.getClass().getSimpleName() +
                    " must be annotated with @CommandInfo");
        }
        return info;
    }

    /**
     * Check if sender has permission to execute this command
     */
    default boolean hasPermission(CommandSender sender) {
        CommandInfo info = getCommandInfo();
        if (info.permission().isEmpty()) {
            return true;
        }
        return sender.hasPermission(info.permission());
    }

    /**
     * Check if command is restricted to players only
     */
    default boolean canExecute(CommandSender sender) {
        CommandInfo info = getCommandInfo();
        if (info.onlyPlayer() && !(sender instanceof Player)) {
            return false;
        }
        return hasPermission(sender);
    }

    /**
     * Get message manager - should be implemented by subcommands
     */
    MessageManager getMessageManager();

}

