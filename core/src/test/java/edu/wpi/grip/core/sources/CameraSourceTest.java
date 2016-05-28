package edu.wpi.grip.core.sources;


import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Service;
import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.util.ImageLoadingUtility;
import edu.wpi.grip.core.util.MockExceptionWitness;
import edu.wpi.grip.util.Files;
import edu.wpi.grip.util.GRIPCoreTestModule;
import net.jodah.concurrentunit.Waiter;
import org.bytedeco.javacpp.indexer.Indexer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CameraSourceTest {
    private GRIPCoreTestModule testModule;
    private CameraSource.Factory cameraSourceFactory;
    private CameraSource cameraSourceWithMockGrabber;
    private MockFrameGrabberFactory mockFrameGrabberFactory;
    private OutputSocket.Factory osf;

    @Rule
    public final Timeout timeout = Timeout.seconds(3);


    class MockFrameGrabber extends FrameGrabber {
        private final Frame frame;
        private final Indexer frameIdx;
        private boolean shouldThrowAtStart = false;
        private boolean shouldThrowAtStop = false;

        MockFrameGrabber() {
            this.frame = new Frame(640 + 1, 480, Frame.DEPTH_SHORT, 3);
            this.frameIdx = frame.createIndexer();
            for (int y = 0; y < frameIdx.rows(); y++) {
                for (int x = 0; x < frameIdx.cols(); x++) {
                    for (int z = 0; z < frameIdx.channels(); z++) {
                        frameIdx.putDouble(new int[]{y, x, z}, y + x + z);
                    }
                }
            }
        }

        @Override
        public void start() throws Exception {
            if (shouldThrowAtStart) {
                throw new FrameGrabber.Exception("Throwing on start was enabled!");
            }
        }

        @Override
        public void stop() throws Exception {
            if (shouldThrowAtStop) {
                throw new FrameGrabber.Exception("Throwing on stop was enabled");
            }
        }

        @Override
        public void trigger() throws Exception {

        }

        @Override
        public Frame grab() throws Exception {
            return frame;
        }

        @Override
        public void release() throws Exception {
            this.frameIdx.release();
        }

        public void setShouldThrowAtStart(boolean shouldThrowAtStart) {
            this.shouldThrowAtStart = shouldThrowAtStart;
        }

        public void setShouldThrowAtStop(boolean shouldThrowAtStop) {
            this.shouldThrowAtStop = shouldThrowAtStop;
        }
    }

    class MockFrameGrabberFactory implements CameraSource.FrameGrabberFactory {
        private MockFrameGrabber frameGrabber = new MockFrameGrabber();

        @Override
        public FrameGrabber create(int deviceNumber) {
            return frameGrabber;
        }

        @Override
        public FrameGrabber create(String addressProperty) throws MalformedURLException {
            return frameGrabber;
        }
    }

    @Before
    public void setUp() throws Exception {
        this.testModule = new GRIPCoreTestModule();
        testModule.setUp();
        final Injector injector = Guice.createInjector(testModule);
        this.cameraSourceFactory = injector.getInstance(CameraSource.Factory.class);
        this.osf = injector.getInstance(OutputSocket.Factory.class);

        final EventBus eventBus = new EventBus();
        class UnhandledExceptionWitness {
            @Subscribe
            public void onUnexpectedThrowableEvent(UnexpectedThrowableEvent event) {
                event.handleSafely((throwable, message, isFatal) -> {
                    throwable.printStackTrace();
                });
            }
        }
        eventBus.register(new UnhandledExceptionWitness());
        this.mockFrameGrabberFactory = new MockFrameGrabberFactory();
        this.cameraSourceWithMockGrabber = new CameraSource(
                eventBus,
                osf,
                mockFrameGrabberFactory,
                origin -> new MockExceptionWitness(eventBus, origin),
                0);
    }

    @After
    public void tearDown() throws Exception {
        mockFrameGrabberFactory.frameGrabber.release();
        testModule.tearDown();
    }

    @Test(expected = IOException.class)
    public void testInvalidURLThrowsIOException() throws Exception {
        cameraSourceFactory.create("Not a URL at all!");
        fail("This test should have thrown an exception.");
    }

    @Test
    public void testCallingStopAndStartDoesNotDeadlock() throws Exception {
        assertEquals("Service did not start new", Service.State.NEW, cameraSourceWithMockGrabber.state());
        // Run this a hundred time to ensure that there isn't a situation where this can deadlock
        for(int i = 0; i < 100; i++) {
            cameraSourceWithMockGrabber.startAsync().awaitRunning();
            assertTrue("The camera source was not started after calling startAsync", cameraSourceWithMockGrabber.isRunning());
            cameraSourceWithMockGrabber.stopAsync().awaitTerminated();
            assertFalse("The camera was not stopped after calling stopAsync", cameraSourceWithMockGrabber.isRunning());
        }
    }

    @Test
    public void testStartRethrowsIfFailure() throws Exception {
        mockFrameGrabberFactory.frameGrabber.setShouldThrowAtStart(true);
        // Problems starting should be restarted
        try {
            cameraSourceWithMockGrabber.startAsync().stopAndAwait();
            fail("Should have thrown an exception");
        } catch (IllegalStateException e) {
            assertThat(e.getCause()).isNotNull();
            assertThat(e.getCause()).isInstanceOf(GrabberService.GrabberServiceException.class);
        }

        assertFalse("Camera service has stopped completely", cameraSourceWithMockGrabber.isRunning());
    }

    @Test(expected = GrabberService.GrabberServiceException.class)
    public void testStopRethrowsIfFailure() throws Exception {
        mockFrameGrabberFactory.frameGrabber.setShouldThrowAtStop(true);
        cameraSourceWithMockGrabber.startAsync();

        try {
            cameraSourceWithMockGrabber.stopAsync().awaitTerminated();
        } catch (IllegalStateException expected) {
            Throwables.propagateIfInstanceOf(expected.getCause(), GrabberService.GrabberServiceException.class);
            throw expected;
        }

        fail("This should have thrown an Exception");
    }

    @Test(expected = IllegalStateException.class)
    public void testStartingTwiceShouldThrowIllegalState() throws Exception {
        try {
            try {
                cameraSourceWithMockGrabber.startAsync();
            } catch (RuntimeException e) {
                fail("This should not have failed");
            }
            cameraSourceWithMockGrabber.startAsync();
        } finally {
            cameraSourceWithMockGrabber.stopAsync();
        }
        fail("The test should have failed with an IllegalStateException");
    }

    @Test
    public void testEnsureThatGrabberIsReinitializedWhenStartThrowsException() throws IOException, TimeoutException {
        final String GRABBER_START_MESSAGE = "This is expected to fail this way";
        Waiter waiter1 = new Waiter();
        Waiter waiter2 = new Waiter();
        Waiter waiter3 = new Waiter();
        Queue<Waiter> waiterQueue = new LinkedList<>(Arrays.asList(waiter1, waiter2, waiter3));

        CameraSource source = new CameraSource(new EventBus(), osf, new CameraSource.FrameGrabberFactory() {
            @Override
            public FrameGrabber create(int deviceNumber) {
                return new SimpleMockFrameGrabber() {
                    @Override
                    public void start() throws Exception {
                        if (!waiterQueue.isEmpty()) {
                            waiterQueue.poll().resume();
                        }
                        throw new FrameGrabber.Exception(GRABBER_START_MESSAGE);
                    }
                };
            }

            @Override
            public FrameGrabber create(String addressProperty) throws MalformedURLException {
                throw new AssertionError("This should not be called");
            }
        }, MockExceptionWitness.MOCK_FACTORY, 0);

        source.startAsync();
        waiter1.await();
        waiter2.await();
        waiter3.await();
        try {
            source.stopAndAwait();
            fail("This should have failed");
        } catch(IllegalStateException e) {
            assertThat(e.getCause()).isNotNull();
            assertThat(e.getCause().getCause()).isNotNull();
            assertThat(e.getCause().getCause()).hasMessage(GRABBER_START_MESSAGE);
        }
    }

    @Test
    public void testFrameRateUpdatesWithGrabSpeed() throws IOException, InterruptedException, TimeoutException {
        Waiter waiter1 = new Waiter();
        Waiter waiter2 = new Waiter();
        Queue<Waiter> waiterQueue = new LinkedList<>(Arrays.asList(waiter1, waiter2));

        Mat image = new Mat();
        ImageLoadingUtility.loadImage(Files.gompeiJpegFile.file.getPath(), image);
        CameraSource source = new CameraSource(new EventBus(), osf, new CameraSource.FrameGrabberFactory() {
            @Override
            public FrameGrabber create(int deviceNumber) {
                return new SimpleMockFrameGrabber() {
                    private OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
                    @Override
                    public Frame grab() throws Exception {
                        try {
                            Thread.sleep(3);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new FrameGrabber.Exception("Thread interrupted", e);
                        }
                        if (!waiterQueue.isEmpty()){
                            waiterQueue.poll().resume();
                        }
                        return converter.convert(image);
                    }
                };
            }

            @Override
            public FrameGrabber create(String addressProperty) throws MalformedURLException {
                throw new AssertionError("This should not be called");
            }
        }, MockExceptionWitness.MOCK_FACTORY, 0);

        source.startAsync().awaitRunning();

        waiter2.await();
        // Move the value over to the socket.
        source.updateOutputSockets();

        assertNotEquals("The frame rate was not updated when the camera was running",
                Double.valueOf(0), source.createOutputSockets().get(1).getValue().get());

        try {
            source.stopAndAwait();
        } catch (IllegalStateException e) {
            // This could happen if the thread is interrupted.
        }
    }
}