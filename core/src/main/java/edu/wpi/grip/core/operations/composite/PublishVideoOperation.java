package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;

import com.google.common.collect.ImmutableList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import edu.wpi.cscore.CameraServerJNI;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.VideoMode;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

import org.bytedeco.javacpp.opencv_core;
import org.opencv.core.Mat;

import java.lang.reflect.Field;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.opencv_core.CV_8S;
import static org.bytedeco.javacpp.opencv_core.CV_8U;

/**
 * Publish an M-JPEG stream with the protocol used by SmartDashboard and the FRC Dashboard.  This
 * allows FRC teams to view video streams on their dashboard during competition even when GRIP has
 * exclusive access to the camera. Uses cscore to host the image streaming server.
 */
public class PublishVideoOperation implements Operation {

  private static final Logger logger = Logger.getLogger(PublishVideoOperation.class.getName());

  static {
    try {
      // Loading the CameraServerJNI class will load the appropriate platform-specific OpenCV JNI
      CameraServerJNI.getHostname();
    } catch (Throwable e) {
      logger.log(Level.SEVERE, "CameraServerJNI load failed! Exiting", e);
      System.exit(31);
    }
  }

  public static final OperationDescription DESCRIPTION =
      OperationDescription.builder()
          .name("Publish Video")
          .summary("Publish an M_JPEG stream to the dashboard.")
          .category(OperationDescription.Category.NETWORK)
          .icon(Icon.iconStream("publish-video"))
          .build();
  private static final int PORT = 1180;

  @SuppressWarnings("PMD.AssignmentToNonFinalStatic")
  private static int numSteps;
  private static final int MAX_STEP_COUNT = 10; // limit ports to 1180-1189
  private static final Deque<Integer> availablePorts =
      Stream.iterate(PORT, i -> i + 1)
          .limit(MAX_STEP_COUNT)
          .collect(Collectors.toCollection(LinkedList::new));

  private final InputSocket<opencv_core.Mat> inputSocket;
  private final InputSocket<Number> qualitySocket;
  private final MjpegServer server;
  private final CvSource serverSource;
  private static final NetworkTable cameraPublisherTable =
      NetworkTable.getTable("/CameraPublisher");
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

    int ourPort = availablePorts.removeFirst();

    server = new MjpegServer("GRIP video publishing server " + ourPort, ourPort);
    serverSource = new CvSource("GRIP CvSource:" + ourPort, VideoMode.PixelFormat.kMJPEG, 0, 0, 0);
    server.setSource(serverSource);
    cameraPublisherTable.putStringArray("streams",
        new String[]{CameraServerJNI.getHostname() + ":" + ourPort});

    numSteps++;
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
    final long now = System.nanoTime();
    opencv_core.Mat input = inputSocket.getValue().get();
    if (input.empty() || input.isNull()) {
      throw new IllegalArgumentException("Input image must not be empty");
    }

    copyJavaCvToOpenCvMat(input, publishMat);
    serverSource.putFrame(publishMat);
    if (lastFrame != -1) {
      long dt = now - lastFrame;
      serverSource.setFPS((int) (1e9 / dt));
    }
    lastFrame = now;
    server.setSource(serverSource);
  }

  @Override
  public synchronized void cleanUp() {
    // Stop the video server if there are no Publish Video steps left
    numSteps--;
    availablePorts.addFirst(server.getPort());
  }

  private void copyJavaCvToOpenCvMat(opencv_core.Mat javaCvMat, Mat openCvMat) {
    if (javaCvMat.depth() != CV_8U && javaCvMat.depth() != CV_8S) {
      throw new IllegalArgumentException("Only 8-bit depth images are supported");
    }

    final opencv_core.Size size = javaCvMat.size();

    // Make sure the output resolution is up to date
    serverSource.setResolution(size.width(), size.height());

    // Make the OpenCV Mat object point to the same block of memory as the JavaCV object.
    // This requires no data transfers or copies and is O(1) instead of O(n)
    if (javaCvMat.address() != openCvMat.nativeObj) {
      try {
        Field nativeObjField = Mat.class.getField("nativeObj");
        nativeObjField.setAccessible(true);
        nativeObjField.setLong(openCvMat, javaCvMat.address());
      } catch (ReflectiveOperationException e) {
        logger.log(Level.WARNING, "Could not set native object pointer", e);
      }
    }
  }

}
