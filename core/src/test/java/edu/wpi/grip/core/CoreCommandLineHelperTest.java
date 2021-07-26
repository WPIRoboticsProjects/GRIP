package edu.wpi.grip.core;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CoreCommandLineHelperTest {

  @Test
  public void testHelp() {
    boolean[] printed = {false};
    boolean[] exited = {false};
    MockHelper m = new MockHelper() {
      @Override
      public void printHelpAndExit() {
        printed[0] = true;
        super.printHelpAndExit();
      }

      @Override
      public void exit() {
        exited[0] = true;
      }
    };
    m.parse("-h");
    assertTrue("Help text was not printed", printed[0]);
    assertTrue("The application didn't exit", exited[0]);
    printed[0] = false;
    exited[0] = false;
    m.parse("--help");
    assertTrue("Help text was not printed", printed[0]);
    assertTrue("The application didn't exit", exited[0]);
    printed[0] = false;
    exited[0] = false;
    m.parse("--port"); // No value given, should print help text and exit
    assertTrue("Help text was not printed", printed[0]);
    assertTrue("The application didn't exit", exited[0]);
  }

  @Test
  public void testVersion() {
    boolean[] printed = {false};
    boolean[] exited = {false};
    MockHelper m = new MockHelper() {
      @Override
      public void printVersionAndExit() {
        printed[0] = true;
        super.printVersionAndExit();
      }

      @Override
      public void exit() {
        exited[0] = true;
      }
    };
    m.parse("-v");
    assertTrue("Version text was not printed", printed[0]);
    assertTrue("The application didn't exit", exited[0]);
    printed[0] = false;
    exited[0] = false;
    m.parse("--version");
    assertTrue("Version text was not printed", printed[0]);
    assertTrue("The application didn't exit", exited[0]);
  }

  private static class MockHelper extends CoreCommandLineHelper {
    @Override
    public void exit() {
      // NOP
    }
  }

}
