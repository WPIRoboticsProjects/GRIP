package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.NormalizeOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.Files;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opencv.core.Mat;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(GenerationTesting.class)
public class NormalizeGenerationTesting extends AbstractGenerationTesting {

  void generatePipeline(String type, double minVal, double maxVal) {
    Step step = gen.addStep(new OperationMetaData(NormalizeOperation.DESCRIPTION,
        () -> new NormalizeOperation(isf, osf)));
    loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);

    for (InputSocket sock : step.getInputSockets()) {
      if (sock.getSocketHint().isCompatibleWith(imgOut.getSocketHint())) {
        gen.connect(imgOut, sock);
      } else if (sock.getSocketHint().getIdentifier().equals("Alpha")) {
        sock.setValue(minVal);
      } else if (sock.getSocketHint().getIdentifier().equals("Beta")) {
        sock.setValue(maxVal);
      } else if (sock.getSocketHint().getIdentifier().equals("Type")) {
        HelperTools.setEnumSocket(sock, type);
      }
    }
  }

  @Test
  public void normInfTest() {
    test(() -> {
      generatePipeline("NORM_INF", 100, 200);
      return true;
    }, (pip) -> pipelineTest(pip), "NORM_INFTest");
  }

  @Test
  public void normL1Test() {
    test(() -> {
      generatePipeline("NORM_L1", 5000000, 5000000);
      return true;
    }, (pip) -> pipelineTest(pip), "NORM_L1Test");
  }

  @Test
  public void normL2Test() {
    test(() -> {
      generatePipeline("NORM_L2", 500000, 500000);
      return true;
    }, (pip) -> pipelineTest(pip), "NORM_L2Test");
  }

  @Test
  public void normMinMaxTest() {
    test(() -> {
      generatePipeline("NORM_MINMAX", 100, 200);
      return true;
    }, (pip) -> pipelineTest(pip), "NORM_MINMAXTest");
  }

  void pipelineTest(PipelineInterfacer pip) {
    ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
    runner.runPipeline();
    Optional out = pipeline.getSteps().get(0).getOutputSockets().get(0).getValue();
    assertTrue("Pipeline did not process", out.isPresent());
    assertFalse("Pipeline output is empty",
        ((org.bytedeco.javacpp.opencv_core.Mat) out.get()).empty());
    pip.setMatSource(0, Files.gompeiJpegFile.file);
    pip.process();
    Mat genMat = (Mat) pip.getOutput("Normalize_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 10.0);
  }
}
