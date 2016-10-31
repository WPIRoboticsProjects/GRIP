package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.BlurOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHints.Outputs;
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
public class BlurGenerationTesting extends AbstractGenerationTesting {
  private final Double blurRatio = new Double(10.0);

  void generatePipeline(String blurType) {
    Step step = gen.addStep(
        new OperationMetaData(BlurOperation.DESCRIPTION, () -> new BlurOperation(isf, osf)));
    loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);

    for (InputSocket sock : step.getInputSockets()) {
      if (sock.getSocketHint().isCompatibleWith(imgOut.getSocketHint())) {
        gen.connect(imgOut, sock);
      } else if (sock.getSocketHint()
          .isCompatibleWith(Outputs.createNumberSocketHint("Number", blurRatio))) {
        sock.setValue(blurRatio);
      } else if (sock.getSocketHint().getIdentifier().equals("Type")) {
        HelperTools.setEnumSocket(sock, blurType);
      }
    }
  }

  @Test
  public void boxBlurTest() {
    test(() -> {
      generatePipeline("Box Blur");
      return true;
    }, (pip) -> pipelineTest(pip), "BoxBlurTest");
  }

  @Test
  public void gaussianBlurTest() {
    test(() -> {
      generatePipeline("Gaussian Blur");
      return true;
    }, (pip) -> pipelineTest(pip), "GaussianBlurTest");
  }

  @Test
  public void medianFilterTest() {
    test(() -> {
      generatePipeline("Median Filter");
      return true;
    }, (pip) -> pipelineTest(pip), "MedianFilterTest");
  }

  @Test
  public void bilateralFilterTest() {
    test(() -> {
      generatePipeline("Bilateral Filter");
      return true;
    }, (pip) -> pipelineTest(pip), "BilateralFilterTest");
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
    Mat genMat = (Mat) pip.getOutput("blur_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 10.0);
  }
}
