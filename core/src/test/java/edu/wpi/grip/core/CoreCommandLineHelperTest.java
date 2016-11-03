package edu.wpi.grip.core;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CoreCommandLineHelperTest {

  private static class Mock extends CoreCommandLineHelper {
    @Override
    void exit() {
      // NOP
    }
  }

  @Test
  public void testHelp() {
    boolean[] printed = {false};
    boolean[] exited = {false};
    Mock m = new Mock() {
      @Override
      void printHelpAndExit() {
        printed[0] = true;
        super.printHelpAndExit();
      }

      @Override
      void exit() {
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
  }

  @Test
  public void testVersion() {
    boolean[] printed = {false};
    boolean[] exited = {false};
    Mock m = new Mock() {
      @Override
      void printVersionAndExit() {
        printed[0] = true;
        super.printHelpAndExit();
      }

      @Override
      void exit() {
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

}
