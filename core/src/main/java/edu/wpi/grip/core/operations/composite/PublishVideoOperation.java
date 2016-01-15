package edu.wpi.grip.core.operations.composite;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHints;
import edu.wpi.grip.core.events.StepRemovedEvent;
import org.bytedeco.javacv.FrameConverter;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bytedeco.javacpp.opencv_core.Mat;

/**
 * Publish an M-JPEG stream with the protocol used by SmartDashboard and the FRC Dashboard.  This allows FRC teams to
 * view video streams on their dashboard during competition even when GRIP has exclusive access to the camera.  In
 * addition, an intermediate processed image in the pipeline could be published instead.
 * <p>
 * Based on WPILib's CameraServer class: https://github.com/robotpy/allwpilib/blob/master/wpilibj/src/athena/java/edu/wpi/first/wpilibj/CameraServer.java
 */
public class PublishVideoOperation implements Operation {

    private final Logger logger = Logger.getLogger(PublishVideoOperation.class.getName());

    private static final int PORT = 1180;
    private static final byte[] MAGIC_NUMBER = {0x01, 0x00, 0x00, 0x00};

    private final Object imageLock = new Object();
    private RenderedImage image = null;
    private Optional<Thread> serverThread = Optional.empty();
    private volatile float compressionQuality = 0.5f;
    private volatile boolean connected = false;
    private volatile int numSteps = 0;

    /**
     * Listens for incoming connections on port 1180 and writes JPEG data whenever there's a new frame.
     */
    private final Runnable runServer = () -> {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Loop forever (or at least until the thread is interrupted).  This lets us recover from the dashboard
        // disconnecting or the network connection going away temporarily.
        while (!Thread.currentThread().isInterrupted()) {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                logger.info("Starting camera server");

                ImageWriter jpegWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
                jpegWriter.setOutput(ImageIO.createImageOutputStream(baos));

                ImageWriteParam jpegWriteParam = jpegWriter.getDefaultWriteParam();
                jpegWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

                try (Socket socket = serverSocket.accept()) {
                    logger.info("Got connection from " + socket.getInetAddress());
                    connected = true;

                    DataOutputStream socketOutputStream = new DataOutputStream(socket.getOutputStream());
                    DataInputStream socketInputStream = new DataInputStream(socket.getInputStream());

                    final int fps = socketInputStream.readInt();
                    final int compression = socketInputStream.readInt();
                    final int size = socketInputStream.readInt();

                    if (compression != -1) {
                        logger.warning("Dashboard video should be in HW mode");
                    }

                    final long frameDuration = 1000000000L / fps;
                    long startTime = System.nanoTime();

                    while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                        baos.reset();

                        // Wait for the main thread to put a new image. This happens whenever perform() is called with
                        // a new input.
                        synchronized (imageLock) {
                            imageLock.wait();
                            jpegWriteParam.setCompressionQuality(compressionQuality / 100.0f);
                            jpegWriter.write(null, new IIOImage(image, null, null), jpegWriteParam);
                        }

                        // The FRC dashboard image protocol consists of a magic number, the size of the image data,
                        // and the image data itself.
                        socketOutputStream.write(MAGIC_NUMBER);
                        socketOutputStream.writeInt(baos.size());
                        baos.writeTo(socketOutputStream);

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
                Thread.currentThread().interrupt(); // This is really unnecessary since the thread is about to exit
                logger.info("Shutting down camera server");
                serverThread = Optional.empty();
                return;
            } finally {
                connected = false;
            }
        }
    };

    @Override
    public String getName() {
        return "Publish Video";
    }

    @Override
    public String getDescription() {
        return "Publish an M-JPEG stream to the dashboard";
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/publish-video.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        eventBus.register(this);
        return new InputSocket<?>[]{
                new InputSocket<>(eventBus, SocketHints.Inputs.createMatSocketHint("Image", false)),
                new InputSocket<>(eventBus, SocketHints.Inputs.createNumberSliderSocketHint("Quality", 80, 0, 100)),
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[0];
    }

    @Override
    public Optional<?> createData() {
        numSteps++;
        if (!serverThread.isPresent()) {
            serverThread = Optional.of(new Thread(runServer, "Camera Server"));
            serverThread.get().setDaemon(true);
            serverThread.get().start();
        }
        return Optional.empty();
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        Mat input = (Mat) inputs[0].getValue().get();
        Number quality = (Number) inputs[1].getValue().get();

        if (!connected) {
            return; // Don't waste any time converting images if there's no dashboard connected
        }

        if (input.empty()) {
            throw new IllegalArgumentException("Input image must not be empty");
        }

        FrameConverter<Mat> frameConverter = new OpenCVFrameConverter.ToMat();
        FrameConverter<BufferedImage> imageConverter = new Java2DFrameConverter();
        BufferedImage image = imageConverter.convert(frameConverter.convert(input));

        synchronized (imageLock) {
            this.image = image;
            this.compressionQuality = quality.floatValue();
            imageLock.notify();
        }
    }

    @Subscribe
    public void onStepRemoved(StepRemovedEvent event) {
        if (event.getStep().getOperation() == this) {
            // Stop the video server if there are no Publish Video steps left
            if (--numSteps == 0) {
                serverThread.ifPresent(Thread::interrupt);
            }
        }
    }
}
