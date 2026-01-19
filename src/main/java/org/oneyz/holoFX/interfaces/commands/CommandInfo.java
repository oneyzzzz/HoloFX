package org.oneyz.holoFX.interfaces.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for command metadata.
 * Used to automatically register and configure subcommands.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {

    /**
     * Permission required to execute this command
     */
    String permission() default "";

    /**
     * The subcommand name (e.g., "create" for "/holo create")
     */
    String commandName();

    /**
     * Usage description (e.g., "/holo create <holo_name> <text>")
     */
    String usage() default "";

    /**
     * Whether only players can execute this command (not console)
     */
    boolean onlyPlayer() default false;

    /**
     * Description of what this command does
     * Can be set directly as a string
     */
    String description() default "";

    /**
     * Path to description in messages.yml (e.g., "commands.create.description")
     * If set, will override the description field
     * Uses MessageManager.getRawMessage(descriptionPath)
     */
    String descriptionPath() default "";

    /**
     * Aliases for this subcommand
     */
    String[] aliases() default {};

}

