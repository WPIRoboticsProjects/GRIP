package edu.wpi.grip.core.sources;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import edu.wpi.cscore.CameraServerJNI;
import edu.wpi.cscore.ImageSink;
import edu.wpi.cscore.VideoMode;
import edu.wpi.cscore.VideoMode.PixelFormat;
import edu.wpi.cscore.raw.RawFrame;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.opencv_core.Mat;

import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC1;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC2;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC3;

@SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
public class JavaCvSink extends ImageSink {
  private final RawFrame frame = new RawFrame();
  private Mat tmpMat;
  private ByteBuffer origByteBuffer;
  private int width;
  private int height;
  private int pixelFormat;
  private final int bgrValue = PixelFormat.kBGR.getValue();

  private int getCVFormat(PixelFormat pixelFormat) {
    int type = 0;
    switch (pixelFormat) {
      case kYUYV:
      case kRGB565:
        type = CV_8UC2;
        break;
      case kBGR:
        type = CV_8UC3;
        break;
      case kGray:
      case kMJPEG:
      default:
        type = CV_8UC1;
        break;
    }
    return type;
  }

  @Override
  public void close() {
    frame.close();
    super.close();
  }

  /**
   * Create a sink for accepting OpenCV images. WaitForFrame() must be called on the created sink to
   * get each new image.
   *
   * @param name Source name (arbitrary unique identifier)
   */
  public JavaCvSink(String name) {
    super(CameraServerJNI.createRawSink(name));
  }

  /**
   * Wait for the next frame and get the image. Times out (returning 0) after 0.225 seconds. The
   * provided image will have three 3-bit channels stored in BGR order.
   *
   * @return Frame time, or 0 on error (call GetError() to obtain the error message)
   */
  public long grabFrame(Mat image) {
    return grabFrame(image, 0.225);
  }

  /**
   * Wait for the next frame and get the image. Times out (returning 0) after timeout seconds. The
   * provided image will have three 3-bit channels stored in BGR order.
   *
   * @return Frame time, or 0 on error (call GetError() to obtain the error message); the frame time
   *     is in 1 us increments.
   */
  public long grabFrame(Mat image, double timeout) {
    checkNotNull(image, "Image cannot be null");
    frame.setWidth(0);
    frame.setHeight(0);
    frame.setPixelFormat(bgrValue);
    long rv = CameraServerJNI.grabSinkFrameTimeout(m_handle, frame, timeout);
    if (rv <= 0) {
      return rv;
    }

    if (frame.getDataByteBuffer() != origByteBuffer || width != frame.getWidth()
        || height != frame.getHeight() || pixelFormat != frame.getPixelFormat()) {
      origByteBuffer = frame.getDataByteBuffer();
      height = frame.getHeight();
      width = frame.getWidth();
      pixelFormat = frame.getPixelFormat();
      tmpMat = new Mat(frame.getHeight(), frame.getWidth(),
          getCVFormat(VideoMode.getPixelFormatFromInt(pixelFormat)),
          new BytePointer(origByteBuffer));
    }
    tmpMat.copyTo(image);
    return rv;
  }
}
