package edu.wpi.grip.core.operations.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as being a publishable proxy for a non-publishable type, like
 * {@code BooleanPublishable} for {@code Boolean}. This annotation only has an affect when placed
 * on a public, concrete, top-level class that implements {@link Publishable}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PublishableProxy {

  /**
   * The type(s) that the marked class is an publishable version of. The marked class must have
   * a copy constructor for each type.
   *
   * <p>These types should use boxed classes instead of primitive types (e.g. {@code Integer.class}
   * instead of {@code int.class}).
   */
  Class[] value();

}
