package edu.wpi.grip.core.sources;

import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.SourceHasPendingUpdateEvent;
import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.core.observables.Observable;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.DaemonThread;
import edu.wpi.grip.core.util.ExceptionWitness;
import edu.wpi.grip.core.util.Pausable;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A source for a video file input.
 */
@XStreamAlias("grip:VideoFile")
public class VideoFileSource extends Source implements Pausable {

  private final String path;
  private final SocketHint<Mat> imageHint = SocketHints.Outputs.createMatSocketHint("Image");
  private final SocketHint<Number> fpsHint = SocketHints.Outputs.createNumberSocketHint("FPS", 0);
  private final OutputSocket<Mat> imageSocket;
  private final OutputSocket<Number> fpsSocket;
  private final Mat workingMat = new Mat();
  @SuppressWarnings("PMD.LinguisticNaming")
  private final AtomicBoolean isNewFrame = new AtomicBoolean(false);
  private FFmpegFrameGrabber frameGrabber;
  private final OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
  private final EventBus eventBus;
  private final ExecutorService manualGrabberService; // only for manual frame control
  private ScheduledFuture<?> grabberFuture = null;
  private final Observable<Integer> currentFrame = Observable.synchronizedOf(0);
  private final Observable<Boolean> paused = Observable.synchronizedOf(false);
  private volatile int frameCount = -1;

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
    this.manualGrabberService = Executors.newSingleThreadExecutor(DaemonThread::new);
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
      frameCount = frameGrabber.getLengthInFrames();
      if (frameCount <= 1) {
        // Only one frame, no point in scheduling automatic updates to grab the same
        // image over and over
        manualGrabberService.submit(this::grabNextFrame);
      } else {
        grabberFuture = Executors.newSingleThreadScheduledExecutor(DaemonThread::new)
            .scheduleAtFixedRate(
                () -> {
                  if (!isPaused()) {
                    grabNextFrame();
                  }
                },
                0L,
                (long) (1e3 / fps),
                TimeUnit.MILLISECONDS
            );
      }
    } catch (FrameGrabber.Exception e) {
      throw new IOException("Could not open video file " + path, e);
    }
  }

  /**
   * Grabs the next frame from the video, or the first frame
   * if the end of the file has been reached.
   */
  private void grabNextFrame() {
    try {
      Frame frame = frameGrabber.grabFrame();
      if (frame == null) {
        // End of the video file, loop back to the first frame
        frameGrabber.setFrameNumber(0);
        grabNextFrame();
        return;
      }
      Mat m = converter.convert(frame);
      if (m == null) {
        // Try again (I have no idea why this happens)
        grabNextFrame();
        return;
      }
      synchronized (workingMat) {
        m.copyTo(workingMat);
      }
      m.release();
      currentFrame.set(frameGrabber.getFrameNumber()); // best guess
      isNewFrame.set(true);
      eventBus.post(new SourceHasPendingUpdateEvent(this));
    } catch (FrameGrabber.Exception e) {
      getExceptionWitness().flagException(e);
    }
  }

  /**
   * Gets an observable value for the current frame number of the video file this source is
   * providing.
   */
  public Observable<Integer> currentFrameProperty() {
    return currentFrame;
  }

  /**
   * Gets the number of frames in the video file. This is a <i>best guess</i> and may not
   * always be accurate.
   */
  public int getFrameCount() {
    return frameCount;
  }

  /**
   * Sets the frame grabber to the given frame number. This will pause frame grabber from reading
   * successive frames; it can be resumed with {@link #resume()}. This runs asynchronously and
   * will complete at some point in the future after this method is called.
   *
   * @param frameNumber the number of the frame to grab
   *
   * @throws IllegalArgumentException if {@code frameNumber} is negative or exceeds the number of
   *                                  frames in the video file
   */
  public void setFrame(int frameNumber) {
    if (frameNumber < 0) {
      throw new IllegalArgumentException("Negative frame number " + frameNumber);
    }
    if (frameNumber > frameGrabber.getLengthInFrames()) {
      throw new IllegalArgumentException(
          "Frame number too high: " + frameNumber + " > " + frameGrabber.getLengthInFrames());
    }
    pause();
    try {
      frameGrabber.setFrameNumber(frameNumber);
    } catch (FrameGrabber.Exception e) {
      getExceptionWitness().flagException(e, "Could not set frame number");
    }
    manualGrabberService.submit(this::grabNextFrame);
  }

  /**
   * Gets the current frame number.
   *
   * @return the current frame number
   */
  public int getFramePosition() {
    return currentFrame.get();
  }

  @Override
  public Observable<Boolean> pausedProperty() {
    return paused;
  }

  @Subscribe
  public void onSourceRemoved(SourceRemovedEvent sourceRemovedEvent) {
    if (sourceRemovedEvent.getSource() == this) {
      try {
        if (grabberFuture != null && !grabberFuture.isCancelled()) {
          grabberFuture.cancel(true);
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

}
