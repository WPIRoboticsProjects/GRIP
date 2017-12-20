package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;
import edu.wpi.grip.core.util.OpenCvShims;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import edu.wpi.cscore.CameraServerJNI;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.VideoMode;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;

import org.bytedeco.javacpp.opencv_core;
import org.opencv.core.Mat;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Publish an M-JPEG stream with the protocol used by SmartDashboard and the FRC Dashboard.  This
 * allows FRC teams to view video streams on their dashboard during competition even when GRIP has
 * exclusive access to the camera. Uses cscore to host the image streaming server.
 */
public class PublishVideoOperation implements Operation {

  private static final Logger logger = Logger.getLogger(PublishVideoOperation.class.getName());

  /**
   * Flags whether or not cscore was loaded. If it could not be loaded, the MJPEG streaming server
   * can't be started, preventing this operation from running.
   */
  private static final boolean cscoreLoaded;

  static {
    boolean loaded;
    try {
      // Loading the CameraServerJNI class will load the appropriate platform-specific OpenCV JNI
      CameraServerJNI.getHostname();
      loaded = true;
    } catch (Throwable e) {
      logger.log(Level.SEVERE, "CameraServerJNI load failed!", e);
      loaded = false;
    }
    cscoreLoaded = loaded;
  }

  public static final OperationDescription DESCRIPTION =
      OperationDescription.builder()
          .name("Publish Video")
          .summary("Publish an M_JPEG stream to the dashboard.")
          .category(OperationDescription.Category.NETWORK)
          .icon(Icon.iconStream("publish-video"))
          .build();
  private static final int INITIAL_PORT = 1180;
  private static final int MAX_STEP_COUNT = 10; // limit ports to 1180-1189

  @SuppressWarnings("PMD.AssignmentToNonFinalStatic")
  private static int totalStepCount;
  @SuppressWarnings("PMD.AssignmentToNonFinalStatic")
  private static int numSteps;
  private static final Deque<Integer> availablePorts =
      Stream.iterate(INITIAL_PORT, i -> i + 1)
          .limit(MAX_STEP_COUNT)
          .collect(Collectors.toCollection(LinkedList::new));

  private final InputSocket<opencv_core.Mat> inputSocket;
  private final InputSocket<Number> qualitySocket;
  private final MjpegServer server;
  private final CvSource serverSource;

  // Write to the /CameraPublisher table so the MJPEG streams are discoverable by other
  // applications connected to the same NetworkTable server (eg Shuffleboard)
  private final ITable cameraPublisherTable = NetworkTable.getTable("/CameraPublisher"); // NOPMD
  private final ITable ourTable;
  private final Mat publishMat = new Mat();
  private long lastFrame = -1;

  @SuppressWarnings("JavadocMethod")
  @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
      justification = "Do not need to synchronize inside of a constructor")
  public PublishVideoOperation(InputSocket.Factory inputSocketFactory) {
    if (numSteps >= MAX_STEP_COUNT) {
      throw new IllegalStateException(
          "Only " + MAX_STEP_COUNT + " instances of PublishVideoOperation may exist");
    }
    this.inputSocket = inputSocketFactory.create(SocketHints.Inputs.createMatSocketHint("Image",
        false));
    this.qualitySocket = inputSocketFactory.create(SocketHints.Inputs
        .createNumberSliderSocketHint("Quality", 80, 0, 100));

    if (cscoreLoaded) {
      int ourPort = availablePorts.removeFirst();

      server = new MjpegServer("GRIP video publishing server " + totalStepCount, ourPort);
      serverSource = new CvSource("GRIP CvSource " + totalStepCount,
          VideoMode.PixelFormat.kMJPEG, 0, 0, 0);
      server.setSource(serverSource);

      ourTable = cameraPublisherTable.getSubTable("GRIP-" + totalStepCount);
      try {
        List<NetworkInterface> networkInterfaces =
            Collections.list(NetworkInterface.getNetworkInterfaces());
        ourTable.putStringArray("streams", generateStreams(networkInterfaces, ourPort));
      } catch (SocketException e) {
        logger.log(Level.WARNING, "Could not enumerate the local network interfaces", e);
        ourTable.putStringArray("streams", new String[0]);
      }
    } else {
      server = null;
      serverSource = null;
      ourTable = null;
    }

    numSteps++;
    totalStepCount++;
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        inputSocket,
        qualitySocket
    );
  }

  @Override
  public List<OutputSocket> getOutputSockets() {
    return ImmutableList.of();
  }

  @Override
  public void perform() {
    final long now = System.nanoTime(); // NOPMD

    if (!cscoreLoaded) {
      throw new IllegalStateException(
          "cscore could not be loaded. The image streaming server cannot be started.");
    }

    opencv_core.Mat input = inputSocket.getValue().get();
    if (input.empty() || input.isNull()) {
      throw new IllegalArgumentException("Input image must not be empty");
    }

    OpenCvShims.copyJavaCvMatToOpenCvMat(input, publishMat);
    // Make sure the output resolution is up to date. Might not be needed, depends on cscore updates
    serverSource.setResolution(input.size().width(), input.size().height());
    serverSource.putFrame(publishMat);
    if (lastFrame != -1) {
      long dt = now - lastFrame;
      serverSource.setFPS((int) (1e9 / dt));
    }
    lastFrame = now;
  }

  @Override
  public synchronized void cleanUp() {
    numSteps--;
    if (cscoreLoaded) {
      availablePorts.addFirst(server.getPort());
      ourTable.getKeys().forEach(ourTable::delete);
      serverSource.setConnected(false);
      serverSource.free();
      server.free();
      if (publishMat != null) {
        publishMat.release();
      }
    }
  }

  /**
   * Generates an array of stream URLs that allow third-party applications to discover the
   * appropriate URLs that can stream MJPEG. The URLs will all point to the same physical machine,
   * but may use different network interfaces (eg WiFi and ethernet).
   *
   * @param networkInterfaces the local network interfaces
   * @param serverPort        the port the mjpeg streaming server is running on
   * @return an array of URLs that can be used to connect to the MJPEG streaming server
   */
  @VisibleForTesting
  static String[] generateStreams(Collection<NetworkInterface> networkInterfaces, int serverPort) {
    return networkInterfaces.stream()
        .flatMap(i -> Collections.list(i.getInetAddresses()).stream())
        .filter(a -> a instanceof Inet4Address) // IPv6 isn't well supported, stick to IPv4
        .filter(a -> !a.isLoopbackAddress())    // loopback addresses only work for local processes
        .distinct()
        .flatMap(a -> Stream.of(
            generateStreamUrl(a.getHostName(), serverPort),
            generateStreamUrl(a.getHostAddress(), serverPort)))
        .distinct()
        .toArray(String[]::new);
  }

  /**
   * Generates a URL that can be used to connect to an MJPEG stream provided by cscore. The host
   * should be a non-loopback IPv4 address that is resolvable by applications running on non-local
   * machines.
   *
   * @param host the server host
   * @param port the port the server is running on
   */
  @VisibleForTesting
  static String generateStreamUrl(String host, int port) {
    return String.format("mjpeg:http://%s:%d/?action=stream", host, port);
  }

}
