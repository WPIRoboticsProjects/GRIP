package edu.wpi.grip.ui.codegeneration.tools;

import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.metrics.MockTimer;
import edu.wpi.grip.core.settings.CodeGenerationSettings;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.util.MockExceptionWitness;
import edu.wpi.grip.ui.codegeneration.Exporter;
import edu.wpi.grip.ui.codegeneration.Language;

import com.google.common.eventbus.EventBus;

import java.io.File;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import static org.junit.Assert.fail;

public class PipelineGenerator {
  private final Pipeline pipeline;
  private final EventBus eventBus;
  private final Connection.Factory<Object> factory;
  private static File codeDir = null;
  private static final Logger logger = Logger.getLogger(PipelineGenerator.class.getName());

  static {
    try {
      codeDir = new File(PipelineGenerator.class.getProtectionDomain()
          .getCodeSource().getLocation().toURI());
    } catch (URISyntaxException e) {
      fail("Could not load code directory");
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }

  @Inject
  PipelineGenerator(Connection.Factory<Object> factory, EventBus eventBus,
                    Pipeline pipeline) {
    this.factory = factory;
    this.eventBus = eventBus;
    this.pipeline = pipeline;
  }

  public Step addStep(OperationMetaData data) {
    Step step = new Step.Factory(MockExceptionWitness.MOCK_FACTORY, MockTimer.MOCK_FACTORY)
        .create(data);
    pipeline.addStep(step);
    return step;
  }

  public <T> void connect(OutputSocket<? extends T> out, InputSocket<T> inp) {
    eventBus.post(new ConnectionAddedEvent(factory.create(out, (InputSocket<Object>) inp)));
  }

  public void export(String fileName) {
    new Exporter(pipeline.getSteps(),
        CodeGenerationSettings.builder()
            .language(Language.JAVA.name)
            .className(fileName)
            .saveDir(codeDir.getAbsolutePath())
            .implementVisionPipeline(false)
            .packageName("")
            .moduleName("")
            .build(),
        true).run();
    new Exporter(pipeline.getSteps(),
        CodeGenerationSettings.builder()
            .language(Language.PYTHON.name)
            .className(fileName)
            .saveDir(codeDir.getAbsolutePath())
            .implementVisionPipeline(false)
            .packageName("")
            .moduleName(fileName)
            .build(),
        true).run();
    new Exporter(pipeline.getSteps(),
        CodeGenerationSettings.builder()
            .language(Language.CPP.name)
            .className(fileName)
            .saveDir(codeDir.getAbsolutePath())
            .implementVisionPipeline(false)
            .packageName("")
            .moduleName("")
            .build(),
        true).run();
  }

  public static File getCodeDir() {
    return codeDir;
  }

}
