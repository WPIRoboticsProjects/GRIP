package edu.wpi.grip.ui.codegeneration.tools;

import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.util.MockExceptionWitness;
import edu.wpi.grip.ui.codegeneration.Exporter;
import edu.wpi.grip.ui.codegeneration.Language;

import com.google.common.eventbus.EventBus;

import java.io.File;
import java.net.URISyntaxException;

import javax.inject.Inject;

import static org.junit.Assert.fail;

public class PipelineGenerator {
  @Inject
  private Pipeline pipeline;
  @Inject
  private Exporter exporter;
  @Inject
  private EventBus eventBus;
  @Inject
  private Connection.Factory<Object> factory;
  static File codeDir = null;

  static {
    try {
      codeDir = new File(PipelineGenerator.class.getProtectionDomain()
          .getCodeSource().getLocation().toURI());
    } catch (URISyntaxException e) {
      e.printStackTrace();
      fail("Could not load code directory");
    }
  }

  public PipelineGenerator() {
  }

  public Step addStep(OperationMetaData data) {
    Step step = new Step.Factory(MockExceptionWitness.MOCK_FACTORY).create(data);
    pipeline.addStep(step);
    return step;
  }

  public <T> void connect(OutputSocket<? extends T> out, InputSocket<T> inp) {
    eventBus.post(new ConnectionAddedEvent(factory.create(out, (InputSocket<Object>) inp)));
  }

  public void export(String fileName) {
    exporter.export(pipeline, Language.JAVA, codeDir.toPath().resolve(fileName + ".java").toFile(),
        false);
    exporter.export(pipeline, Language.PYTHON, codeDir.toPath().resolve(fileName + ".py").toFile(),
        false);
  }

}
