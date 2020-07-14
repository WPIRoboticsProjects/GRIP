package edu.wpi.grip.core.sources;

import edu.wpi.cscore.CameraServerJNI;
import edu.wpi.cscore.HttpCamera;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameConverter;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

// This is here because FrameGrabber has an exception called Exception which triggers PMD
@SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes", "all"})
public class CSHttpCameraFrameGrabber extends FrameGrabber {

  private static Exception loadingException = null;

  private final String url;
  private final double readTimeout;

  private Mat decoded = null;
  private FrameConverter<Mat> converter = new OpenCVFrameConverter.ToMat();

  private JavaCvSink javaCvSink;
  private HttpCamera httpCamera;

  public CSHttpCameraFrameGrabber(String urlstr, int readTimeout, TimeUnit unit) {
    super();
    this.url = checkNotNull(urlstr, "urlstr");
    this.readTimeout = TimeUnit.MILLISECONDS.convert(readTimeout, unit) / 1000.0;
  }

  public static void tryLoad() throws Exception {
    if (loadingException != null) {
      throw loadingException;
    } else {
      try {
        Loader.load(org.bytedeco.javacpp.opencv_highgui.class);
        CameraServerJNI.Helper.setExtractOnStaticLoad(false);
        CameraServerJNI.forceLoad();
      } catch (Throwable t) {
        throw loadingException = new Exception("Failed to load " + CSHttpCameraFrameGrabber.class, t);
      }
    }
  }

  public HttpCamera getCamera() {
    return this.httpCamera;
  }

  @Override
  public void start() throws Exception {
    javaCvSink = new JavaCvSink("InputCamera");
    httpCamera = new HttpCamera("InputHttp", url);
    javaCvSink.setSource(httpCamera);
  }

  public void stop() throws Exception {
    if (javaCvSink != null) {
      javaCvSink.close();
    }

    if (httpCamera != null) {
      httpCamera.close();
    }

    if (decoded != null) {
      decoded.close();
    }
  }

  @Override
  public void trigger() throws Exception {
  }

  @Override
  public Frame grab() throws Exception {
    try {
      if (decoded == null) {
        decoded = new Mat();
      }
      long frameTime = javaCvSink.grabFrame(decoded, readTimeout);
      if (frameTime > 0) {
        return converter.convert(decoded);
      } else {
        throw new IOException("Frame not read: " + frameTime);
      }
    } catch (IOException e) {
      throw new Exception(e.getMessage(), e);
    }
  }

  @Override
  public void release() throws Exception {
  }
}
