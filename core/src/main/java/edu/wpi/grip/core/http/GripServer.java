package edu.wpi.grip.core.http;

import edu.wpi.grip.core.events.AppSettingsChangedEvent;
import edu.wpi.grip.core.exception.GripServerException;
import edu.wpi.grip.core.settings.SettingsProvider;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;

/**
 * An internal HTTP server.
 */
@Singleton
public class GripServer {

  /**
   * Factory for creating a new Jetty server.
   */
  private final JettyServerFactory serverFactory;

  /**
   * The port that this server is running on.
   */
  private final int port;

  /**
   * The internal Jetty server that actually handles all server operations.
   */
  private Server server;

  /**
   * A collection of Jetty handlers that can be added to or removed during runtime.
   */
  private final HandlerCollection handlers = new HandlerCollection(true);

  /**
   * The current lifecycle state of the server.
   */
  private State state = State.PRE_RUN;

  /**
   * Possible lifecycle states of the server.
   */
  public enum State {
    /**
     * The server has not been started yet.
     */
    PRE_RUN,
    /**
     * The server is currently running.
     */
    RUNNING,
    /**
     * The server was running and has been stopped.
     */
    STOPPED
  }

  /**
   * The root path for all GRIP-related HTTP activity.
   */
  public static final String ROOT_PATH = "/GRIP";

  /**
   * The root path for uploading data to the server.
   */
  public static final String UPLOAD_PATH = ROOT_PATH + "/upload";

  /**
   * The path for uploading images. To upload an image, post an HTTP event to
   * {@code /GRIP/upload/image}, with the image bytes as the data.
   */
  public static final String IMAGE_UPLOAD_PATH = UPLOAD_PATH + "/image";

  /**
   * The path for setting which pipeline to run. To set the pipeline, post an
   * HTTP event to {@code /GRIP/upload/pipeline},
   * with the content of the pipeline save file as the data.
   */
  public static final String PIPELINE_UPLOAD_PATH = UPLOAD_PATH + "/pipeline";

  /**
   * The path for requesting data. Data will be returned as a json-formatted
   * map of the outputs of all requested data sets.
   *
   * <p>For example, performing a {@code GET} request on the path
   * {@code /GRIP/data?foo&bar} will return a map such as
   * <br>
   * <code><pre>
   * {
   *   'foo': {
   *        // data
   *      },
   *   'bar':
   *     {
   *       // data
   *     }
   * }
   * </pre></code>
   */
  public static final String DATA_PATH = ROOT_PATH + "/data";

  /**
   * The default port the server should run on.
   */
  public static final int DEFAULT_PORT = 2084;

  /**
   * The lowest valid TCP port number.
   */
  private static final int MIN_PORT = 1024;

  /**
   * The highest valid TCP port number.
   */
  private static final int MAX_PORT = 65535;

  /**
   * Checks if the given TCP port is valid for a server to run on. This doesn't check availability.
   *
   * @param port the port to check
   *
   * @return true if the port is valid, false if not
   */
  public static boolean isPortValid(int port) {
    return port >= MIN_PORT && port <= MAX_PORT;
  }

  public interface JettyServerFactory {
    Server create(int port);
  }

  public static class JettyServerFactoryImpl implements JettyServerFactory {

    @Override
    public Server create(int port) {
      return new Server(port);
    }
  }

  @Inject
  GripServer(ContextStore contextStore,
             JettyServerFactory serverFactory,
             SettingsProvider settingsProvider) {
    this.port = settingsProvider.getAppSettings().getServerPort();
    this.serverFactory = serverFactory;
    this.server = serverFactory.create(port);
    this.server.setHandler(handlers);
    handlers.addHandler(new NoContextHandler(contextStore));

    Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
  }

  /**
   * Adds the given handler to the server. Does nothing if the server already has that handler.
   *
   * @param handler the handler to add
   */
  public void addHandler(Handler handler) {
    if (!handlers.contains(handler)) {
      handlers.addHandler(handler);
    }
  }

  /**
   * Removes the given handler from the server.
   * Does nothing if the server does not have that handler.
   *
   * @param handler the handler to remove
   */
  public void removeHandler(Handler handler) {
    handlers.removeHandler(handler);
  }

  /**
   * Starts this server.
   * Has no effect if the server has already been started or if it's been stopped.
   */
  public void start() {
    if (state == State.PRE_RUN) {
      try {
        server.start();
      } catch (Exception ex) {
        throw new GripServerException("Could not start Jetty server", ex);
      }
      state = State.RUNNING;
    }
  }

  /**
   * Stops this server. Note that a shutdown hook has been registered to call
   * this method, so it's unlikely that this should need to be called. If you
   * need to restart the server, use {@link #restart()} as this method will kill the
   * internal HTTP server, which cannot be restarted by {@link #start()}.
   */
  public void stop() {
    if (state == State.RUNNING) {
      try {
        server.stop();
      } catch (Exception ex) {
        throw new GripServerException("Could not stop Jetty server", ex);
      }
      state = State.STOPPED;
    }
  }

  /**
   * Restarts the server on the current port.
   *
   * @throws GripServerException if the server was unable to be restarted.
   */
  public void restart() {
    try {
      if (state == State.RUNNING) {
        try {
          server.stop();
        } catch (Exception ex) {
          throw new GripServerException("Could not stop Jetty server", ex);
        }
        state = State.STOPPED;
      }
      server = serverFactory.create(port);
      start();
    } catch (GripServerException | IllegalStateException ex) {
      throw new GripServerException("Could not restart GripServer", ex);
    }
  }

  /**
   * Gets the current state of the server.
   */
  public State getState() {
    return state;
  }

  /**
   * Stops the server (if it's running) and creates a new HTTP server on the given port.
   *
   * @param port the new port to run on.
   */
  public void setPort(int port) {
    if (!isPortValid(port)) {
      throw new IllegalArgumentException("Invalid port: " + port);
    }
    stop();
    server = serverFactory.create(port);
    server.setHandler(handlers);
    state = State.PRE_RUN;
    start();
  }

  /**
   * Gets the port this server is running on.
   */
  public int getPort() {
    return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
  }

  @Subscribe
  public void settingsChanged(AppSettingsChangedEvent event) {
    int port = event.getAppSettings().getServerPort();
    if (port != getPort()) {
      setPort(port);
    }
  }

}
