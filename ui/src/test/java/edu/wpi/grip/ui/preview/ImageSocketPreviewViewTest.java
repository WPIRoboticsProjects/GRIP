package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.ui.GRIPUIModule;
import edu.wpi.grip.ui.util.MockGripPlatform;
import edu.wpi.grip.util.Files;
import edu.wpi.grip.util.GRIPCoreTestModule;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.junit.After;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Scene;
import javafx.stage.Stage;

import static org.testfx.api.FxAssert.verifyThat;

public class ImageSocketPreviewViewTest extends ApplicationTest {
    private GRIPCoreTestModule testModule;
    private static final String identifier = "image";

    @Override
    public void start(Stage stage) {
        testModule = new GRIPCoreTestModule();
        testModule.setUp();

        final Injector injector = Guice.createInjector(Modules.override(testModule).with(new GRIPUIModule()));
        final ImageSocketPreviewView imageSocketPreviewView =
                new ImageSocketPreviewView(new MockGripPlatform(new EventBus()),
                        injector.getInstance(OutputSocket.Factory.class)
                                .create(new SocketHint.Builder<>(Mat.class)
                                        .identifier(identifier)
                                        .initialValueSupplier(Files.gompeiJpegFile::createMat)
                                        .build()));
        final Scene scene = new Scene(imageSocketPreviewView);
        stage.setScene(scene);
        stage.show();
    }

    @After
    public void tearDown() {
        testModule.tearDown();
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void testIfImageRenders() {
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(identifier, NodeMatchers.isVisible());
    }
}