package org.oneyz.holoFX.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.interfaces.commands.CommandInfo;
import org.oneyz.holoFX.interfaces.commands.EditSubCommand;
import org.oneyz.holoFX.interfaces.commands.SubCommand;
import org.oneyz.holoFX.interfaces.tabcomplete.TabComplete;
import org.oneyz.holoFX.interfaces.tabcomplete.TabCompleteConfig;
import org.oneyz.holoFX.tabcomplete.TabCompleteUtil;
import org.oneyz.holoFX.enums.TabCompleteType;
import org.oneyz.holoFX.commands.subcommands.edit.*;
import org.oneyz.holoFX.utils.Logger;
import org.oneyz.holoFX.utils.MessageManager;

import java.util.*;

@CommandInfo(
        commandName = "edit",
        permission = "holo.edit",
        usage = "holo edit <hologram_name> <action> [args...]",
        descriptionPath = "descriptions.edit",
        aliases = {"e"}
)
@TabComplete({
        @TabCompleteConfig(position = 0, type = TabCompleteType.HOLOGRAM_LIST),
        @TabCompleteConfig(position = 1, type = TabCompleteType.EDIT_ACTIONS)
})
public class EditCommand implements SubCommand {

    private final HoloFX plugin;
    private final Map<String, EditSubCommand> editCommands;
    private final TabCompleteUtil tabCompleteUtil;

    public EditCommand(HoloFX plugin) {
        this.plugin = plugin;
        this.editCommands = new HashMap<>();
        this.tabCompleteUtil = new TabCompleteUtil(plugin);

        registerEditCommands();
    }

    @Override
    public MessageManager getMessageManager() {
        return plugin.getMessageManager();
    }

    private void registerEditCommands() {
        editCommands.put("addline", new AddLineEditCommand(plugin));
        editCommands.put("editline", new EditLineEditCommand(plugin));
        editCommands.put("removeline", new RemoveLineEditCommand(plugin));
        editCommands.put("set_scale", new SetScaleEditCommand(plugin));
        editCommands.put("set_defaultbackground", new SetDefaultBackgroundEditCommand(plugin));
        editCommands.put("set_brightness", new SetBrightnessEditCommand(plugin));
        editCommands.put("set_background", new SetBackgroundEditCommand(plugin));
        editCommands.put("set_viewrange", new SetViewRangeEditCommand(plugin));
        editCommands.put("set_shadowstrength", new SetShadowStrengthEditCommand(plugin));
        editCommands.put("set_shadowradius", new SetShadowRadiusEditCommand(plugin));
        editCommands.put("set_alignment", new SetAlignmentEditCommand(plugin));
        editCommands.put("set_billboard", new SetBillboardEditCommand(plugin));
        editCommands.put("set_seethrough", new SetSeeThroughEditCommand(plugin));
        editCommands.put("set_linewidth", new SetLineWidthEditCommand(plugin));
        editCommands.put("set_shadow", new SetShadowEditCommand(plugin));
        editCommands.put("set_permission", new SetPermissionEditCommand(plugin));
        editCommands.put("set_opacity", new SetOpacityEditCommand(plugin));
        editCommands.put("set_leftrotation", new SetLeftRotationEditCommand(plugin));
        editCommands.put("set_rightrotation", new SetRightRotationEditCommand(plugin));
        editCommands.put("set_offset_x", new SetOffsetXEditCommand(plugin));
        editCommands.put("set_offset_y", new SetOffsetYEditCommand(plugin));
        editCommands.put("set_offset_z", new SetOffsetZEditCommand(plugin));
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            getMessageManager().sendMessage(sender, "edit.usage");
            displayAvailableActions(sender);
            return true;
        }

        String hologramName = args[0];
        String action = args[1];
        String[] actionArgs = java.util.Arrays.copyOfRange(args, 2, args.length);

        EditSubCommand editCommand = editCommands.get(action.toLowerCase());

        if (editCommand == null) {
            getMessageManager().sendMessage(sender, "edit.general.invalid_action", Map.of("action", action));
            getMessageManager().sendMessage(sender, "edit.general.available_actions_info");
            displayAvailableActions(sender);
            return true;
        }

        try {
            return editCommand.execute(sender, hologramName, actionArgs);
        } catch (Exception e) {
            getMessageManager().sendMessage(sender, "edit.general.error_executing", Map.of("error", e.getMessage()));
            Logger.severe("Error in EditCommand", e);
            return true;
        }
    }

    private void displayAvailableActions(CommandSender sender) {
        getMessageManager().sendMessage(sender, "edit.general.actions.header");
        getMessageManager().sendMessage(sender, "edit.general.actions.available");

        String[] actions = {
            "addline", "editline", "removeline",
            "set_scale", "set_alignment", "set_billboard", "set_seethrough", "set_shadow", "set_linewidth",
            "set_defaultbackground", "set_background", "set_shadowstrength", "set_shadowradius", "set_opacity",
            "set_leftrotation", "set_rightrotation", "set_brightness", "set_viewrange", "set_permission",
            "set_offset_x", "set_offset_y", "set_offset_z"
        };

        for (String actionName : actions) {
            getMessageManager().sendMessage(sender, "edit.general.actions.item", Map.of("action_name", actionName));
        }

        getMessageManager().sendMessage(sender, "edit.general.actions.footer");
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return tabCompleteUtil.getSubCommandCompletions(sender, this, args);
    }
}

