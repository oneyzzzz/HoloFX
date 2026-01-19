package org.oneyz.holoFX.interfaces.tabcomplete;

import org.oneyz.holoFX.enums.TabCompleteType;

import java.lang.annotation.*;

/**
 * Annotation for a single tab complete argument configuration
 * Use in arrays within @TabComplete annotation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TabCompleteConfig {

    /**
     * Argument position (0-indexed)
     * For command: /holo edit hologramName action
     * Position 0 = hologramName
     * Position 1 = action
     */
    int position();

    /**
     * Type of tab completion
     */
    TabCompleteType type();

    /**
     * Static suggestions (used with STATIC type)
     * Example: {"left", "right", "center"}
     */
    String[] suggestions() default {};
}

