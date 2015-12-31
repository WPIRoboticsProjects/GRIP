package edu.wpi.grip.ui.util;

import com.google.common.base.Throwables;
import edu.wpi.grip.ui.Controller;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ControllerMapTest extends ApplicationTest {

    private static final String SIZE_OF_CONTROLLER_NOT_SAME_STRING =
            "The size of the controller was not the same";

    private MockPane mockPane;

    private class MockPane extends Pane {

        private final ControllerMap controllerMap;

        public MockPane() {
            super();
            controllerMap = new ControllerMap(this.getChildren());
        }

    }

    private class MockController implements Controller {

        private final Pane pane = new Pane();

        @Override
        public Pane getRoot() {
            return pane;
        }
    }

    @Override
    public void start(Stage stage) {
        mockPane = new MockPane();
        final Scene scene = new Scene(mockPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testRemoveWithController() throws Exception {
        interact(() -> {
            final MockController mockController = new MockController();
            mockPane.controllerMap.add(mockController);
            mockPane.controllerMap.removeWithController(mockController);
            assertEquals("The size of the controller was not 0 after removed with pane", 0, mockPane.controllerMap.size());
        });
    }

    @Test
    public void testRemoveWithPane() throws Exception {
        interact(() -> {
            final MockController mockController = new MockController();
            mockPane.controllerMap.add(mockController);
            mockPane.controllerMap.removeWithNode(mockController.getRoot());
            assertEquals("The size of the controller was not 0 after removed with pane", 0, mockPane.controllerMap.size());
        });
    }

    @Test
    public void testAdd() throws Exception {
        interact(() -> {
            mockPane.controllerMap.add(new MockController());
            assertEquals(SIZE_OF_CONTROLLER_NOT_SAME_STRING, 1, mockPane.controllerMap.size());
        });
    }

    @Test
    public void testAddAll() throws Exception {
        interact(() -> {
            mockPane.controllerMap.addAll(new MockController(), new MockController());
            assertEquals(SIZE_OF_CONTROLLER_NOT_SAME_STRING, 2, mockPane.controllerMap.size());
        });
    }

    @Test
    public void testAddAll1() throws Exception {
        interact(() -> {
            mockPane.controllerMap.addAll(Arrays.asList(new MockController(), new MockController()));
            assertEquals(SIZE_OF_CONTROLLER_NOT_SAME_STRING, 2, mockPane.controllerMap.size());
        });
    }

    @Test
    public void testGetWithPane() throws Exception {
        interact(() -> {
            final MockController mockController = new MockController();
            mockPane.controllerMap.add(mockController);
            assertEquals(SIZE_OF_CONTROLLER_NOT_SAME_STRING,
                    mockController, mockPane.controllerMap.getWithNode(mockController.getRoot()));
        });
    }

    @Test(expected = RuntimeException.class)
    public void testTryToModifyManagedList() throws Exception {
        interact(() -> {
            // Store for before the test
            Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.currentThread().getUncaughtExceptionHandler();
            try {
                final Throwable[] exception = {null};
                Thread.currentThread().setUncaughtExceptionHandler((t, e) -> exception[0] = e);

                final MockController mockController = new MockController();
                mockPane.controllerMap.add(mockController);
                mockPane.getChildren().remove(0);
                if (exception[0] != null) {
                    // Reset this after this test.
                    Throwables.propagateIfInstanceOf(exception[0], IllegalArgumentException.class);
                }
            } finally {
                // Reset this back to what it was.
                Thread.currentThread().setUncaughtExceptionHandler(uncaughtExceptionHandler);
            }
        });
    }
}