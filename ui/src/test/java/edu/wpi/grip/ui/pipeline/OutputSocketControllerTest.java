package edu.wpi.grip.ui.pipeline;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sockets.MockOutputSocket;
import edu.wpi.grip.ui.util.TestAnnotationFXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class OutputSocketControllerTest extends ApplicationTest {

    private MockOutputSocket outputSocket;
    private InitiallyPreviewedOutputSocket initiallyPreviewedOutputSocket;
    private OutputSocketController defaultOutputSocketController;
    private OutputSocketController initiallyPreviewedOutputSocketController;

    private class InitiallyPreviewedOutputSocket extends MockOutputSocket {
        public InitiallyPreviewedOutputSocket(String socketName) {
            super(socketName);
            this.setPreviewed(true);
        }
    }

    @Override
    public void start(Stage stage) {
        outputSocket = new MockOutputSocket("Mock Output");
        initiallyPreviewedOutputSocket = new InitiallyPreviewedOutputSocket("Initially previewed");

        final SocketHandleView.Factory socketHandleFactory
                = socket -> new SocketHandleView(new EventBus(), null, null, new SocketHandleView.SocketDragService(), socket);
        defaultOutputSocketController =
                new OutputSocketController(socketHandleFactory, outputSocket);
        initiallyPreviewedOutputSocketController =
                new OutputSocketController(socketHandleFactory, initiallyPreviewedOutputSocket);

        final GridPane gridPane = new GridPane();
        gridPane.add(TestAnnotationFXMLLoader.load(defaultOutputSocketController), 0, 0);
        gridPane.add(TestAnnotationFXMLLoader.load(initiallyPreviewedOutputSocketController), 0, 1);

        final Scene scene = new Scene(gridPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testClickOnButton() throws Exception {
        clickOn(defaultOutputSocketController.previewButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue("The output socket was not made previewed when button was clicked", outputSocket.isPreviewed());
    }

    @Test
    public void testClickOnButtonTwice() throws Exception {
        clickOn(defaultOutputSocketController.previewButton());
        clickOn(defaultOutputSocketController.previewButton());
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse("The output socket was not made not previewed when button was clicked twice", outputSocket.isPreviewed());
    }

    @Test
    public void testInitiallyPreviewedOutputSocket() {
        assertTrue("The preview button did not initialize selected", initiallyPreviewedOutputSocketController.previewButton().isSelected());
    }

}