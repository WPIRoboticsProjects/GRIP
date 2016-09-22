package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.opencv.NewSizeOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;

import org.junit.Test;
import org.opencv.core.Size;

import java.util.Optional;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class NewSizeGenerationTesting extends AbstractGenerationTesting {

  void generatePipeline(double width, double height) {
    Step desat = gen.addStep(
        new OperationMetaData(NewSizeOperation.DESCRIPTION, () -> new NewSizeOperation(isf, osf)));
    for (InputSocket sock : desat.getInputSockets()) {
      if (sock.getSocketHint().getIdentifier().equals("width")) {
        sock.setValue(width);
      } else if (sock.getSocketHint().getIdentifier().equals("height")) {
        sock.setValue(height);
      }
    }
  }

  @Test
  public void newSizeTest() {
    test(() -> {
      generatePipeline(3, 5);
      return true;
    }, (pip) -> pipelineTest(pip), "newSizeTest");
  }

  void pipelineTest(PipelineInterfacer pip) {
    new ManualPipelineRunner(eventBus, pipeline).runPipeline();
    Optional out = pipeline.getSteps().get(0).getOutputSockets().get(0).getValue();
    assertTrue("Output is not present", out.isPresent());
    org.bytedeco.javacpp.opencv_core.Size gripSize =
        (org.bytedeco.javacpp.opencv_core.Size) out.get();
    pip.process();
    Size genSize = (Size) pip.getOutput("New_Size_Output", GenType.SIZE);
    assertSame("The grip width: " + gripSize.width() + "does not equals the generated width: "
        + genSize.width, gripSize.width(), (int) genSize.width);
    assertSame("The grip height: " + gripSize.height() + "does not equals the generated height: "
        + genSize.height, gripSize.height(), (int) genSize.height);
  }

}
