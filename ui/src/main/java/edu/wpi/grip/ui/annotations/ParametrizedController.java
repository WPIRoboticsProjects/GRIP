package edu.wpi.grip.ui.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface ParametrizedController {
    String url();
}
