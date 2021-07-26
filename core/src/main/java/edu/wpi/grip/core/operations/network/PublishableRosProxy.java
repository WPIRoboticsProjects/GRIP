package edu.wpi.grip.core.operations.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link edu.wpi.grip.core.operations.network.ros.JavaToMessageConverter
 * JavaToMessageConverter} field as a proxy for a non-ROS-publishable type.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PublishableRosProxy {

  /**
   * The type the marked converter is a proxy for.
   */
  Class value();

}
