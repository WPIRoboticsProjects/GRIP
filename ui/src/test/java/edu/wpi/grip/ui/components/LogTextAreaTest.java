package edu.wpi.grip.ui.components;


import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class LogTextAreaTest extends ApplicationTest {
    private LogTextArea logTextArea;

    @Override
    public void start(Stage stage) {
        logTextArea = new LogTextArea();
        stage.setScene(new Scene(logTextArea));
        stage.show();
    }

    @Test
    public void testAddFirstLine() {
        final String FIRST_LINE = "First Line Of Text";
        logTextArea.addLineToLog(FIRST_LINE);
        assertEquals("First line wasn't added", FIRST_LINE + "\n", logTextArea.getText());
    }

    @Test
    public void testAddTwoLines() {
        final String FIRST_LINE = "First Line Of Text";
        final String SECOND_LINE = "Second line of text";
        logTextArea.addLineToLog(FIRST_LINE);
        logTextArea.addLineToLog(SECOND_LINE);
        assertEquals("Second line wasn't added", FIRST_LINE + "\n" + SECOND_LINE + "\n", logTextArea.getText());
    }

    @Test
    public void testAddingReallyLongLineOfText() {
        final String INITIAL_TEXT = StringUtils.rightPad("Initial string", LogTextArea.MAX_STRING_LENGTH - 3 , "Long\n");
        logTextArea.addLineToLog(INITIAL_TEXT);
        assertEquals("Text was not added to logger", INITIAL_TEXT + "\n", logTextArea.getText());
    }

    @Test
    public void testAddingStringThatIsTooLong() {
        final String INITIAL_TEXT = StringUtils.rightPad("Initial string", LogTextArea.MAX_STRING_LENGTH, "Long\n");
        logTextArea.addLineToLog(INITIAL_TEXT);
        assertNotEquals("The initial text should not have been appended as it was too long", INITIAL_TEXT + "\n", logTextArea.getText());
    }

    @Test
    public void testAddingStringToFullLog() {
        testAddingStringThatIsTooLong();
        final String currentText = logTextArea.getText();
        logTextArea.addLineToLog("More logged text");
        assertEquals("This text should not be appended as the log is full", currentText, logTextArea.getText());
    }

    @Test
    public void testScrollIsMaintainedWhenScrollIsPaused() {
        logTextArea.setPausedScroll(true);
        final double INITIAL_SCROLL = logTextArea.getScrollTop();
        final String LONG_TEXT = StringUtils.rightPad("InitialString", 500, "Text\n");
        logTextArea.addLineToLog(LONG_TEXT);
        assertEquals("The log should not have scrolled", INITIAL_SCROLL, logTextArea.getScrollTop(), 0.001);
    }


}