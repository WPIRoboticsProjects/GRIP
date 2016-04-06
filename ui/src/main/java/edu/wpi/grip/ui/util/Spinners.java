package edu.wpi.grip.ui.util;


import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Optional;

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
     *
     * @param defaultValue The value to use if nothing is entered
     * @param <T>          The number type of the spinner
     */
    @SuppressWarnings("PMD.IfElseStmtsMustUseBraces")
    public static <T extends Number> void makeEditableSafely(Spinner<T> spinner, NumberFormat format, T defaultValue) {
        spinner.setEditable(true);

        TextField editor = spinner.getEditor();

        // Override the converter used to interpret editor values.  If there's an error, we want to set the default
        // value instead of throwing an exception.
        StringConverter<T> oldConverter = spinner.getValueFactory().getConverter();
        spinner.getValueFactory().setConverter(new StringConverter<T>() {
            @Override
            public String toString(T t) {
                return oldConverter.toString(t);
            }

            @Override
            public T fromString(String s) {
                return Optional.ofNullable(oldConverter.fromString(s)).orElse(defaultValue);
            }
        });

        // Commit the spinner value when the editor loses focus
        editor.focusedProperty().addListener((s, ov, focused) -> {
            if (!focused) {
                SpinnerValueFactory<T> valueFactory = spinner.getValueFactory();
                StringConverter<T> converter = valueFactory.getConverter();
                valueFactory.setValue(converter.fromString(editor.getText()));
            }
        });

        // Filter out invalid editor changes
        editor.setTextFormatter(new TextFormatter<T>(change -> {
            if (change.isContentChange() && !change.getControlNewText().isEmpty()) {
                ParsePosition position = new ParsePosition(0);
                format.parse(change.getControlNewText(), position);

                if (position.getIndex() == 0 || position.getIndex() < change.getControlNewText().length()) {
                    return null;
                }
            }

            return change;
        }));
    }
}
