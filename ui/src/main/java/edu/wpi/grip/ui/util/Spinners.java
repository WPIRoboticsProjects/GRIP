package edu.wpi.grip.ui.util;


import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

import java.util.function.Function;

/**
 * Utility methods to set up spinners in a safe way.
 */
public final class Spinners {

    private Spinners() {
        /* no-op */
    }

    /**
     * Makes the spinner editable.
     * Ensures that the values will be committed when focus is lost.
     * Ensures that the only values that the spinner can accept are parable into whatever number type they represent.
     * @param <T> The number type of the spinner
     */
    @SuppressWarnings("PMD.IfElseStmtsMustUseBraces")
    public static <T extends Number> void makeEditableSafely(Spinner<T> spinner, Function<String, T> parseToNumber) {
        spinner.setEditable(true);
        spinner.focusedProperty().addListener((s, ov, nv) -> {
            if (nv) return;
            commitEditorText(spinner);
        });
        // Ensure the value entered is only a number
        spinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if ("".equals(newValue)) {
                spinner.getEditor().setText("0");
            } else try {
                T value = parseToNumber.apply(newValue);
                spinner.getEditor().setText(value.toString());
            } catch (NumberFormatException e) {
                spinner.getEditor().setText(oldValue);
            }
        });
    }

    /**
     *
     * @see <a href="http://stackoverflow.com/questions/32340476/manually-typing-in-text-in-javafx-spinner-is-not-updating-the-value-unless-user">Stack Overflow Post</a>
     */
    private static <T> void commitEditorText(Spinner<T> spinner) {
        if (!spinner.isEditable()) return;
        String text = spinner.getEditor().getText();
        SpinnerValueFactory<T> valueFactory = spinner.getValueFactory();
        if (valueFactory != null) {
            StringConverter<T> converter = valueFactory.getConverter();
            if (converter != null) {
                T value = converter.fromString(text);
                valueFactory.setValue(value);
            }
        }
    }
}
