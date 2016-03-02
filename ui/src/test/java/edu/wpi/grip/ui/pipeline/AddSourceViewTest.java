package edu.wpi.grip.ui.pipeline;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.MockCameraSource;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;

@RunWith(Enclosed.class)
public class AddSourceViewTest {

    /**
     * Tests what happens when a source is created and started successfully
     */
    public static class AddSourceViewNoExceptionsTest extends ApplicationTest {

        private EventBus eventBus;
        private AddSourceView addSourceView;
        private MockCameraSourceFactory mockCameraSourceFactory;

        class MockCameraSourceFactory implements CameraSource.Factory {
            private final EventBus eventBus;
            private Optional<CameraSource> lastSourceCreated = Optional.empty();

            MockCameraSourceFactory(EventBus eventBus) {
                this.eventBus = eventBus;
            }

            @Override
            public CameraSource create(int deviceNumber) throws IOException {
                return assignLastCreated(new MockCameraSource(eventBus, deviceNumber));
            }

            @Override
            public CameraSource create(String address) throws IOException {
                return assignLastCreated(new MockCameraSource(eventBus, address));
            }

            @Override
            public CameraSource create(Properties properties) throws IOException {
                return null;
            }

            private MockCameraSource assignLastCreated(MockCameraSource source) {
                lastSourceCreated = Optional.of(source);
                return source;
            }
        }

        @Override
        public void start(Stage stage) {
            this.eventBus = new EventBus("Test Event Bus");
            this.mockCameraSourceFactory = new MockCameraSourceFactory(eventBus);

            addSourceView = new AddSourceView(eventBus, null, null, mockCameraSourceFactory);

            final Scene scene = new Scene(addSourceView, 800, 600);
            stage.setScene(scene);
            stage.show();
        }

        @After
        public void after() {
            // Ensuer that all of the dialogs that were created get closed afterward.
            addSourceView.closeDialogs();
        }


        @Test
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        public void testClickOnCreateWebCameraOpensDialog() throws Exception {
            clickOn(addSourceView.getWebcamButton());
            WaitForAsyncUtils.waitForFxEvents();
            verifyThat("." + AddSourceView.SOURCE_DIALOG_STYLE_CLASS, NodeMatchers.isVisible());
        }

        @Test
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        public void testClickOnCreateIPCameraOpensDialog() throws Exception {
            clickOn(addSourceView.getIpcamButton());
            WaitForAsyncUtils.waitForFxEvents();
            verifyThat("." + AddSourceView.SOURCE_DIALOG_STYLE_CLASS, NodeMatchers.isVisible());
        }

        @Test
        public void testCreatesSourceStarted() throws Exception {
            // When
            clickOn(addSourceView.getWebcamButton());
            WaitForAsyncUtils.waitForFxEvents();
            verifyThat("." + AddSourceView.SOURCE_DIALOG_STYLE_CLASS, NodeMatchers.isVisible());

            clickOn("OK");
            WaitForAsyncUtils.waitForFxEvents();

            // Then
            Optional<CameraSource> cameraSource = mockCameraSourceFactory.lastSourceCreated;
            assertTrue("A source was not constructed", cameraSource.isPresent());
            assertTrue("A source was not created started", cameraSource.get().isRunning());
            verifyThat("." + AddSourceView.SOURCE_DIALOG_STYLE_CLASS, NodeMatchers.isNull());
        }
    }

    /**
     * Tests what happens when the source being created and startedthrows an exception.
     */
    public static class AddSourceViewWithExceptionsTest extends ApplicationTest {
        private EventBus eventBus;
        private AddSourceView addSourceView;
        private MockCameraSourceFactory mockCameraSourceFactory;

        class MockCameraSourceFactory implements CameraSource.Factory {
            private final EventBus eventBus;
            private Optional<CameraSource> lastSourceCreated = Optional.empty();

            MockCameraSourceFactory(EventBus eventBus) {
                this.eventBus = eventBus;
            }

            @Override
            public CameraSource create(int deviceNumber) throws IOException {
                return assignLastCreated(new MockCameraSource(eventBus, deviceNumber) {
                    @Override
                    public MockCameraSource startAsync() {
                        return this;
                    }
                });
            }

            @Override
            public CameraSource create(String address) throws IOException {
                return assignLastCreated(new MockCameraSource(eventBus, address) {
                    @Override
                    public MockCameraSource startAsync() {
                        return this;
                    }
                });
            }

            @Override
            public CameraSource create(Properties properties) throws IOException {
                return null;
            }

            private MockCameraSource assignLastCreated(MockCameraSource source) {
                lastSourceCreated = Optional.of(source);
                return source;
            }
        }

        @Override
        public void start(Stage stage) {
            this.eventBus = new EventBus("Test Event Bus");
            this.mockCameraSourceFactory = new MockCameraSourceFactory(eventBus);

            addSourceView = new AddSourceView(eventBus, null, null, mockCameraSourceFactory);

            final Scene scene = new Scene(addSourceView, 800, 600);
            stage.setScene(scene);
            stage.show();
        }

        @After
        public void after() {
            // Ensuer that all of the dialogs that were created get closed afterward.
            addSourceView.closeDialogs();
        }

        @Test
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        public void testWhenStartFailsDialogStillCloses() throws Exception {
            // When
            clickOn(addSourceView.getWebcamButton());
            WaitForAsyncUtils.waitForFxEvents();

            clickOn("OK");

            WaitForAsyncUtils.waitForFxEvents();

            // The dialog should not have closed because the source wasn't started
            verifyThat("." + AddSourceView.SOURCE_DIALOG_STYLE_CLASS, NodeMatchers.isNull());
        }

        @Test
        public void testCreatesSourceStartedFails() throws Exception {
            // When
            clickOn(addSourceView.getWebcamButton());
            WaitForAsyncUtils.waitForFxEvents();
            verifyThat("." + AddSourceView.SOURCE_DIALOG_STYLE_CLASS, NodeMatchers.isVisible());

            clickOn("OK");
            WaitForAsyncUtils.waitForFxEvents();

            // Then
            Optional<CameraSource> cameraSource = mockCameraSourceFactory.lastSourceCreated;
            assertTrue("A source was not constructed", cameraSource.isPresent());
            assertFalse("A source was not created but should not have been started", cameraSource.get().isRunning());
        }

    }

}