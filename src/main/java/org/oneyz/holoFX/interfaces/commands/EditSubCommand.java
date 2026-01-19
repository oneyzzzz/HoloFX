package org.oneyz.holoFX.interfaces.commands;

import org.bukkit.command.CommandSender;
import org.oneyz.holoFX.utils.MessageManager;

public interface EditSubCommand {

    /**
     * Execute the edit subcommand
     *
     * @param sender       The command sender
     * @param hologramName The name of the hologram to edit
     * @param args         Additional arguments for the action
     * @return true if successful
     */
    boolean execute(CommandSender sender, String hologramName, String[] args);
    MessageManager getMessageManager();

}

