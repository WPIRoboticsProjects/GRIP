package edu.wpi.grip.ui.util;

import edu.wpi.grip.ui.annotations.ParametrizedController;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public final class TestAnnotationFXMLLoader {

    private TestAnnotationFXMLLoader() { /* no-op */ }

    public static <T> T load(Object annotatedController) {
        try {
            return FXMLLoader.<T>load(annotatedController.getClass().getResource(
                    annotatedController.getClass().getAnnotation(ParametrizedController.class).url()),
                    null, null,
                    c -> annotatedController
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load FXML", e);
        }
    }
}
