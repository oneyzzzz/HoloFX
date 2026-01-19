package org.oneyz.holoFX.interfaces.hologram;

/**
 * Interface for hologram edit operations that will be queued
 */
public interface HologramEditOperation {
    /**
     * Execute the operation
     *
     * @return true if operation was successful
     */
    boolean execute();

    /**
     * Get a description of this operation
     */
    String getDescription();
}

