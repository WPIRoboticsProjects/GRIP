package edu.wpi.grip.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static edu.wpi.grip.core.OperationDescription.Category;
import static edu.wpi.grip.core.OperationDescription.Category.MISCELLANEOUS;

/**
 * Annotates an {@link Operation} subclass to describe it. This annotation gets transformed into a
 * {@link OperationDescription}. All operation classes with this annotation will be automatically
 * discovered and added to the palette at startup.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {

  /**
   * The name of the operation being described.
   */
  String name();

  /**
   * A brief summary of the operation. In-depth descriptions, usage guides, and examples
   * should be on the wiki, not here.
   */
  String summary();

  /**
   * The category the operation belongs to. Defaults to
   * {@link OperationDescription.Category#MISCELLANEOUS MISCELLANEOUS}.
   */
  Category category() default MISCELLANEOUS;

  /**
   * All known aliases of the operation. If the name of the operation changes, the previous name
   * should be here. Defaults to an empty array.
   */
  String[] aliases() default {};

  /**
   * The name of the icon to use to display the operation. If empty ({@code ""}), no icon will be
   * shown. The icon should be located in {@code /edu/wpi/grip/ui/icons/}.
   */
  String iconName() default "";

}
