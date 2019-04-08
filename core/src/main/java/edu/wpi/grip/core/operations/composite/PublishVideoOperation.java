package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.annotation.operation.OperationCategory;
import edu.wpi.grip.core.MatWrapper;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bytedeco.javacpp.opencv_imgcodecs.CV_IMWRITE_JPEG_QUALITY;
import static org.bytedeco.javacpp.opencv_imgcodecs.imencode;

/**
 * Publish an M-JPEG stream with the protocol used by SmartDashboard and the FRC Dashboard.  This
 * allows FRC teams to view video streams on their dashboard during competition even when GRIP has
 * exclusive access to the camera.  In addition, an intermediate processed image in the pipeline
 * could be published instead. Based on WPILib's CameraServer class:
 * https://github.com/robotpy/allwpilib/blob/master/wpilibj/src/athena/java/edu/wpi/first/wpilibj
 * /CameraServer.java
 */
@Description(name = "Publish Video",
             summary = "Publish an MJPEG stream",
             category = OperationCategory.NETWORK,
             iconName = "publish-video")
public class PublishVideoOperation implements Operation {

  private static final Logger logger = Logger.getLogger(PublishVideoOperation.class.getName());
  private static final int PORT = 1180;
  private static final byte[] MAGIC_NUMBER = {0x01, 0x00, 0x00, 0x00};

  @SuppressWarnings("PMD.AssignmentToNonFinalStatic")
  private static int numSteps;
  private final Object imageLock = new Object();
  private final BytePointer imagePointer = new BytePointer();
  private final Thread serverThread;
  private final InputSocket<MatWrapper> inputSocket;
  private final InputSocket<Number> qualitySocket;
  @SuppressWarnings("PMD.SingularField")
  private volatile boolean connected = false;
  private volatile boolean hasImage = false;
  /**
   * Listens for incoming connections on port 1180 and writes JPEG data whenever there's a new
   * frame.
   */
  private final Runnable runServer = () -> {
    // Loop forever (or at least until the thread is interrupted).  This lets us recover from the
    // dashboard
    // disconnecting or the network connection going away temporarily.
    while (!Thread.currentThread().isInterrupted()) {
      try (ServerSocket serverSocket = new ServerSocket(PORT)) {
        logger.info("Starting camera server");

        try (Socket socket = serverSocket.accept()) {
          logger.info("Got connection from " + socket.getInetAddress());
          connected = true;

          DataOutputStream socketOutputStream = new DataOutputStream(socket.getOutputStream());
          DataInputStream socketInputStream = new DataInputStream(socket.getInputStream());

          byte[] buffer = new byte[128 * 1024];
          int bufferSize;

          final int fps = socketInputStream.readInt();
          final int compression = socketInputStream.readInt();
          final int size = socketInputStream.readInt();

          if (compression != -1) {
            logger.warning("Dashboard video should be in HW mode");
          }

          final long frameDuration = 1000000000L / fps;
          long startTime = System.nanoTime();

          while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
            // Wait for the main thread to put a new image. This happens whenever perform() is
            // called with
            // a new input.
            synchronized (imageLock) {
              while (!hasImage) {
                imageLock.wait();
              }

              // Copy the image data into a pre-allocated buffer, growing it if necessary
              bufferSize = (int) imagePointer.limit();
              if (bufferSize > buffer.length) {
                buffer = new byte[(int) imagePointer.limit()];
              }
              imagePointer.get(buffer, 0, bufferSize);
              hasImage = false;
            }

            // The FRC dashboard image protocol consists of a magic number, the size of the image
            // data,
            // and the image data itself.
            socketOutputStream.write(MAGIC_NUMBER);
            socketOutputStream.writeInt(bufferSize);
            socketOutputStream.write(buffer, 0, bufferSize);

            // Limit the FPS to whatever the dashboard requested
            int remainingTime = (int) (frameDuration - (System.nanoTime() - startTime));
            if (remainingTime > 0) {
              Thread.sleep(remainingTime / 1000000, remainingTime % 1000000);
            }

            startTime = System.nanoTime();
          }
        }
      } catch (IOException e) {
        logger.log(Level.WARNING, e.getMessage(), e);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // This is really unnecessary since the thread is
        // about to exit
        logger.info("Shutting down camera server");
        return;
      } finally {
        connected = false;
      }
    }
  };

  @Inject
  @SuppressWarnings("JavadocMethod")
  @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                      justification = "Do not need to synchronize inside of a constructor")
  public PublishVideoOperation(InputSocket.Factory inputSocketFactory) {
    if (numSteps != 0) {
      throw new IllegalStateException("Only one instance of PublishVideoOperation may exist");
    }
    this.inputSocket = inputSocketFactory.create(SocketHints.createImageSocketHint("Image"));
    this.qualitySocket = inputSocketFactory.create(SocketHints.Inputs
        .createNumberSliderSocketHint("Quality", 80, 0, 100));
    numSteps++;

    serverThread = new Thread(runServer, "Camera Server");
    serverThread.setDaemon(true);
    serverThread.start();
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
    if (!connected) {
      return; // Don't waste any time converting images if there's no dashboard connected
    }

    if (inputSocket.getValue().get().empty()) {
      throw new IllegalArgumentException("Input image must not be empty");
    }

    synchronized (imageLock) {
      imencode(".jpeg", inputSocket.getValue().get().getCpu(), imagePointer,
          new IntPointer(CV_IMWRITE_JPEG_QUALITY, qualitySocket.getValue().get().intValue()));
      hasImage = true;
      imageLock.notifyAll();
    }
  }

  @Override
  public synchronized void cleanUp() {
    // Stop the video server if there are no Publish Video steps left
    serverThread.interrupt();
    numSteps--;
  }
}
