package org.oneyz.holoFX.interfaces.tabcomplete;

import java.lang.annotation.*;

/**
 * Annotation for tab complete configurations on commands
 * Example usage:
 *
 * @CommandInfo(...)
 * @TabComplete({
 *     @TabCompleteConfig(position = 0, type = TabCompleteType.HOLOGRAM_LIST),
 *     @TabCompleteConfig(position = 1, type = TabCompleteType.EDIT_ACTIONS)
 * })
 * public class EditCommand implements SubCommand { ... }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TabComplete {

    /**
     * Array of tab complete configurations for each argument position
     */
    TabCompleteConfig[] value();
}

