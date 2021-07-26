package edu.wpi.grip.annotation.operation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static edu.wpi.grip.annotation.operation.OperationCategory.MISCELLANEOUS;


/**
 * Annotates an {@code Operation} subclass to describe it. This annotation gets transformed into a
 * {@code OperationDescription}. All operation classes with this annotation will be automatically
 * discovered and added to the palette at startup.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {

  /**
   * The name of the operation being described.
   *
   * @return the name of the operation
   */
  String name();

  /**
   * A brief summary of the operation. In-depth descriptions, usage guides, and examples
   * should be on the wiki, not here.
   *
   * @return a summary of the operation
   */
  String summary();

  /**
   * The category the operation belongs to. Defaults to
   * {@link OperationCategory#MISCELLANEOUS MISCELLANEOUS}.
   *
   * @return the category to which the operation belongs
   */
  OperationCategory category() default MISCELLANEOUS;

  /**
   * All known aliases of the operation. If the name of the operation changes, the previous name
   * should be here. Defaults to an empty array.
   *
   * @return known aliases
   */
  String[] aliases() default {};

  /**
   * The name of the icon to use to display the operation. If empty ({@code ""}), no icon will be
   * shown. The icon should be located in {@code /edu/wpi/grip/ui/icons/}.
   *
   * @return the name of the icon
   */
  String iconName() default "";

}
