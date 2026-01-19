package org.oneyz.holoFX.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.interfaces.commands.CommandInfo;
import org.oneyz.holoFX.interfaces.commands.SubCommand;
import org.oneyz.holoFX.utils.Logger;
import org.oneyz.holoFX.utils.MessageManager;

import java.util.*;

/**
 * Command manager that handles the main "holo" command and its subcommands.
 * Automatically registers subcommands that are annotated with @CommandInfo.
 */
public class CommandManager implements CommandExecutor, TabCompleter {

    private final HoloFX plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final List<String> commandAliases = new ArrayList<>();
    private final MessageManager messageManager;

    public CommandManager(HoloFX plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }


    /**
     * Register a subcommand
     * @param subCommand The subcommand to register
     */
    public void registerSubCommand(SubCommand subCommand) {
        CommandInfo info = subCommand.getCommandInfo();
        String commandName = info.commandName().toLowerCase();

        if (subCommands.containsKey(commandName)) {
            Logger.warning("Subcommand '" + commandName + "' is already registered!");
            return;
        }

        subCommands.put(commandName, subCommand);
        commandAliases.add(commandName);
        for (String alias : info.aliases()) {
            String lowerAlias = alias.toLowerCase();
            if (!subCommands.containsKey(lowerAlias)) {
                subCommands.put(lowerAlias, subCommand);
            }
        }

        Logger.info("Registered subcommand: /" + commandName +
                (info.permission().isEmpty() ? "" : " (permission: " + info.permission() + ")"));
    }

    /**
     * Register multiple subcommands
     * @param subCommands Array of subcommands to register
     */
    public void registerSubCommands(SubCommand... subCommands) {
        for (SubCommand subCommand : subCommands) {
            registerSubCommand(subCommand);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommandName = args[0].toLowerCase();

        if (!subCommands.containsKey(subCommandName)) {
            plugin.getMessageManager().sendMessage(sender, "subcommand.invalid_arguments");
            sendHelp(sender);
            return true;
        }

        SubCommand subCommand = subCommands.get(subCommandName);

        if (!subCommand.canExecute(sender)) {
            if (subCommand.getCommandInfo().onlyPlayer()) {
                subCommand.getMessageManager().sendMessage(sender, "player_only");
            } else {
                subCommand.getMessageManager().sendMessage(sender, "permission_denied");
            }
            return true;
        }

        try {
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            boolean success = subCommand.execute(sender, subArgs);

            if (!success) {
                String commandName = subCommand.getCommandInfo().commandName().toLowerCase();
                subCommand.getMessageManager().sendMessage(sender, commandName + ".usage");
            }

            return true;
        } catch (Exception e) {
            subCommand.getMessageManager().sendMessage(sender, messageManager.getGeneralMessage("errors.commands.execution_failed"));
            Logger.severe("Error executing subcommand " + subCommandName, e);
            return true;
        }
    }


    private void sendHelp(CommandSender sender) {
        messageManager.sendRawMessage(sender, "help.header");
        messageManager.sendRawMessage(sender, "help.title");

        for (String cmdName : commandAliases) {
            SubCommand subCommand = subCommands.get(cmdName);
            if (subCommand != null && subCommand.hasPermission(sender)) {
                CommandInfo info = subCommand.getCommandInfo();

                String description = "";
                if (!info.descriptionPath().isEmpty()) {
                    description = messageManager.getRawMessage(info.descriptionPath());
                } else if (!info.description().isEmpty()) {
                    description = info.description();
                }

                messageManager.sendRawMessage(sender, "help.command_format",
                    Map.of("usage", info.usage(), "description", description));
            }
        }

        messageManager.sendRawMessage(sender, "help.footer");
    }


    public SubCommand getSubCommand(String name) {
        return subCommands.get(name.toLowerCase());
    }

    public boolean isRegistered(String name) {
        return subCommands.containsKey(name.toLowerCase());
    }

    public Map<String, SubCommand> getAllSubCommands() {
        return new HashMap<>(subCommands);
    }

    public int getSubCommandCount() {
        return commandAliases.size();
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();

            for (String cmdName : commandAliases) {
                SubCommand subCommand = subCommands.get(cmdName);
                if (subCommand != null && subCommand.hasPermission(sender)) {
                    if (cmdName.equals(subCommand.getCommandInfo().commandName().toLowerCase())) {
                        if (cmdName.startsWith(input)) {
                            suggestions.add(cmdName);
                        }
                    }
                }
            }

            Collections.sort(suggestions);
            return suggestions;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand != null && subCommand.hasPermission(sender)) {
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            return subCommand.getTabCompletions(sender, subArgs);
        }

        return new ArrayList<>();
    }

}

