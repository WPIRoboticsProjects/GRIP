package edu.wpi.grip.core.sources;


import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.SourceHasPendingUpdateEvent;
import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.ExceptionWitness;
import edu.wpi.grip.core.util.service.AutoRestartingService;
import edu.wpi.grip.core.util.service.LoggingListener;
import edu.wpi.grip.core.util.service.RestartableService;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.VideoInputFrameGrabber;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Provides a way to generate a constantly updated {@link Mat} from a camera
 */
@XStreamAlias(value = "grip:Camera")
public class CameraSource extends Source implements RestartableService {

    /**
     * The path that Axis cameras stream MJPEG videos from.  Although any URL can be supplied
     * {@link CameraSource.Factory#create(String)}, allowing this to work with basically any network video stream, this
     * default path allows the Axis M1011 cameras used in FRC to work when only an IP address is supplied.
     */
    public static final String DEFAULT_IP_CAMERA_PATH = "/mjpg/video.mjpg";
    private static final int
            /**
             * Connecting to a device can take the most time.
             * This should have a little bit of leeway.
             *
             * On a fairly decent computer with a great internet connection 7 seconds is more than enough.
             * This value has been doubled to ensure that people running computers that may be older
             * or have firewalls that will slow down connecting can still use the device.
             */
            IP_CAMERA_CONNECTION_TIMEOUT = 14;
    private static final int
            /**
             * Reading from an existing connection shouldn't take that long.
             * If it does we should really give up and try to reconnect.
             */
            IP_CAMERA_READ_TIMEOUT = 5;
    private static final TimeUnit IP_CAMERA_TIMEOUT_UNIT = TimeUnit.SECONDS;

    private final static String DEVICE_NUMBER_PROPERTY = "deviceNumber";
    private final static String ADDRESS_PROPERTY = "address";
    private static final Logger logger = Logger.getLogger(CameraSource.class.getName());

    private final EventBus eventBus;
    private final String name;

    private final Properties properties;

    private final SocketHint<Mat> imageOutputHint = SocketHints.Inputs.createMatSocketHint("Image", true);
    private final SocketHint<Number> frameRateOutputHint = SocketHints.createNumberSocketHint("Frame Rate", 0);
    private final OutputSocket<Mat> frameOutputSocket;
    private final OutputSocket<Number> frameRateOutputSocket;
    private final Supplier<FrameGrabber> grabberSupplier;
    private final AtomicBoolean isNewFrame = new AtomicBoolean(false);
    private final Mat currentFrameTransferMat = new Mat();
    private final AutoRestartingService cameraService;
    private volatile double frameRate = 0;

    public interface Factory {
        CameraSource create(int deviceNumber) throws IOException;

        CameraSource create(String address) throws IOException;

        CameraSource create(Properties properties) throws IOException;
    }

    /**
     * Allows for the creation of a frame grabber using either a device number or URL string address.
     */
    public interface FrameGrabberFactory {
        FrameGrabber create(int deviceNumber);

        FrameGrabber create(String addressProperty) throws MalformedURLException;
    }

    public static class FrameGrabberFactoryImpl implements FrameGrabberFactory {
        FrameGrabberFactoryImpl() { /* no-op */ }

        public FrameGrabber create(int deviceNumber) {
            // On Windows, videoInput is much more reliable for webcam capture.  On other platforms, OpenCV's frame
            // grabber class works fine.
            if (StandardSystemProperty.OS_NAME.value().contains("Windows")) {
                return new VideoInputFrameGrabber(deviceNumber);
            } else {
                return new OpenCVFrameGrabber(deviceNumber);
            }
        }

        public FrameGrabber create(String addressProperty) throws MalformedURLException {
            // If no path was specified in the URL (ie: it was something like http://10.1.90.11/), use the default path
            // for Axis M1011 cameras.
            if (new URL(addressProperty).getPath().length() <= 1) {
                addressProperty += DEFAULT_IP_CAMERA_PATH;
            }
            return new IPCameraFrameGrabber(
                    addressProperty,
                    IP_CAMERA_CONNECTION_TIMEOUT,
                    IP_CAMERA_READ_TIMEOUT,
                    IP_CAMERA_TIMEOUT_UNIT);
        }
    }

    /**
     * Creates a camera source that can be used as an input to a pipeline
     *
     * @param eventBus     The EventBus to attach to
     * @param deviceNumber The device number of the webcam
     */
    @AssistedInject
    CameraSource(
            final EventBus eventBus,
            final OutputSocket.Factory outputSocketFactory,
            final FrameGrabberFactory grabberFactory,
            final ExceptionWitness.Factory exceptionWitnessFactory,
            @Assisted final int deviceNumber) throws IOException {
        this(eventBus, outputSocketFactory, grabberFactory, exceptionWitnessFactory, createProperties(deviceNumber));
    }

    /**
     * Creates a camera source that can be used as an input to a pipeline
     *
     * @param eventBus The EventBus to attach to
     * @param address  A URL to stream video from an IP camera
     */
    @AssistedInject
    CameraSource(
            final EventBus eventBus,
            final OutputSocket.Factory outputSocketFactory,
            final FrameGrabberFactory grabberFactory,
            final ExceptionWitness.Factory exceptionWitnessFactory,
            @Assisted final String address) throws IOException {
        this(eventBus, outputSocketFactory, grabberFactory, exceptionWitnessFactory, createProperties(address));
    }

    /**
     * Used for serialization
     */
    @AssistedInject
    CameraSource(
            final EventBus eventBus,
            final OutputSocket.Factory outputSocketFactory,
            final FrameGrabberFactory grabberFactory,
            final ExceptionWitness.Factory exceptionWitnessFactory,
            @Assisted final Properties properties) throws MalformedURLException {
        super(exceptionWitnessFactory);
        this.eventBus = eventBus;
        this.frameOutputSocket = outputSocketFactory.create(imageOutputHint);
        this.frameRateOutputSocket = outputSocketFactory.create(frameRateOutputHint);
        this.properties = properties;

        final String deviceNumberProperty = properties.getProperty(DEVICE_NUMBER_PROPERTY);
        final String addressProperty = properties.getProperty(ADDRESS_PROPERTY);

        if (deviceNumberProperty != null) {
            final int deviceNumber = Integer.parseInt(deviceNumberProperty);
            this.name = "Webcam " + deviceNumber;
            this.grabberSupplier = () -> grabberFactory.create(deviceNumber);
        } else if (addressProperty != null) {
            this.name = "IP Camera " + new URL(addressProperty).getHost();
            this.grabberSupplier = () -> {
                try {
                    return grabberFactory.create(addressProperty);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException(e.getMessage(), e);
                }
            };
        } else {
            throw new IllegalArgumentException("Cannot initialize CameraSource without either a device number or " +
                    "address");
        }

        /* This must be initialized in the constructor otherwise the grabber supplier won't be present */
        this.cameraService = new AutoRestartingService<>(() -> new GrabberService(name, grabberSupplier, new CameraSourceUpdater() {
            @Override
            public void setFrameRate(double value) {
                CameraSource.this.frameRate = value;
                isNewFrame.set(true);
            }

            @Override
            public void copyNewMat(Mat matToCopy) {
                synchronized (CameraSource.this.currentFrameTransferMat) {
                    matToCopy.copyTo(CameraSource.this.currentFrameTransferMat);
                }
                isNewFrame.set(true);
            }

            @Override
            public void updatesComplete() {
                eventBus.post(new SourceHasPendingUpdateEvent(CameraSource.this));
            }
        }, getExceptionWitness()::clearException));
        this.cameraService.addListener(new Listener() {
            @Override
            public void failed(State from, Throwable failure) {
                if (failure instanceof GrabberService.GrabberServiceException) {
                    // These are expected exceptions. Handle them by flagging an exception
                    getExceptionWitness().flagException((GrabberService.GrabberServiceException) failure, "Camera service crashed");
                } else {
                    // Rethrow as an uncaught exception if this is not an exception we expected.
                    Optional.ofNullable(Thread.getDefaultUncaughtExceptionHandler())
                            .ifPresent(handler -> handler.uncaughtException(Thread.currentThread(), failure));
                }
            }
        }, MoreExecutors.directExecutor());
        this.cameraService.addListener(new LoggingListener(logger, CameraSource.class), MoreExecutors.directExecutor());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<OutputSocket> createOutputSockets() {
        return ImmutableList.of(
                frameOutputSocket,
                frameRateOutputSocket
        );
    }

    @Override
    protected boolean updateOutputSockets() {
        // We have a new frame then we need to update the socket value
        if (isNewFrame.compareAndSet(true, false)) {
            // The camera frame thread should not try to modify the transfer mat while it is being written to the pipeline
            synchronized (currentFrameTransferMat) {
                currentFrameTransferMat.copyTo(frameOutputSocket.getValue().get());
            }
            frameOutputSocket.setValueOptional(frameOutputSocket.getValue());

            // Update the frame rate value
            frameRateOutputSocket.setValue(frameRate);
            // We have updated output sockets
            return true;
        } else {
            return false; // No output sockets were updated
        }
    }

    @Override
    public Properties getProperties() {
        return this.properties;
    }

    @Override
    public void initialize() {
        startAsync();
    }

    /**
     * Starts the service that runs the camera source
     */
    @Override
    public CameraSource startAsync() {
        cameraService.startAsync();
        return this;
    }

    @Override
    public boolean isRunning() {
        return cameraService.isRunning();
    }


    /**
     * Stops the service that is running camera source.
     */
    @Override
    public CameraSource stopAsync() {
        cameraService.stopAsync();
        return this;
    }

    @Override
    public void stopAndAwait() {
        stopAsync().cameraService.stopAndAwait();
    }

    @Override
    public void stopAndAwait(long timeout, TimeUnit unit) throws TimeoutException {
        stopAsync().cameraService.stopAndAwait(timeout, unit);
    }

    @Override
    public void awaitRunning() {
        cameraService.awaitRunning();
    }

    @Override
    public void awaitRunning(long timeout, TimeUnit unit) throws TimeoutException {
        cameraService.awaitRunning(timeout, unit);
    }

    @Override
    public void awaitTerminated() {
        cameraService.awaitTerminated();
    }

    @Override
    public void awaitTerminated(long timeout, TimeUnit unit) throws TimeoutException {
        cameraService.awaitTerminated(timeout, unit);
    }

    @Override
    public Throwable failureCause() {
        return cameraService.failureCause();
    }

    @Override
    public void addListener(Listener listener, Executor executor) {
        cameraService.addListener(listener, executor);
    }

    @Override
    public State state() {
        return cameraService.state();
    }

    @Subscribe
    public void onSourceRemovedEvent(SourceRemovedEvent event) throws InterruptedException, TimeoutException, IOException {
        if (event.getSource() == this) {
            try {
                this.stopAsync();
            } finally {
                this.eventBus.unregister(this);
            }
        }
    }

    private static Properties createProperties(String address) {
        final Properties properties = new Properties();
        properties.setProperty(ADDRESS_PROPERTY, address);
        return properties;
    }

    private static Properties createProperties(int deviceNumber) {
        final Properties properties = new Properties();
        properties.setProperty(DEVICE_NUMBER_PROPERTY, Integer.toString(deviceNumber));
        return properties;
    }
}
