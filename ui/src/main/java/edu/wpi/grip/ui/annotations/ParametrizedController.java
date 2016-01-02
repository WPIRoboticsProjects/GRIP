package edu.wpi.grip.ui.annotations;

import java.lang.annotation.*;

/**
 * Tag a JavaFX controller that needs to use assisted injection to instantiate the fields.
 * This class is used by the {@link edu.wpi.grip.ui.GRIPUIModule} to load the FXML fields into the class
 * at runtime. The URL should be in the same package in the resources folder as the class that this
 * is annotating.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Documented
public @interface ParametrizedController {
    String url();
}
