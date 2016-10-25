package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.MaskOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.Files;

import org.junit.Test;
import org.opencv.core.Mat;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MaskTesting extends AbstractGenerationTesting {

  void set() {
    HSVThresholdSetup.setup(this);
    Step mask = gen.addStep(
        new OperationMetaData(MaskOperation.DESCRIPTION, () -> new MaskOperation(isf, osf)));
    OutputSocket hsvImg = pipeline.getSteps().get(0).getOutputSockets().get(0);
    for (InputSocket sock : mask.getInputSockets()) {
      String sockHint = sock.getSocketHint().getIdentifier();
      if ("Input".equals(sockHint)) {
        gen.connect(pipeline.getSources().get(0).getOutputSockets().get(0), sock);
      } else if ("Mask".equals(sockHint)) {
        gen.connect(hsvImg, sock);
      }
    }
  }

  @Test
  public void maskTest() {
    test(() -> {
      set();
      return true;
    }, (pip) -> validate(pip), "MaskGripIconTest");
  }

  void validate(PipelineInterfacer pip) {
    new ManualPipelineRunner(eventBus, pipeline).runPipeline();
    Optional out = pipeline.getSteps().get(1).getOutputSockets().get(0).getValue();
    assertTrue("Pipeline did not process", out.isPresent());
    assertFalse("Pipeline output is empty",
        ((org.bytedeco.javacpp.opencv_core.Mat) out.get()).empty());
    pip.setMatSource(0, Files.imageFile.file);
    pip.process();
    Mat genMat = (Mat) pip.getOutput("Mask_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 0.5);
  }
}
