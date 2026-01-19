package org.oneyz.holoFX.utils;

import org.bukkit.command.CommandSender;
import org.oneyz.holoFX.HoloFX;
import java.util.logging.Level;

/**
 * Utility class for simplified logging without needing to call plugin.getLogger() repeatedly.
 * Uses java.util.logging.Logger from the plugin instance.
 */
public class Logger {

    private static java.util.logging.Logger logger;

    /**
     * Initialize the logger with the plugin instance.
     * Must be called during plugin startup.
     */
    public static void init(HoloFX plugin) {
        logger = plugin.getLogger();
    }

    /**
     * Log an info message.
     */
    public static void info(String message) {
        if (logger != null) {
            logger.log(Level.INFO, message);
        }
    }

    /**
     * Log a warning message.
     */
    public static void warning(String message) {
        if (logger != null) {
            logger.log(Level.WARNING, message);
        }
    }

    /**
     * Log a severe (error) message.
     */
    public static void severe(String message) {
        if (logger != null) {
            logger.log(Level.SEVERE, message);
        }
    }

    /**
     * Log a severe message with an exception.
     */
    public static void severe(String message, Throwable throwable) {
        if (logger != null) {
            logger.log(Level.SEVERE, message, throwable);
        }
    }

    /**
     * Log a fine debug message.
     */
    public static void fine(String message) {
        if (logger != null) {
            logger.log(Level.FINE, message);
        }
    }

}

