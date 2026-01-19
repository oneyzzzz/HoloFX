package org.oneyz.holoFX.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.Material;
import org.oneyz.holoFX.HoloFX;
import org.oneyz.holoFX.enums.TabCompleteType;
import org.oneyz.holoFX.interfaces.commands.CommandInfo;
import org.oneyz.holoFX.interfaces.commands.SubCommand;
import org.oneyz.holoFX.interfaces.tabcomplete.TabComplete;
import org.oneyz.holoFX.interfaces.tabcomplete.TabCompleteConfig;
import org.oneyz.holoFX.models.Hologram;
import org.oneyz.holoFX.tabcomplete.TabCompleteUtil;
import org.oneyz.holoFX.utils.Logger;
import org.oneyz.holoFX.utils.MessageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommandInfo(
        commandName = "settings",
        permission = "holo.settings",
        usage = "holo settings <hologram_name> [line_number]",
        descriptionPath = "descriptions.settings",
        aliases = {"config", "c"},
        onlyPlayer = true
)

@TabComplete({
        @TabCompleteConfig(position = 0, type = TabCompleteType.HOLOGRAM_LIST),
        @TabCompleteConfig(position = 1, type = TabCompleteType.LINE_NUMBERS)
})
public class SettingsCommand implements SubCommand {

    private final HoloFX plugin;
    private final TabCompleteUtil tabCompleteUtil;

    public SettingsCommand(HoloFX plugin) {
        this.plugin = plugin;
        this.tabCompleteUtil = new TabCompleteUtil(plugin);
    }

    @Override
    public MessageManager getMessageManager() {
        return plugin.getMessageManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            return false;
        }

        Player player = (Player) sender;
        String hologramName = args[0];

        Hologram hologram = plugin.getHologramLoader().getHologram(hologramName);
        if (hologram == null) {
            getMessageManager().sendMessage(sender, "settings.not_found", Map.of("name", hologramName));
            return true;
        }

        if (!hologram.hasValidWorld()) {
            getMessageManager().sendMessage(sender, "settings.invalid_world", Map.of("name", hologramName));
            return true;
        }

        try {
            boolean isLineSpecific = args.length > 1 && args[1].matches("\\d+");
            int lineNumber = -1;

            if (isLineSpecific) {
                lineNumber = Integer.parseInt(args[1]) - 1;
                if (lineNumber < 0 || lineNumber >= hologram.getLines().size()) {
                    getMessageManager().sendMessage(sender, "settings.line_out_of_range",
                        Map.of("line_count", String.valueOf(hologram.getLines().size())));
                    return true;
                }
            }

            ItemStack book = createSettingsBook(hologram, lineNumber);
            player.getInventory().addItem(book);
            getMessageManager().sendMessage(sender, "settings.book_added");
            return true;

        } catch (Exception e) {
            Logger.severe("Error displaying hologram settings: " + hologramName, e);
            getMessageManager().sendMessage(sender, "settings.error", Map.of("error", e.getMessage()));
            return true;
        }
    }

    /**
     * Create a written book with hologram settings
     *
     * @param hologram The hologram to display settings for
     * @param lineNumber The specific line to show
     * @return ItemStack representing the written book
     */
    private ItemStack createSettingsBook(Hologram hologram, int lineNumber) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();

        if (bookMeta == null) {
            return book;
        }

        bookMeta.setTitle("§c§lHoloFX Settings");
        bookMeta.setAuthor("HoloFX");

        List<String> pages = new ArrayList<>();

        if (lineNumber >= 0) {
            Hologram.Line line = hologram.getLines().get(lineNumber);
            pages.add(createLineTextPage(hologram, line, lineNumber + 1));
            pages.add(createLineSettingsPage(hologram, line, lineNumber + 1));
        } else {
            for (int i = 0; i < hologram.getLines().size(); i++) {
                Hologram.Line line = hologram.getLines().get(i);
                pages.add(createLineTextPage(hologram, line, i + 1));
                pages.add(createLineSettingsPage(hologram, line, i + 1));
            }
        }

        for (String pageContent : pages) {
            bookMeta.addPage(pageContent);
        }

        book.setItemMeta(bookMeta);
        return book;
    }

    /**
     * Create a page for the text content of a hologram line
     *
     * @param hologram The parent hologram
     * @param line The hologram line
     * @param lineNumber The line number (1-based)
     * @return The formatted page content
     */
    private String createLineTextPage(Hologram hologram, Hologram.Line line, int lineNumber) {
        StringBuilder page = new StringBuilder();

        page.append(getMessageManager().getRawMessage("commands.settings.header")).append("\n");
        page.append(getMessageManager().getRawMessage("commands.settings.hologram_name", Map.of("name", hologram.getName()))).append("\n");
        page.append(getMessageManager().getRawMessage("commands.settings.line_number", Map.of("line", String.valueOf(lineNumber)))).append("\n");
        page.append(getMessageManager().getRawMessage("commands.settings.header")).append("\n\n");

        page.append(getMessageManager().getRawMessage("commands.settings.text_header")).append("\n");
        String textContent = line.getFirstLine();
        if (textContent.length() > 35) {
            page.append(getMessageManager().getRawMessage("commands.settings.text_content", Map.of("text", wrapText(textContent, 35)))).append("\n\n");
        } else {
            page.append(getMessageManager().getRawMessage("commands.settings.text_content", Map.of("text", textContent))).append("\n\n");
        }

        Hologram.Offset offset = line.getOffset();
        page.append(getMessageManager().getRawMessage("commands.settings.offset_header")).append("\n");
        page.append(getMessageManager().getRawMessage("commands.settings.offset_x", Map.of("value", formatDouble(offset.getX())))).append("\n");
        page.append(getMessageManager().getRawMessage("commands.settings.offset_y", Map.of("value", formatDouble(offset.getY())))).append("\n");
        page.append(getMessageManager().getRawMessage("commands.settings.offset_z", Map.of("value", formatDouble(offset.getZ())))).append("\n");

        return page.toString();
    }

    /**
     * Create a page for the settings of a hologram line
     *
     * @param hologram The parent hologram
     * @param line The hologram line
     * @param lineNumber The line number (1-based)
     * @return The formatted page content
     */
    private String createLineSettingsPage(Hologram hologram, Hologram.Line line, int lineNumber) {
        StringBuilder page = new StringBuilder();
        Hologram.DisplaySettings settings = line.getDisplaySettings();

        page.append(getMessageManager().getRawMessage("commands.settings.header")).append("\n");
        page.append(getMessageManager().getRawMessage("commands.settings.settings_header", Map.of("line", String.valueOf(lineNumber)))).append("\n");
        page.append(getMessageManager().getRawMessage("commands.settings.header")).append("\n\n");

        page.append(getMessageManager().getRawMessage("commands.settings.text_opacity",
            Map.of("value", String.valueOf(settings.getTextOpacity())))).append("\n");

        page.append(getMessageManager().getRawMessage("commands.settings.line_width",
            Map.of("value", String.valueOf(settings.getLineWidth())))).append("\n");

        page.append(getMessageManager().getRawMessage("commands.settings.alignment",
            Map.of("value", settings.getTextAlignment()))).append("\n");

        page.append(getMessageManager().getRawMessage("commands.settings.billboard",
            Map.of("value", settings.getBillboard().toString()))).append("\n");

        String shadowValue = settings.isShadow() ?
            getMessageManager().getRawMessage("commands.settings.enabled") :
            getMessageManager().getRawMessage("commands.settings.disabled");
        page.append(getMessageManager().getRawMessage("commands.settings.shadow",
            Map.of("value", shadowValue))).append("\n");

        String seeThroughValue = settings.isSeeThrough() ?
            getMessageManager().getRawMessage("commands.settings.enabled") :
            getMessageManager().getRawMessage("commands.settings.disabled");
        page.append(getMessageManager().getRawMessage("commands.settings.see_through",
            Map.of("value", seeThroughValue))).append("\n");

        if (settings.getShadowRadius() != null) {
            page.append(getMessageManager().getRawMessage("commands.settings.shadow_radius",
                Map.of("value", formatDouble(settings.getShadowRadius())))).append("\n");
        }

        if (settings.getShadowStrength() != null) {
            page.append(getMessageManager().getRawMessage("commands.settings.shadow_strength",
                Map.of("value", formatDouble(settings.getShadowStrength())))).append("\n");
        }

        if (settings.getViewRange() != null) {
            page.append(getMessageManager().getRawMessage("commands.settings.view_range",
                Map.of("value", formatDouble(settings.getViewRange())))).append("\n");
        }

        if (settings.getBackground() != null && !settings.getBackground().isEmpty()) {
            page.append(getMessageManager().getRawMessage("commands.settings.background",
                Map.of("value", settings.getBackground()))).append("\n");
        }

        String defaultBgValue = settings.isDefaultBackground() ?
            getMessageManager().getRawMessage("commands.settings.enabled") :
            getMessageManager().getRawMessage("commands.settings.disabled");
        page.append(getMessageManager().getRawMessage("commands.settings.default_background",
            Map.of("value", defaultBgValue))).append("\n");

        if (settings.getPermission() != null && !settings.getPermission().isEmpty()) {
            page.append(getMessageManager().getRawMessage("commands.settings.permission",
                Map.of("value", settings.getPermission()))).append("\n");
        }

        if (settings.getBrightness() != null) {
            page.append(getMessageManager().getRawMessage("commands.settings.brightness",
                Map.of("value", String.valueOf(settings.getBrightness())))).append("\n");
        }

        if (settings.getScale() != null && !settings.getScale().isEmpty()) {
            String scaleStr = formatFloatList(settings.getScale());
            page.append(getMessageManager().getRawMessage("commands.settings.scale",
                Map.of("value", scaleStr))).append("\n");
        }

        if (settings.getTranslation() != null && !settings.getTranslation().isEmpty()) {
            String translationStr = formatFloatList(settings.getTranslation());
            page.append(getMessageManager().getRawMessage("commands.settings.translation",
                Map.of("value", translationStr))).append("\n");
        }

        if (settings.getLeftRotationQuaternion() != null && !settings.getLeftRotationQuaternion().isEmpty()) {
            String rotationStr = formatFloatList(settings.getLeftRotationQuaternion());
            page.append(getMessageManager().getRawMessage("commands.settings.left_rotation",
                Map.of("value", rotationStr))).append("\n");
        }

        if (settings.getRightRotationQuaternion() != null && !settings.getRightRotationQuaternion().isEmpty()) {
            String rotationStr = formatFloatList(settings.getRightRotationQuaternion());
            page.append(getMessageManager().getRawMessage("commands.settings.right_rotation",
                Map.of("value", rotationStr))).append("\n");
        }

        page.append("\n");
        page.append(getMessageManager().getRawMessage("commands.settings.footer"));

        return page.toString();
    }

    /**
     * Wrap text to a specific width
     *
     * @param text The text to wrap
     * @param width The maximum width per line
     * @return Wrapped text with newlines
     */
    private String wrapText(String text, int width) {
        StringBuilder result = new StringBuilder();
        int currentWidth = 0;

        for (String word : text.split(" ")) {
            if (currentWidth + word.length() + 1 > width) {
                result.append("\n");
                currentWidth = 0;
            }

            if (currentWidth > 0) {
                result.append(" ");
                currentWidth++;
            }

            result.append(word);
            currentWidth += word.length();
        }

        return result.toString();
    }

    /**
     * Format a double value for display
     *
     * @param value The double value
     * @return Formatted string
     */
    private String formatDouble(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        } else {
            return String.format("%.2f", value);
        }
    }

    /**
     * Format a list of floats for display
     *
     * @param values The float list
     * @return Formatted string like [x, y, z]
     */
    private String formatFloatList(List<Float> values) {
        if (values == null || values.isEmpty()) {
            return "N/A";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(", ");
            float val = values.get(i);
            if (val == (long) val) {
                sb.append((long) val);
            } else {
                sb.append(String.format("%.2f", val));
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return tabCompleteUtil.getSubCommandCompletions(sender, this, args);
    }
}

