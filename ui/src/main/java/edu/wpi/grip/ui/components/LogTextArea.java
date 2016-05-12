package edu.wpi.grip.ui.components;

import com.google.common.annotations.VisibleForTesting;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextArea;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A text area that is used to display a log.
 * Implements the ability to pause scrolling and the protection of not
 * causing the JavaFX thread to hang if the string becomes too long.
 *
 * @see <a href="http://stackoverflow.com/a/27747004/3708426">Original idea for this class</a>
 */
public class LogTextArea extends TextArea {
    @VisibleForTesting
    static final int MAX_STRING_LENGTH = 200000;
    private final BooleanProperty pausedScrollProperty = new SimpleBooleanProperty(false);
    @SuppressWarnings("PMD.AvoidStringBufferField")
    private StringBuilder fullLog = new StringBuilder();
    private boolean full = false;


    /**
     * Adds a line to the log. This will keep track of the scroll position and maintain
     * the scroll position if scrolling is paused.
     *
     * @param data The line to add to the text area.
     */
    public void addLineToLog(String data) {
        checkNotNull(data, "Data cannot be null");
        if (fullLog.length() + data.length() >= MAX_STRING_LENGTH && !full) {
            full = true;
            fullLog.append("[ERROR] Too much output to display. Discarding the rest.");
        } else if (!full) {
            fullLog.append(data + "\n");
        } else {
            return;
        }
        final double scrollPosition;
        if (isPausedScroll()) {
            scrollPosition = this.getScrollTop();
        } else {
            scrollPosition = Double.MAX_VALUE;
        }
        this.setText(fullLog.toString());
        this.setScrollTop(scrollPosition);
    }

    /**
     * @return True if the scroll has been paused
     */
    public final boolean isPausedScroll() {
        return pausedScrollProperty.getValue();
    }

    /**
     * @return A property that can be bound to prevent auto scrolling
     */
    public final BooleanProperty pausedScrollProperty() {
        return pausedScrollProperty;
    }

    public final void setPausedScroll(boolean value) {
        pausedScrollProperty.setValue(value);
    }

    @Override
    public void clear() {
        fullLog = new StringBuilder();
        full = false;
    }
}
