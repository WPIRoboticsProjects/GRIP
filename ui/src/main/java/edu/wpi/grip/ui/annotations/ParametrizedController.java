package edu.wpi.grip.ui.annotations;

import edu.wpi.grip.ui.GripUiModule;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tag a JavaFX controller that needs to use assisted injection to instantiate the fields. This
 * class is used by the {@link GripUiModule} to load the FXML fields into the class at runtime. The
 * URL should be in the same package in the resources folder as the class that this is annotating.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Documented
public @interface ParametrizedController {
  /**
   * @return The name of the fxml file to be loaded as the controller.
   */
  String url();
}
