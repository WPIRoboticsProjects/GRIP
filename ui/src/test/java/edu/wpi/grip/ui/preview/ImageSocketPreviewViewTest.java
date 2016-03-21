package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.ui.util.MockGripPlatform;
import edu.wpi.grip.util.Files;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import static org.testfx.api.FxAssert.verifyThat;

public class ImageSocketPreviewViewTest extends ApplicationTest {
    private static final String identifier = "image";
    @Override
    public void start(Stage stage) {
        final ImageSocketPreviewView imageSocketPreviewView =
                new ImageSocketPreviewView(new MockGripPlatform(new EventBus()),
                        new OutputSocket<>(
                                new EventBus(), new SocketHint.Builder<>(Mat.class)
                                .identifier(identifier)
                                .initialValueSupplier(Files.gompeiJpegFile::createMat)
                                .build()));
        final Scene scene = new Scene(imageSocketPreviewView);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void testIfImageRenders() {
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(identifier, NodeMatchers.isVisible());
    }
}