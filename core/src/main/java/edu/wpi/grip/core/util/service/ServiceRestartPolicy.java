package edu.wpi.grip.core.util.service;

/**
 *
 * @see <a href="https://gist.github.com/vladdu/b8af7709e26206b1832b">Original version</a>
 */
@FunctionalInterface
public interface ServiceRestartPolicy {

    /**
     * Policy might want to keep track of when the latest restarts have
     * happened.
     */
    default void notifyRestart() {
        /* no-op */
    }

     boolean shouldRestart();

}
