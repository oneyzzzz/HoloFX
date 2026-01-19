package org.oneyz.holoFX.tabcomplete;

import org.bukkit.command.CommandSender;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.enums.TabCompleteType;

import java.util.*;

/**
 * Provider for tab completions based on configuration
 * Handles different types of tab completions (hologram list, actions, etc.)
 */
public class TabCompleteProvider {

    private final HoloFX plugin;

    public TabCompleteProvider(HoloFX plugin) {
        this.plugin = plugin;
    }

    /**
     * Get tab completions based on type
     *
     * @param sender The command sender
     * @param type The tab completion type
     * @param suggestions Static suggestions (if type is STATIC)
     * @param args Current command arguments
     * @return List of suggestions
     */
    public List<String> getCompletions(CommandSender sender, TabCompleteType type, String[] suggestions, String[] args) {
        return getCompletions(sender, type, suggestions, args, null);
    }

    /**
     * Get tab completions based on type with context
     *
     * @param sender The command sender
     * @param type The tab completion type
     * @param suggestions Static suggestions (if type is STATIC)
     * @param args Current command arguments
     * @param context Additional context (e.g., hologramName for LINE_NUMBERS)
     * @return List of suggestions
     */
    public List<String> getCompletions(CommandSender sender, TabCompleteType type, String[] suggestions, String[] args, String context) {
        return switch (type) {
            case HOLOGRAM_LIST -> getHologramList(args);
            case EDIT_ACTIONS -> getEditActions(args);
            case LINE_NUMBERS -> getLineNumbers(args, context);
            case STATIC -> getStaticSuggestions(suggestions, args);
            case CUSTOM -> new ArrayList<>();
        };
    }

    /**
     * Get list of all hologram names
     */
    private List<String> getHologramList(String[] args) {
        List<String> completions = new ArrayList<>();
        String partial = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        plugin.getHologramDisplayManager().getAllActiveHolograms().keySet().forEach(name -> {
            if (name.toLowerCase().startsWith(partial)) {
                completions.add(name);
            }
        });

        return completions;
    }

    /**
     * Get list of edit actions
     */
    private List<String> getEditActions(String[] args) {
        List<String> actions = new ArrayList<>(Arrays.asList(
                "addline", "editline", "removeline",
                "set_scale", "set_offset_x", "set_offset_y", "set_offset_z",
                "set_shadow", "set_shadowradius", "set_shadowstrength",
                "set_opacity", "set_linewidth", "set_alignment", "set_billboard",
                "set_background", "set_defaultbackground",
                "set_seethrough", "set_viewrange", "set_brightness",
                "set_permission", "set_leftrotation", "set_rightrotation"
        ));

        String partial = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
        List<String> filtered = new ArrayList<>();

        for (String action : actions) {
            if (action.startsWith(partial)) {
                filtered.add(action);
            }
        }

        return filtered;
    }

    /**
     * Get line numbers for a specific hologram
     */
    private List<String> getLineNumbers(String[] args, String hologramName) {
        List<String> completions = new ArrayList<>();
        if (hologramName == null || hologramName.isEmpty()) {
            if (args.length < 1) {
                return completions;
            }
            hologramName = args[0];
        }

        var hologram = plugin.getHologramLoader().getHologram(hologramName);

        if (hologram != null) {
            int lineCount = hologram.getLines().size();
            String partial = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

            for (int i = 1; i <= lineCount; i++) {
                String lineNum = String.valueOf(i);
                if (lineNum.startsWith(partial)) {
                    completions.add(lineNum);
                }
            }
        }

        return completions;
    }

    /**
     * Get static suggestions
     */
    private List<String> getStaticSuggestions(String[] suggestions, String[] args) {
        if (suggestions == null || suggestions.length == 0) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();
        String partial = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(partial)) {
                completions.add(suggestion);
            }
        }

        return completions;
    }
}

