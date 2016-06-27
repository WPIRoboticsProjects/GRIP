package edu.wpi.grip.core.operations.network;

import edu.wpi.grip.core.operations.composite.ContoursReport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for accessors of values that should be published to a NetworkProtocol. Using
 * annotations to define these methods instead of having the class implement a method in an
 * interface allows an object (such as {@link ContoursReport} to have arbitrarily many publishable
 * values.  More values (like x, y, width, height, etc...) can be added simply by adding more
 * accessors with this annotation. One potential way of having multiple values without using
 * annotations might be to make the interface method return a list, but this would prevent us from
 * knowing how many values there are and what their names are without having an instance of the
 * object being published. The weight of each accessor must be specified.  This determines order of
 * the inputs to {@link PublishAnnotatedOperation}. It's important to specify weights if there are
 * multiple keys because otherwise, different JVMs will return them in different orders, leading to
 * projects that are interpreted differently on different machines.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PublishValue {
  /**
   * @return The key that will be displayed as the name of the field to be published.
   */
  String key() default "";

  /**
   * @return The weight that will order the sockets by.
   */
  int weight();
}
