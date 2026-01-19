package org.oneyz.holoFX.utils;

import org.oneyz.holoFX.interfaces.hologram.HologramEditOperation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Per-hologram queue system to prevent race conditions during concurrent edits.
 * Ensures that only one operation is executed at a time per hologram.
 */
public class HologramEditQueueManager {

    private final Map<String, HologramOperationQueue> queues = new HashMap<>();

    /**
     * Execute an operation for a hologram using the queue system
     *
     * @param hologramName The name of the hologram
     * @param operation The operation to execute
     * @return true if operation was successful
     */
    public boolean executeOperation(String hologramName, HologramEditOperation operation) {
        HologramOperationQueue queue = getOrCreateQueue(hologramName);
        return queue.enqueueAndExecute(operation);
    }

    /**
     * Get or create a queue for a specific hologram
     */
    private synchronized HologramOperationQueue getOrCreateQueue(String hologramName) {
        return queues.computeIfAbsent(hologramName, k -> new HologramOperationQueue(hologramName));
    }

    /**
     * Get the pending operations count for a hologram
     */
    public int getPendingOperations(String hologramName) {
        HologramOperationQueue queue = queues.get(hologramName);
        return queue != null ? queue.getPendingCount() : 0;
    }

    /**
     * Check if a hologram is currently being edited
     */
    public boolean isBeingEdited(String hologramName) {
        HologramOperationQueue queue = queues.get(hologramName);
        return queue != null && queue.isProcessing();
    }

    /**
     * Clear queue for a hologram (use when hologram is deleted)
     */
    public void clearQueue(String hologramName) {
        queues.remove(hologramName);
    }

    /**
     * Internal queue class for managing operations per hologram
     */
    private static class HologramOperationQueue {
        private final String hologramName;
        private final Queue<HologramEditOperation> operationQueue = new LinkedList<>();
        private final ReentrantLock lock = new ReentrantLock();
        private boolean isProcessing = false;

        HologramOperationQueue(String hologramName) {
            this.hologramName = hologramName;
        }

        /**
         * Enqueue and try to execute operation immediately
         */
        boolean enqueueAndExecute(HologramEditOperation operation) {
            lock.lock();
            try {
                operationQueue.add(operation);

                if (!isProcessing) {
                    return executeNext();
                }

                return true;
            } finally {
                lock.unlock();
            }
        }

        /**
         * Execute next operation from queue
         */
        private boolean executeNext() {
            if (operationQueue.isEmpty()) {
                isProcessing = false;
                return true;
            }

            try {
                isProcessing = true;
                HologramEditOperation operation = operationQueue.poll();

                if (operation != null) {
                    boolean result = operation.execute();
                    if (!operationQueue.isEmpty()) {
                        return executeNext();
                    }

                    isProcessing = false;
                    return result;
                }

                isProcessing = false;
                return true;
            } catch (Exception e) {
                Logger.severe("Error executing hologram operation for '" + hologramName + "': " + e.getMessage(), e);
                isProcessing = false;
                operationQueue.clear();
                return false;
            }
        }

        /**
         * Get number of pending operations
         */
        int getPendingCount() {
            lock.lock();
            try {
                return operationQueue.size();
            } finally {
                lock.unlock();
            }
        }

        /**
         * Check if currently processing
         */
        boolean isProcessing() {
            lock.lock();
            try {
                return isProcessing;
            } finally {
                lock.unlock();
            }
        }
    }
}

