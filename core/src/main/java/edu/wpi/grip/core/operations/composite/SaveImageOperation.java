package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.core.FileManager;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

  private final SocketHint<Mat> inputHint
      = SocketHints.Inputs.createMatSocketHint("Input", false);
  private final SocketHint<FileTypes> fileTypeHint
      = SocketHints.createEnumSocketHint("File type", FileTypes.JPEG);
  private final SocketHint<Number> qualityHint
      = SocketHints.Inputs.createNumberSliderSocketHint("Quality", 90, 0, 100);
  private final SocketHint<Number> periodHint
      = SocketHints.Inputs.createNumberSpinnerSocketHint("Period", 0.1);
  private final SocketHint<Boolean> activeHint
      = SocketHints.Inputs.createCheckboxSocketHint("Active", false);

  private final SocketHint<Mat> outputHint = SocketHints.Outputs.createMatSocketHint("Output");

  private final InputSocket<Mat> inputSocket;
  private final InputSocket<FileTypes> fileTypesSocket;
  private final InputSocket<Number> qualitySocket;
  private final InputSocket<Number> periodSocket;
  private final InputSocket<Boolean> activeSocket;

  private final OutputSocket<Mat> outputSocket;

  private final FileManager fileManager;
  private final BytePointer imagePointer = new BytePointer();
  private final Stopwatch stopwatch = Stopwatch.createStarted();
  private final DateTimeFormatter formatter
      = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");

  private enum FileTypes {
    JPEG, PNG;

    @Override
    public String toString() {
      return super.toString().toLowerCase();
    }
  }

  @SuppressWarnings("JavadocMethod")
  public SaveImageOperation(InputSocket.Factory inputSocketFactory,
                            OutputSocket.Factory outputSocketFactory,
                            FileManager fileManager) {
    this.fileManager = fileManager;

    inputSocket = inputSocketFactory.create(inputHint);
    fileTypesSocket = inputSocketFactory.create(fileTypeHint);
    qualitySocket = inputSocketFactory.create(qualityHint);
    periodSocket = inputSocketFactory.create(periodHint);
    activeSocket = inputSocketFactory.create(activeHint);

    outputSocket = outputSocketFactory.create(outputHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        inputSocket,
        fileTypesSocket,
        qualitySocket,
        periodSocket,
        activeSocket
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

    // don't save new image until period expires
    if (stopwatch.elapsed(TimeUnit.MILLISECONDS)
        < periodSocket.getValue().get().doubleValue() * 1000L) {
      return;
    }
    stopwatch.reset();
    stopwatch.start();

    imencode("." + fileTypesSocket.getValue().get(), inputSocket.getValue().get(), imagePointer,
        new IntPointer(CV_IMWRITE_JPEG_QUALITY, qualitySocket.getValue().get().intValue()));
    byte[] buffer = new byte[128 * 1024];
    int bufferSize = imagePointer.limit();
    if (bufferSize > buffer.length) {
      buffer = new byte[imagePointer.limit()];
    }
    imagePointer.get(buffer, 0, bufferSize);

    fileManager.saveImage(buffer, LocalDateTime.now().format(formatter)
        + "." + fileTypesSocket.getValue().get());
  }
}
