package edu.wpi.grip.core.sources;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.junit.Test;

import java.util.function.Supplier;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertTrue;
import static org.python.icu.impl.Assert.fail;

public class GrabberServiceTest {
    private static class SimpleUpdater implements CameraSourceUpdater {
        @Override
        public void setFrameRate(double value) {
            /* no-op */
        }

        @Override
        public void copyNewMat(Mat matToCopy) {
            /* no-op */
        }

        @Override
        public void updatesComplete() {
            /* no-op */
        }
    }

    static class ConstructorThrowingFrameGrabber extends SimpleMockFrameGrabber {
        private static final String CONSTRUCTOR_EXCEPTION_MESSAGE = "This should be thrown when the constructor is called.";

        ConstructorThrowingFrameGrabber() {
            super();
            throw new IllegalStateException(CONSTRUCTOR_EXCEPTION_MESSAGE);
        }
    }

    private static GrabberService createSimpleGrabberService(Supplier<FrameGrabber> frameGrabberSupplier) {
        return createSimpleGrabberService(frameGrabberSupplier, new SimpleUpdater());
    }

    private static GrabberService createSimpleGrabberService(Supplier<FrameGrabber> frameGrabberSupplier, CameraSourceUpdater updater) {
        return new GrabberService("",
                frameGrabberSupplier,
                updater,
                () -> {});
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void testConstructorDoesNotThrowExceptionIfFrameGrabberExceptionDoes() {
        new GrabberService("",
                () -> new ConstructorThrowingFrameGrabber(),
                new SimpleUpdater(),
                () -> {});
        // This is all we are testing. This should just work without a problem.
    }

    @Test(expected = IllegalStateException.class)
    public void testStartUpRethrowsConstructorException() throws GrabberService.GrabberServiceException {
        try {
            final GrabberService grabberService = createSimpleGrabberService(ConstructorThrowingFrameGrabber::new);
            grabberService.startUp();
            fail("This should have thrown an exception");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage(ConstructorThrowingFrameGrabber.CONSTRUCTOR_EXCEPTION_MESSAGE);
            throw e;
        }
        fail("This should have rethrown an exception");
    }

    @Test(expected = GrabberService.GrabberServiceException.class)
    public void testStartUpRethrowsFrameGrabberExceptionAsGrabberServiceException() throws GrabberService.GrabberServiceException {
        final String exceptionMessage = "Start: Kaboom!";
        try {
            final GrabberService grabberService = createSimpleGrabberService(
                    () -> new SimpleMockFrameGrabber() {
                        @Override
                        public void start() throws FrameGrabber.Exception {
                            throw new FrameGrabber.Exception(exceptionMessage);
                        }
                    });
            grabberService.startUp();
            fail("Should have thrown an exception when starting");
        } catch (GrabberService.GrabberServiceException e) {
            assertThat(e.getCause()).hasMessage(exceptionMessage);
            throw e;
        }
        fail("Should have rethrown the exception in the catch block");
    }


    @Test(expected = GrabberService.GrabberServiceException.class)
    public void testRunOnceThrowsExceptionWhenGrabThrowsException() throws GrabberService.GrabberServiceException {
        final String exceptionMessage = "Grab: Kaboom!";
        try {
            final GrabberService grabberService = createSimpleGrabberService(
                    () -> new SimpleMockFrameGrabber() {
                        @Override
                        public Frame grab() throws FrameGrabber.Exception {
                            throw new FrameGrabber.Exception(exceptionMessage);
                        }
                    });
            try {
                grabberService.startUp();
            } catch (GrabberService.GrabberServiceException e) {
                throw new IllegalStateException("This test should not fail in this way", e);
            }

            grabberService.runOneGrab(null, null);

            fail("Should have thrown an exception when running one grab");
        } catch (GrabberService.GrabberServiceException e) {
            assertThat(e.getCause()).hasMessage(exceptionMessage);
            throw e;
        }
        fail("Should have rethrown an exception in the catch block");
    }

    @Test(expected = GrabberService.GrabberServiceException.class)
    public void testRunOnceThrowsExceptionWhenNullFrameIsReturned() throws GrabberService.GrabberServiceException {
        try {
            final GrabberService grabberService = createSimpleGrabberService(
                    () -> new SimpleMockFrameGrabber() {
                        @Override
                        public Frame grab() {
                            return null;
                        }
                    });
            try {
                grabberService.startUp();
            } catch (GrabberService.GrabberServiceException e) {
                throw new IllegalStateException("This test should not fail in this way", e);
            }

            grabberService.runOneGrab(new OpenCVFrameConverter.ToMat(), null);

            fail("Should have thrown an exception when running one grab");
        } catch (GrabberService.GrabberServiceException e) {
            assertThat(e.getCause()).isNull();
            throw e;
        }
        fail("Should have rethrown an exception in the catch block");
    }

    @Test(expected = GrabberService.GrabberServiceException.class)
    public void testRunOnceThrowsExceptionWhenEmptyFrameIsReturned() throws GrabberService.GrabberServiceException {
        try {
            final GrabberService grabberService = createSimpleGrabberService(
                    () -> new SimpleMockFrameGrabber() {
                        @Override
                        public Frame grab() {
                            return new Frame();
                        }
                    });
            try {
                grabberService.startUp();
            } catch (GrabberService.GrabberServiceException e) {
                throw new IllegalStateException("This test should not fail in this way", e);
            }

            // When grab is called, just return an empty mat no matter what.
            grabberService.runOneGrab(new OpenCVFrameConverter.ToMat() {
                @Override
                public Mat convert(Frame frame) {
                    return new Mat();
                }
            }, null);

            fail("Should have thrown an exception when running one grab because the mat returned was empty");
        } catch (GrabberService.GrabberServiceException e) {
            assertThat(e.getCause()).isNull();
            throw e;
        }
        fail("Should have rethrown an exception in the catch block");
    }

    @Test
    public void testEnsureThatUpdaterIsRunEvenIfExceptionIsThrownInShutDown() {
        final String STOP_EXCEPTION = "This should be thrown but then caught";
        final boolean[] updateWasCalled = {false};
        final GrabberService grabberService = createSimpleGrabberService(() -> new SimpleMockFrameGrabber() {
            @Override
            @SuppressWarnings("PMD.SignatureDeclareThrowsException")
            public void stop() throws Exception {
                throw new FrameGrabber.Exception(STOP_EXCEPTION);
            }
        }, new SimpleUpdater() {
            @Override
            public void updatesComplete() {
                updateWasCalled[0] = true;
            }
        });
        try {
            try {
                grabberService.startUp();
            } catch (GrabberService.GrabberServiceException e) {
                throw new AssertionError("This should not have failed", e);
            }
            grabberService.shutDown();
            fail("This should have thrown an exception");
        } catch (GrabberService.GrabberServiceException e) {
            assertTrue("updatesComplete was not called", updateWasCalled[0]);
            assertThat(e.getCause()).isNotNull();
            assertThat(e.getCause()).hasMessage(STOP_EXCEPTION);
            assertThat(e.getCause()).isInstanceOf(FrameGrabber.Exception.class);
        }
    }
}