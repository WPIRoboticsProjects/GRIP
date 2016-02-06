package edu.wpi.grip.core.sources;


import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Service;
import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.wpi.grip.core.GRIPCoreModule;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.util.MockExceptionWitness;
import org.bytedeco.javacpp.indexer.Indexer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.IOException;
import java.net.MalformedURLException;

import static org.junit.Assert.*;

public class CameraSourceTest {
    private Injector injector;
    private CameraSource.Factory cameraSourceFactory;

    private EventBus eventBus;
    private CameraSource cameraSourceWithMockGrabber;
    private MockFrameGrabberFactory mockFrameGrabberFactory;

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
        this.injector = Guice.createInjector(new GRIPCoreModule());
        this.cameraSourceFactory = injector.getInstance(CameraSource.Factory.class);

        this.eventBus = new EventBus();
        class UnhandledExceptionWitness {
            @Subscribe
            public void onUnexpectedThrowableEvent(UnexpectedThrowableEvent event) {
                event.handleSafely((throwable, message, isFatal) -> {
                    throwable.printStackTrace();
                });
            }
        }
        this.eventBus.register(new UnhandledExceptionWitness());
        this.mockFrameGrabberFactory = new MockFrameGrabberFactory();
        this.cameraSourceWithMockGrabber = new CameraSource(
                eventBus,
                mockFrameGrabberFactory,
                origin -> new MockExceptionWitness(eventBus, origin),
                0);
    }

    @After
    public void tearDown() throws Exception {
        mockFrameGrabberFactory.frameGrabber.release();
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
        // Problems starting should be swallowed
        cameraSourceWithMockGrabber.startAsync().stopAndAwait();
        assertFalse("Camera service has stopped completely", cameraSourceWithMockGrabber.isRunning());
    }

    @Test(expected = FrameGrabber.Exception.class)
    public void testStopRethrowsIfFailure() throws Exception {
        mockFrameGrabberFactory.frameGrabber.setShouldThrowAtStop(true);
        cameraSourceWithMockGrabber.startAsync();

        try {
            cameraSourceWithMockGrabber.stopAsync().awaitTerminated();
        } catch (IllegalStateException expected) {
            Throwables.propagateIfInstanceOf(expected.getCause(), FrameGrabber.Exception.class);
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

}