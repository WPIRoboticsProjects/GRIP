package edu.wpi.grip.core.operations.composite;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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

    public static final OperationDescription DESCRIPTION =
            OperationDescription.builder()
                    .name("Save Images to Disk")
                    .summary("Save image periodically to local disk")
                    .category(OperationDescription.Category.MISCELLANEOUS)
                    .icon(Icon.iconStream("publish-video"))
                    .build();

    private static final Logger logger = Logger.getLogger(SaveImageOperation.class.getName());
    private final SocketHint<Mat> inputHint = SocketHints.Inputs.createMatSocketHint("Input", false);
    private final SocketHint<Number> qualityHint = SocketHints.Inputs.createNumberSliderSocketHint("Quality", 90, 0, 100);
    private final SocketHint<Number> periodHint = SocketHints.Inputs.createNumberSpinnerSocketHint("Period", 0.1);
    private final SocketHint<Boolean> activeHint = SocketHints.Inputs.createCheckboxSocketHint("Active", false);
    private final SocketHint<String> prefixHint = SocketHints.Inputs.createTextSocketHint("Prefix", "./");
    private final SocketHint<String> suffixHint = SocketHints.Inputs.createTextSocketHint("Suffix", ".jpg");

    private final SocketHint<Mat> outputHint = SocketHints.Outputs.createMatSocketHint("Output");

    private final InputSocket<Mat> inputSocket;
    private final InputSocket<Number> qualitySocket;
    private final InputSocket<Number> periodSocket;
    private final InputSocket<Boolean> activeSocket;
    private final InputSocket<String> prefixSocket;
    private final InputSocket<String> suffixSocket;

    private final OutputSocket<Mat> outputSocket;

    private final Object imageLock = new Object();
    private final BytePointer imagePointer = new BytePointer();
    private Optional<Thread> saveThread = Optional.empty();
    private Stopwatch stopwatch = Stopwatch.createStarted();
    private int numSteps = 0;
    private SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS");
    private String prefix = "./";
    private String suffix = ".jpg";

    public SaveImageOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory outputSocketFactory) {
        inputSocket = inputSocketFactory.create(inputHint);
        qualitySocket = inputSocketFactory.create(qualityHint);
        periodSocket = inputSocketFactory.create(periodHint);
        activeSocket = inputSocketFactory.create(activeHint);
        prefixSocket = inputSocketFactory.create(prefixHint);
        suffixSocket = inputSocketFactory.create(suffixHint);

        outputSocket = outputSocketFactory.create(outputHint);

        numSteps++;
        if (!saveThread.isPresent()) {
            saveThread = Optional.of(new Thread(runServer, "Image Saver"));
            saveThread.get().setDaemon(true);
            saveThread.get().start();
        }
    }

    /**
     * Listens for incoming connections on port 1180 and writes JPEG data whenever there's a new frame.
     */
    private final Runnable runServer = () -> {
        // Loop forever (or at least until the thread is interrupted).
        logger.info("Starting image saver");
        byte[] buffer = new byte[128 * 1024];
        int bufferSize;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Wait for the main thread to put a new image.
                synchronized (imageLock) {
                    imageLock.wait();

                    // Copy the image data into a pre-allocated buffer, growing it if necessary
                    bufferSize = imagePointer.limit();
                    if (bufferSize > buffer.length) buffer = new byte[imagePointer.limit()];
                    imagePointer.get(buffer, 0, bufferSize);
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
    public synchronized void cleanUp() {
        if (--numSteps == 0) {
            saveThread.ifPresent(Thread::interrupt);
        }
    }

    @Override
    public List<InputSocket> getInputSockets() {
        return ImmutableList.of(
                inputSocket,
                qualitySocket,
                periodSocket,
                activeSocket,
                prefixSocket,
                suffixSocket
        );
    }

    @Override
    public List<OutputSocket> getOutputSockets() {
        return ImmutableList.of(
                outputSocket
        );
    }

    @Override
    public void perform() {
        if (!activeSocket.getValue().orElse(false)) {
            return;
        }

        if (!inputSocket.getValue().isPresent()) {
            throw new IllegalArgumentException("Input image must not be empty");
        }

        // don't save new image until period expires
        if (stopwatch.elapsed(TimeUnit.NANOSECONDS) < periodSocket.getValue().get().doubleValue()*1000000000L) {
            return;
        }
        stopwatch.reset();
        stopwatch.start();

        synchronized (imageLock) {
            imencode(".jpeg", inputSocket.getValue().get(), imagePointer, new IntPointer(CV_IMWRITE_JPEG_QUALITY,
                    qualitySocket.getValue().get().intValue()));
            prefix = prefixSocket.getValue().get();
            suffix = suffixSocket.getValue().get();
            imageLock.notify();
        }
    }
}
