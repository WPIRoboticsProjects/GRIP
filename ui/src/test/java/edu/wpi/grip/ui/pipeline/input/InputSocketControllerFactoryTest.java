package edu.wpi.grip.ui.pipeline.input;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.Palette;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.Operations;
import edu.wpi.grip.generated.CVOperations;
import edu.wpi.grip.ui.GRIPUIModule;
import edu.wpi.grip.util.GRIPCoreTestModule;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

import java.util.Arrays;
import java.util.Collection;

import static org.testfx.api.FxAssert.verifyThat;

@RunWith(Parameterized.class)
public class InputSocketControllerFactoryTest extends ApplicationTest {

    private GRIPCoreTestModule testModule;
    private Step.Factory stepFactory;
    private InputSocketControllerFactory inputSocketControllerFactory;
    private GridPane gridPane;

    private final Operation operation;
    @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
    private final String name;

    @Parameterized.Parameters(name = "{index}: operation({0})={1}")
    public static Collection<Object[]> data() {
        GRIPCoreTestModule testModule = new GRIPCoreTestModule();
        testModule.setUp();

        Injector injector = Guice.createInjector(testModule);
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

        testModule.tearDown();

        return Arrays.asList(params);
    }

    /**
     * @param operation The operation under test
     * @param name      The name. This is used for logging if the tests fail
     */
    public InputSocketControllerFactoryTest(Operation operation, String name) {
        super();
        this.operation = operation;
        this.name = name;
    }

    @Override
    public void start(Stage stage) {
        testModule = new GRIPCoreTestModule();
        testModule.setUp();
        Injector injector = Guice.createInjector(Modules.override(testModule).with(new GRIPUIModule()));
        inputSocketControllerFactory = injector.getInstance(InputSocketControllerFactory.class);
        stepFactory = injector.getInstance(Step.Factory.class);
        gridPane = new GridPane();

        Scene scene = new Scene(gridPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Before
    public void tearDown() {
        testModule.tearDown();
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
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