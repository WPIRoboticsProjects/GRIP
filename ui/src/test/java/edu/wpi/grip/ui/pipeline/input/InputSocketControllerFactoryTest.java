package edu.wpi.grip.ui.pipeline.input;

import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Palette;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.OperationsFactory;
import edu.wpi.grip.core.operations.network.MockGripNetworkModule;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.ui.GripUiModule;
import edu.wpi.grip.util.GripCoreTestModule;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

import java.util.Arrays;
import java.util.Collection;

import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import static org.testfx.api.FxAssert.verifyThat;


@RunWith(Parameterized.class)
public class InputSocketControllerFactoryTest extends ApplicationTest {

  private final OperationMetaData operationMeta;
  @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
  private final String name;
  private GripCoreTestModule testModule;
  private Step.Factory stepFactory;
  private InputSocketControllerFactory inputSocketControllerFactory;
  private GridPane gridPane;

  /**
   * @param operationMeta The operation under test
   * @param name          The name. This is used for logging if the tests fail
   */
  public InputSocketControllerFactoryTest(OperationMetaData operationMeta, String name) {
    super();
    this.operationMeta = operationMeta;
    this.name = name;
  }

  @Parameterized.Parameters(name = "{index}: operation({0})={1}")
  public static Collection<Object[]> data() {
    GripCoreTestModule testModule = new GripCoreTestModule();
    testModule.setUp();

    Injector injector = Guice.createInjector(Modules.override(testModule)
        .with(new MockGripNetworkModule()));
    final Palette palette = injector.getInstance(Palette.class);
    final EventBus eventBus = injector.getInstance(EventBus.class);
    OperationsFactory.create(eventBus, injector).addOperations();
    OperationsFactory.createCV(eventBus).addOperations();
    Collection<OperationMetaData> operationMetas = palette.getOperations();

    Object[][] params = new Object[operationMetas.size()][2];
    final int[] index = {0};
    operationMetas.forEach(operationMeta -> {
      params[index[0]][0] = operationMeta;
      params[index[0]][1] = operationMeta.getDescription().name();
      index[0]++;
    });

    testModule.tearDown();

    return Arrays.asList(params);
  }

  @Override
  public void start(Stage stage) {
    testModule = new GripCoreTestModule();
    testModule.setUp();
    Injector injector = Guice.createInjector(Modules.override(testModule)
        .with(new GripUiModule(), new MockGripNetworkModule()));
    inputSocketControllerFactory = injector.getInstance(InputSocketControllerFactory.class);
    stepFactory = injector.getInstance(Step.Factory.class);
    gridPane = new GridPane();

    Scene scene = new Scene(gridPane, 800, 600);
    stage.setScene(scene);
    stage.show();
  }

  @After
  public void tearDown() {
    testModule.tearDown();
  }

  @Test
  @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
  public void testCreateAllKnownInputSocketControllers() throws Exception {
    final Step step = stepFactory.create(operationMeta);
    interact(() -> {
      for (int i = 0; i < step.getInputSockets().size(); i++) {
        final InputSocket<?> inputSocket = step.getInputSockets().get(i);
        InputSocketController controller = inputSocketControllerFactory.create(inputSocket);
        gridPane.add(controller.getRoot(), 0, i);
        verifyThat(controller.getHandle(), NodeMatchers.isEnabled());
        verifyThat(controller.getHandle(), NodeMatchers.isVisible());
      }
    });
  }
}
