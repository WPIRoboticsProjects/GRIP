package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;

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

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
        InetAddress localHost = InetAddress.getLocalHost();
        ourTable.putStringArray("streams",
            new String[]{
                generateStreamUrl(localHost.getHostName(), ourPort),
                generateStreamUrl(localHost.getHostAddress(), ourPort)
            });
      } catch (UnknownHostException e) {
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

    copyJavaCvToOpenCvMat(input, publishMat);
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
    }
  }

  private static String generateStreamUrl(String host, int port) {
    return String.format("mjpeg:http://%s:%d/?action=stream", host, port);
  }

  /**
   * Copies the data from a JavaCV Mat wrapper object into an OpenCV Mat wrapper object so it's
   * usable by the {@link CvSource} for this operation.
   *
   * <p>Since the JavaCV and OpenCV bindings both target the same native version of OpenCV, this is
   * implemented by simply changing the OpenCV Mat's native pointer to be the same as the one for
   * the JavaCV Mat. This prevents memory copies and resizing/reallocating memory for the OpenCV
   * wrapper to fit the source image. Updating the pointer is a simple field write (albeit via
   * reflection), which is much faster and easier than allocating and copying byte buffers.</p>
   *
   * <p>A caveat to this approach is that the memory layout used by the OpenCV binaries bundled with
   * both wrapper libraries <i>must</i> be identical. Using the same OpenCV version for both
   * libraries should be enough.</p>
   *
   * @param javaCvMat the JavaCV Mat wrapper object to copy from
   * @param openCvMat the OpenCV Mat wrapper object to copy into
   * @throws RuntimeException if the OpenCV native pointer could not be set
   */
  @VisibleForTesting
  static void copyJavaCvToOpenCvMat(opencv_core.Mat javaCvMat, Mat openCvMat)
      throws RuntimeException {
    // Make the OpenCV Mat object point to the same block of memory as the JavaCV object.
    // This requires no data transfers or copies and is O(1) instead of O(n)
    if (javaCvMat.address() != openCvMat.nativeObj) {
      try {
        Field nativeObjField = Mat.class.getField("nativeObj");
        nativeObjField.setAccessible(true);
        nativeObjField.setLong(openCvMat, javaCvMat.address());
      } catch (ReflectiveOperationException e) {
        logger.log(Level.WARNING, "Could not set native object pointer", e);
        throw new RuntimeException("Could not copy the image", e);
      }
    }
  }

}
