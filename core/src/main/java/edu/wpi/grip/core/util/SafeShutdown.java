package edu.wpi.grip.core.util;

import javax.annotation.Nullable;

/**
 * This class should be used to shutdown GRIP safely.
 * This is because shutdown hooks may throw exceptions, as such
 * we need to know if the application being is shutdown.
 */
public final class SafeShutdown {

    public static volatile boolean stopping = false;

    static {
        /*
         * Shutdown hook run order is non-deterministic but this increases our likelihood of flagging a shutdown
         * that we can't control.
         */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                SafeShutdown.stopping = true;
            }
        });
    }

    /**
     * Shutdown's the VM in such a way that flags that the vm is stopping.
     * This is so that we don't run the normal exception handling code when
     * shutting down the application.
     *
     * @param statusCode exit status.
     * @param hook       The hook to run before the System shutdown.
     *                   This will be run after stopping has been flagged true.
     *                   This is nullable.
     * @see System#exit(int)
     */
    public static final void exit(int statusCode, @Nullable PreSystemExitHook hook) {
        flagStopping();
        try {
            if (hook != null) {
                hook.run();
            }
        } finally {
            System.exit(statusCode);
        }

    }

    /**
     * Helper method that passes null as the PreSystemExitHook.
     *
     * @param statusCode exit status.
     * @see #exit(int)
     */
    public static final void exit(int statusCode) {
        exit(statusCode, null);
    }


    /**
     * HACK!
     * Shutdown hooks can throw exceptions.
     * On Windows, the static method after {@link org.bytedeco.javacpp.Loader#loadLibrary} throws such an exception
     * in a shutdown hook.
     *
     * @return True if if the application is shutting down.
     * @see <a href="https://github.com/WPIRoboticsProjects/GRIP/issues/297">GRIP Issue</a>
     * @see <a href="https://github.com/bytedeco/javacpp/issues/60">Bytedeco issue</a>
     */
    public static final boolean isStopping() {
        return stopping;
    }

    /**
     * Flags that the application is shutting down.
     */
    public static void flagStopping() {
        stopping = true;
    }

    @FunctionalInterface
    public interface PreSystemExitHook {
        void run();
    }
}
