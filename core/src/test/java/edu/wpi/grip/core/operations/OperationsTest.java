package edu.wpi.grip.core.operations;

import edu.wpi.grip.core.GripCoreModule;
import edu.wpi.grip.core.GripFileModule;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.metrics.MockTimer;
import edu.wpi.grip.core.operations.network.GripNetworkModule;
import edu.wpi.grip.core.operations.network.MockGripNetworkModule;
import edu.wpi.grip.core.util.MockExceptionWitness;
import edu.wpi.grip.util.GripCoreTestModule;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
@SuppressWarnings("all")
public class OperationsTest {

  @Parameter
  public OperationMetaData operationMetaData;

  private static GripCoreTestModule testModule;
  private static Injector injector;

  static {
    // Only because @Parameters runs before @BeforeClass... for some weird reason
    setUpClass();
  }

  // @Parameters is called before @BeforeClass, so we call it manually in a static initializer block
  //  @BeforeClass
  public static void setUpClass() {
    testModule = new GripCoreTestModule();
    testModule.setUp();
    injector = Guice.createInjector(
        Modules.override(new GripCoreModule(), new GripNetworkModule(), new GripFileModule())
            .with(new MockGripNetworkModule(), testModule));
  }

  @AfterClass
  public static void tearDownClass() {
    testModule.tearDown();
  }

  @Parameters(name = "{index}: Operation({0})")
  public static Collection<Object[]> data() {
    EventBus eventBus = injector.getInstance(EventBus.class);
    List<OperationMetaData> operationMetaDatas =
        ImmutableList.<OperationMetaData>builder()
            .addAll(
                OperationsFactory
                    .create(eventBus, injector)
                    .operations())
            .addAll(
                OperationsFactory
                    .createCV(eventBus)
                    .operations())
            .build();

    Object[][] params = new Object[operationMetaDatas.size()][1];
    final int[] index = {0};
    operationMetaDatas.forEach(operationMeta -> {
      params[index[0]][0] = operationMeta;
      index[0]++;
    });
    return Arrays.asList(params);
  }


  @Test
  public void testCreateAllSteps() {
    final Step step =
        new Step.Factory((origin) -> new MockExceptionWitness(new EventBus(), origin),
            MockTimer.MOCK_FACTORY)
            .create(operationMetaData);
    step.setRemoved();
  }
}
