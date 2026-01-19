package org.oneyz.holoFX.interfaces.tabcomplete;

import java.lang.annotation.*;

/**
 * Annotation for tab complete configurations on edit subcommands
 * Similar to @TabComplete

 * Example usage:

 * @TabCompleteEdit({
 *     @TabCompleteConfig(position = 0, type = TabCompleteType.STATIC, suggestions = {"Przykładowy tekst nowej linii"})
 * })


 * public class AddLineEditCommand implements EditSubCommand { ... }

 * For /holo edit <hologramName> addline <position 0 gets suggestions here>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TabCompleteEdit {

    /**
     * Array of tab complete configurations for each argument position
     * Note: Position 0 in args[] for EditSubCommand corresponds to the first argument
     * after the action name in the full command
     */
    TabCompleteConfig[] value();
}

