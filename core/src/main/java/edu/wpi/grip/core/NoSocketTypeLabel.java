package edu.wpi.grip.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * By default, the GUI shows labels for the type of each socket based on {@link Class#getSimpleName()}.  This is useful
 * as a hint to the user of what connections would be valid (type-safe) to make.  However, for some types this
 * information is kind of useless, since the type is either named almost exactly the same as the identifier (for
 * example, MySettingsEnum/MySettings), or the type is just so long that it gets cut off in the UI and it's probably
 * just best to not include it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NoSocketTypeLabel {
}
