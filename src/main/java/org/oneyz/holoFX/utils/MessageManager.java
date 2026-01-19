package org.oneyz.holoFX.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.oneyz.holoFX.HoloFX;

import java.io.File;
import java.util.Map;


public class MessageManager {

    private final HoloFX plugin;
    private FileConfiguration messages;
    private String prefix;

    public MessageManager(HoloFX plugin) {
        this.plugin = plugin;
        loadMessages();
    }


    public void loadMessages() {
        try {
            File messagesFile = new File(plugin.getDataFolder(), "messages.yml");

            if (!messagesFile.exists()) {
                plugin.saveResource("messages.yml", false);
            }

            messages = YamlConfiguration.loadConfiguration(messagesFile);
            prefix = messages.getString("prefix", "&b&lHolo&f&lFX &8&l| &r");

            Logger.info("Messages loaded successfully from messages.yml");
        } catch (Exception e) {
            Logger.severe("Error loading messages.yml", e);
            messages = new YamlConfiguration();
            prefix = "&b&lHolo&f&lFX &8&l| &r";
        }
    }


    public String getMessage(String path) {
        String message = messages.getString("commands." + path, path);
        return colorize(prefix + message);
    }

    public String getMessage(String path, Map<String, String> replacements) {
        String message = messages.getString("commands." + path, path);

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return colorize(prefix + message);
    }


    public String getGeneralMessage(String path) {
        String message = messages.getString("general." + path, path);
        return colorize(prefix + message);
    }

    public String getGeneralMessage(String path, Map<String, String> replacements) {
        String message = messages.getString("general." + path, path);

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return colorize(prefix + message);
    }


    public String getRawMessage(String path) {
        String message = messages.getString(path, path);
        return colorize(message);
    }

    public String getRawMessage(String path, Map<String, String> replacements) {
        String message = messages.getString(path, path);

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return colorize(message);
    }

    public void sendMessage(CommandSender sender, String path) {
        String message = getMessage(path);
        sender.sendMessage(message);
    }

    public void sendMessage(CommandSender sender, String path, Map<String, String> replacements) {
        String message = getMessage(path, replacements);
        sender.sendMessage(message);
    }

    public void sendRawMessage(CommandSender sender, String path) {
        String message = getRawMessage(path);
        sender.sendMessage(message);
    }

    public void sendRawMessage(CommandSender sender, String path, Map<String, String> replacements) {
        String message = getRawMessage(path, replacements);
        sender.sendMessage(message);
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public String getPrefix() {
        return colorize(prefix);
    }
}

