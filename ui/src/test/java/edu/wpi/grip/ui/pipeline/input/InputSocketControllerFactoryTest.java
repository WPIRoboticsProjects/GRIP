package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.operations.Operations;
import edu.wpi.grip.generated.CVOperations;
import edu.wpi.grip.ui.GRIPUIModule;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.runners.Parameterized.Parameters;
import static org.testfx.api.FxAssert.verifyThat;

@RunWith(Parameterized.class)
public class InputSocketControllerFactoryTest extends ApplicationTest {

    @Parameters(name = "{index}: operation({0})={1}")
    public static Collection<Object[]> data() {
        Injector injector = Guice.createInjector(new GRIPCoreModule());
        final Palette palette = injector.getInstance(Palette.class);
        final EventBus eventBus = injector.getInstance(EventBus.class);
        Operations.addOperations(eventBus);
        CVOperations.addOperations(eventBus);
        Collection<Operation> operations = palette.getOperations();

        Object[][] params = new Object[operations.size()][2];
        final int[] index = {0};
        operations.forEach(operation -> {
            params[index[0]][0] = operation;
            params[index[0]][1] = operation.getName();
            index[0]++;
        });
        return Arrays.asList(params);
    }

    private Step.Factory stepFactory;
    private InputSocketControllerFactory inputSocketControllerFactory;
    private GridPane gridPane;

    private final Operation operation;
    private final String name;

    /**
     * @param operation The operation under test
     * @param name      The name. This is used for logging if the tests fail
     */
    public InputSocketControllerFactoryTest(Operation operation, String name) {
        this.operation = operation;
        this.name = name;
    }

    @Override
    public void start(Stage stage) {
        Injector injector = Guice.createInjector(new GRIPCoreModule(), new GRIPUIModule());
        inputSocketControllerFactory = injector.getInstance(InputSocketControllerFactory.class);
        stepFactory = injector.getInstance(Step.Factory.class);
        gridPane = new GridPane();

        Scene scene = new Scene(gridPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testCreateAllKnownInputSocketControllers() throws Exception {
        final Step step = stepFactory.create(operation);
        interact(() -> {
            for (int i = 0; i < step.getInputSockets().length; i++) {
                final InputSocket<?> inputSocket = step.getInputSockets()[i];
                InputSocketController controller = inputSocketControllerFactory.create(inputSocket);
                gridPane.add(controller.getRoot(), 0, i);
                verifyThat(controller.getHandle(), NodeMatchers.isEnabled());
                verifyThat(controller.getHandle(), NodeMatchers.isVisible());
            }
        });
    }
}