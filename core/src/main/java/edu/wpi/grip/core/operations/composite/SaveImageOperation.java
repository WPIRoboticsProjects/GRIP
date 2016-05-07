package edu.wpi.grip.core.operations.composite;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHints;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_IMWRITE_JPEG_QUALITY;
import static org.bytedeco.javacpp.opencv_imgcodecs.imencode;

/**
 * Save JPEG files periodically to the local disk.
 */
public class SaveImageOperation implements Operation {

    private final Logger logger = Logger.getLogger(SaveImageOperation.class.getName());

    private final Object imageLock = new Object();
    private final BytePointer imagePointer = new BytePointer();
    private Optional<Thread> saveThread = Optional.empty();
    private Stopwatch stopwatch = Stopwatch.createStarted();
    private int numSteps = 0;
    private SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS");
    private String prefix = "./";
    private String suffix = ".jpg";

    /**
     * Listens for incoming connections on port 1180 and writes JPEG data whenever there's a new frame.
     */
    private final Runnable runServer = () -> {
        // Loop forever (or at least until the thread is interrupted).
        logger.info("Starting image saver");
        byte[] buffer = new byte[128 * 1024];
        int bufferSize;
        String prefix;
        String suffix;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Wait for the main thread to put a new image.
                synchronized (imageLock) {
                    imageLock.wait();

                    // Copy the image data into a pre-allocated buffer, growing it if necessary
                    bufferSize = imagePointer.limit();
                    if (bufferSize > buffer.length) buffer = new byte[imagePointer.limit()];
                    imagePointer.get(buffer, 0, bufferSize);
                    prefix = this.prefix;
                    suffix = this.suffix;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            // Write the file
            try (FileOutputStream out = new FileOutputStream(prefix + format.format(new Date()) + suffix)) {
                out.write(buffer, 0, bufferSize);
            } catch (IOException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        logger.info("Shutting down image saver");
        saveThread = Optional.empty();
    };

    @Override
    public String getName() {
        return "Save Images to Disk";
    }

    @Override
    public String getDescription() {
        return "Save image periodically to local disk";
    }

    @Override
    public Category getCategory() {
        return Category.MISCELLANEOUS;
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/publish-video.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        return new InputSocket<?>[]{
                new InputSocket<>(eventBus, SocketHints.Inputs.createMatSocketHint("Image", false)),
                new InputSocket<>(eventBus, SocketHints.Inputs.createNumberSliderSocketHint("Quality", 80, 0, 100)),
                new InputSocket<>(eventBus, SocketHints.Inputs.createNumberSpinnerSocketHint("Period", 0.1)),
                new InputSocket<>(eventBus, SocketHints.createBooleanSocketHint("Active", false)),
                new InputSocket<>(eventBus, SocketHints.Inputs.createTextSocketHint("Prefix", "./")),
                new InputSocket<>(eventBus, SocketHints.Inputs.createTextSocketHint("Suffix", ".jpg")),
        };
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[0];
    }

    @Override
    public synchronized Optional<?> createData() {
        numSteps++;
        if (!saveThread.isPresent()) {
            saveThread = Optional.of(new Thread(runServer, "Image Saver"));
            saveThread.get().setDaemon(true);
            saveThread.get().start();
        }
        return Optional.empty();
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        Mat input = (Mat) inputs[0].getValue().get();
        Number quality = (Number) inputs[1].getValue().get();
        Number period = (Number) inputs[2].getValue().get();
        boolean active = (Boolean) inputs[3].getValue().get();
        String prefix = (String) inputs[4].getValue().get();
        String suffix = (String) inputs[5].getValue().get();

        if (!active) {
            return;
        }

        if (input.empty()) {
            throw new IllegalArgumentException("Input image must not be empty");
        }

        // don't save new image until period expires
        if (stopwatch.elapsed(TimeUnit.NANOSECONDS) < period.doubleValue()*1000000000L) {
            return;
        }
        stopwatch.reset();
        stopwatch.start();

        synchronized (imageLock) {
            imencode(".jpeg", input, imagePointer, new IntPointer(CV_IMWRITE_JPEG_QUALITY, quality.intValue()));
            this.prefix = prefix;
            this.suffix = suffix;
            imageLock.notify();
        }
    }

    @Override
    public synchronized void cleanUp(InputSocket<?>[] inputs, OutputSocket<?>[] outputs, Optional<?> data) {
        if (--numSteps == 0) {
            saveThread.ifPresent(Thread::interrupt);
        }
    }
}
