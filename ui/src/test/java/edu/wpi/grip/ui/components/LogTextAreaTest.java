package edu.wpi.grip.ui.components;


import edu.wpi.grip.ui.UiTests;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testfx.framework.junit.ApplicationTest;

import javafx.scene.Scene;
import javafx.stage.Stage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@Category(UiTests.class)
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
    final String firstLine = "First Line Of Text";
    logTextArea.addLineToLog(firstLine);
    assertEquals("First line wasn't added", firstLine + "\n", logTextArea.getText());
  }

  @Test
  public void testAddTwoLines() {
    final String firstLine = "First Line Of Text";
    final String secondLine = "Second line of text";
    logTextArea.addLineToLog(firstLine);
    logTextArea.addLineToLog(secondLine);
    assertEquals("Second line wasn't added", firstLine + "\n" + secondLine + "\n", logTextArea
        .getText());
  }

  @Test
  public void testAddingReallyLongLineOfText() {
    final String initialLength = StringUtils.rightPad("Initial string", LogTextArea
        .MAX_STRING_LENGTH - 3, "Long\n");
    logTextArea.addLineToLog(initialLength);
    assertEquals("Text was not added to logger", initialLength + "\n", logTextArea.getText());
  }

  @Test
  public void testAddingStringThatIsTooLong() {
    final String initialText = StringUtils.rightPad("Initial string", LogTextArea
        .MAX_STRING_LENGTH, "Long\n");
    logTextArea.addLineToLog(initialText);
    assertNotEquals("The initial text should not have been appended as it was too long",
        initialText + "\n", logTextArea.getText());
  }

  @Test
  public void testAddingStringToFullLog() {
    testAddingStringThatIsTooLong();
    final String currentText = logTextArea.getText();
    logTextArea.addLineToLog("More logged text");
    assertEquals("This text should not be appended as the log is full", currentText, logTextArea
        .getText());
  }

  @Test
  public void testScrollIsMaintainedWhenScrollIsPaused() {
    logTextArea.setPausedScroll(true);
    final double initialScroll = logTextArea.getScrollTop();
    final String longText = StringUtils.rightPad("InitialString", 500, "Text\n");
    logTextArea.addLineToLog(longText);
    assertEquals("The log should not have scrolled", initialScroll, logTextArea.getScrollTop(),
        0.001);
  }


}
