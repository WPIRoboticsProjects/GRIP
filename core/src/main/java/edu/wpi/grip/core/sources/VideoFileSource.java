package edu.wpi.grip.core.sources;

import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.SourceHasPendingUpdateEvent;
import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.ExceptionWitness;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A source for a video file input.
 */
@XStreamAlias("grip:VideoFile")
public class VideoFileSource extends Source {

  private final String path;
  private final SocketHint<Mat> imageHint = SocketHints.Outputs.createMatSocketHint("Image");
  private final SocketHint<Number> fpsHint = SocketHints.Outputs.createNumberSocketHint("FPS", 0);
  private final OutputSocket<Mat> imageSocket;
  private final OutputSocket<Number> fpsSocket;
  private final Mat workingMat = new Mat();
  private final AtomicBoolean isNewFrame = new AtomicBoolean(false);
  private FFmpegFrameGrabber frameGrabber;
  private final OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
  private final EventBus eventBus;
  private VideoCaptureThread captureThread;

  @AssistedInject
  VideoFileSource(OutputSocket.Factory osf,
                  ExceptionWitness.Factory exceptionWitnessFactory,
                  EventBus eventBus,
                  @Assisted Properties properties) {
    this(osf, exceptionWitnessFactory, eventBus, properties.getProperty("path"));
  }

  @AssistedInject
  VideoFileSource(OutputSocket.Factory osf,
                  ExceptionWitness.Factory exceptionWitnessFactory,
                  EventBus eventBus,
                  @Assisted File file) {
    this(osf, exceptionWitnessFactory, eventBus, file.getAbsolutePath());
  }

  private VideoFileSource(OutputSocket.Factory osf,
                          ExceptionWitness.Factory exceptionWitnessFactory,
                          EventBus eventBus,
                          String path) {
    super(exceptionWitnessFactory);
    this.eventBus = eventBus;
    this.path = path;
    this.imageSocket = osf.create(imageHint);
    this.fpsSocket = osf.create(fpsHint);
  }

  @Override
  public String getName() {
    return new File(path).getName();
  }

  @Override
  protected List<OutputSocket> createOutputSockets() {
    return ImmutableList.of(
        imageSocket,
        fpsSocket
    );
  }

  @Override
  protected boolean updateOutputSockets() {
    if (isNewFrame.compareAndSet(true, false)) {
      // New frame, update outputs
      synchronized (workingMat) {
        workingMat.copyTo(imageSocket.getValue().get());
      }
      imageSocket.setValue(imageSocket.getValue().get()); // force the socket to update
      return true;
    } else {
      // No new frame, no update
      return false;
    }
  }

  @Override
  public Properties getProperties() {
    Properties p = new Properties();
    p.put("path", path);
    return p;
  }

  @Override
  public void initialize() throws IOException {
    try {
      frameGrabber = new FFmpegFrameGrabber(path);
      frameGrabber.start();
      final double fps = frameGrabber.getFrameRate();
      fpsSocket.setValue(fps);
      captureThread = new VideoCaptureThread(fps, path);
      captureThread.start();
    } catch (FrameGrabber.Exception e) {
      throw new IOException("Could not open video file " + path, e);
    }
  }

  @Subscribe
  public void onSourceRemoved(SourceRemovedEvent sourceRemovedEvent) {
    if (sourceRemovedEvent.getSource() == this) {
      try {
        if (captureThread != null) {
          captureThread.quit();
        }
        frameGrabber.stop();
      } catch (FrameGrabber.Exception e) {
        getExceptionWitness().flagException(e, "Exception when stopping frame grabber");
      } finally {
        eventBus.unregister(this);
      }
    }
  }

  public interface Factory {
    VideoFileSource create(File file);

    VideoFileSource create(Properties props);
  }

  private class VideoCaptureThread extends Thread {

    private final double fps;
    private boolean doRun = true;

    private VideoCaptureThread(double fps, String path) {
      super("VideoFileCaptureThread-" + path);
      setDaemon(true);
      this.fps = fps;
    }

    @Override
    public void run() {
      while (doRun) {
        try {
          Frame frame = frameGrabber.grabFrame();
          if (frame == null) {
            // End of the video file, loop back to the first frame
            frameGrabber.setFrameNumber(0);
            continue;
          }
          Mat m = converter.convert(frame);
          if (m == null) {
            // Try again (I have no idea why this happens)
            continue;
          }
          synchronized (workingMat) {
            m.copyTo(workingMat);
          }
          m.release();
          isNewFrame.set(true);
          eventBus.post(new SourceHasPendingUpdateEvent(VideoFileSource.this));
          Thread.sleep((long) (1e3 / fps));
        } catch (FrameGrabber.Exception e) {
          getExceptionWitness().flagException(e);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          getExceptionWitness().flagException(e, "Interrupted while waiting for next frame");
        }
      }
    }

    public void quit() {
      doRun = false;
    }

  }

}
