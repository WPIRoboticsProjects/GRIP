package edu.wpi.grip.core.operations.network.networktables;

import com.google.inject.Singleton;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

import java.io.File;
import java.util.logging.Logger;

/**
 * This class encapsulates the way we map various settings to the global NetworkTables state.
 */
@Singleton
public class TestingNTManager extends NTManager implements AutoCloseable {

  private static final Logger logger = Logger.getLogger(TestingNTManager.class.getName());

  public TestingNTManager() {
    // We may have another instance of this method lying around
    NetworkTable.shutdown();

    // Redirect NetworkTables log messages to our own log files.  This gets rid of console spam,
    // and it also lets
    // us grep through NetworkTables messages just like any other messages.
    NetworkTablesJNI.setLogger((level, file, line, msg) -> {
      String filename = new File(file).getName();
      logger.log(ntLogLevels.get(level), String.format("NetworkTables: %s:%d %s", filename, line,
          msg));
    }, 0);

    NetworkTable.setServerMode();
    NetworkTable.initialize();
  }

  @Override
  public void close() {
    NetworkTable.shutdown();
  }

}
