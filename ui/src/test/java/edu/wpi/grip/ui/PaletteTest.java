package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.events.StepAddedEvent;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.util.GRIPCoreTestModule;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PaletteTest extends ApplicationTest {

    private final GRIPCoreTestModule testModule = new GRIPCoreTestModule();
    private EventBus eventBus;

    @Override
    public void start(Stage stage) throws IOException {
        testModule.setUp();

        Injector injector = Guice.createInjector(Modules.override(testModule).with(new GRIPUIModule()));
        eventBus = injector.getInstance(EventBus.class);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Palette.fxml"));
        loader.setControllerFactory(injector::getInstance);
        stage.setScene(new Scene(loader.load(), 300, 800));
        stage.show();
    }

    @Override
    public void stop() {
        testModule.tearDown();
    }

    @Test
    public void testPalette() {
        // Given a single operation...
        Operation operation = new TestOperation();
        eventBus.post(new OperationAddedEvent(new OperationMetaData(TestOperation.DESCRIPTION, () -> operation)));

        // Record when a a StepAddedEvent happens
        Step[] step = new Step[]{null};
        eventBus.register(new Object() {
            @Subscribe
            public void onStepAdded(StepAddedEvent event) {
                step[0] = event.getStep();
            }
        });

        // If we click on the operation button in the palette...
        clickOn("#test-operation");
        WaitForAsyncUtils.waitForFxEvents();

        // Then there should be a step added
        assertNotNull("Clicking on palette did not add a new step", step[0]);
        assertEquals("Clicking on palette did not add the correct step", TestOperation.DESCRIPTION, step[0].getOperationDescription());
    }

    private static class TestOperation implements Operation {
        public static final OperationDescription DESCRIPTION
                = OperationDescription.builder().name("test").summary("test").build();

        @Override
        public List<InputSocket> getInputSockets() {
            return Collections.emptyList();
        }

        @Override
        public List<OutputSocket> getOutputSockets() {
            return Collections.emptyList();
        }

        @Override
        public void perform() {
            //no-op
        }
    }

}
