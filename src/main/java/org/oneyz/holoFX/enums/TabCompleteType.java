package org.oneyz.holoFX.enums;

/**
 * Enum for different types of tab completions
 */
public enum TabCompleteType {
    /**
     * List of all loaded holograms
     */
    HOLOGRAM_LIST,

    /**
     * List of edit actions (set_scale, set_shadow, etc.)
     */
    EDIT_ACTIONS,

    /**
     * Line numbers for a specific hologram
     */
    LINE_NUMBERS,

    /**
     * Static suggestions provided in annotation params
     */
    STATIC,

    /**
     * Custom - will be handled by command implementation
     */
    CUSTOM
}

