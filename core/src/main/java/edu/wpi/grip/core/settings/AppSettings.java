package edu.wpi.grip.core.settings;

import edu.wpi.grip.core.http.GripServer;

import com.google.common.base.MoreObjects;

import javax.annotation.Nonnegative;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Holds settings for the GRIP app. These settings are either global settings or set via the
 * command line.
 */
@SuppressWarnings("JavadocMethod")
public class AppSettings implements Settings, Cloneable {

  @Setting(label = "Internal server port",
           description = "The port that the internal server should run on.")
  private int serverPort = GripServer.DEFAULT_PORT;

  public int getServerPort() {
    return serverPort;
  }

  public void setServerPort(@Nonnegative int serverPort) {
    checkArgument(GripServer.isPortValid(serverPort),
        "Server port must be in the range 1024..65535");
    this.serverPort = serverPort;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("serverPort", serverPort)
        .toString();
  }

  @Override
  public AppSettings clone() {
    try {
      return (AppSettings) super.clone();
    } catch (CloneNotSupportedException impossible) {
      throw new AssertionError(impossible);
    }
  }

}
