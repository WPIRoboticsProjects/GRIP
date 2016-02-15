package edu.wpi.grip.core.util;

/**
 * An enum that indicates if GRIP is running in GUI mode with JavaFX or as a headless command line application.
 * <p>
 * To the get the mode, this can be injected into a class (ie: @Inject private GRIPMode mode;)
 */
public enum GRIPMode {
    GUI, HEADLESS
}
