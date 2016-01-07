package edu.wpi.grip.core.operations.networktables;

import edu.wpi.grip.core.operations.composite.ContoursReport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for accessors of values that should be published to NetworkTables.
 * <p>
 * Using annotations to define these methods instead of having the class implement a method in an interface allows an
 * object (such as {@link ContoursReport} to have arbitrarily many publishable values.  More values (like x, y, width,
 * height, etc...) can be added simply by adding more accessors with this annotation.
 * <p>
 * One potential way of having multiple values without using annotations might be to make the interface method return
 * a list, but this would prevent us from knowing how many values there are and what their names are without having
 * an instance of the object being published.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NTValue {
    String key();
}
