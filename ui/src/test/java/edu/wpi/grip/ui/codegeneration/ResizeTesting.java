package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.ResizeOperation;
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

public class ResizeTesting extends AbstractGenerationTesting {

  void setup(String interp) {
    Step resize = gen.addStep(
        new OperationMetaData(ResizeOperation.DESCRIPTION, () -> new ResizeOperation(isf, osf)));
    loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
    for (InputSocket sock : resize.getInputSockets()) {
      String sockHint = sock.getSocketHint().getIdentifier();
      if ("Input".equals(sockHint)) {
        gen.connect(imgOut, sock);
      } else if ("Width".equals(sockHint)) {
        sock.setValue(new Double(500));
      } else if ("Height".equals(sockHint)) {
        sock.setValue(new Double(250));
      } else if ("Interpolation".equals(sockHint)) {
        HelperTools.setEnumSocket(sock, interp);
      }
    }
  }

  @Test
  public void nearestTest() {
    test(() -> {
      setup("None");
      return true;
    }, (pip) -> pipelineTest(pip), "ResizeNearestTest");
  }

  @Test
  public void linearTest() {
    test(() -> {
      setup("Linear");
      return true;
    }, (pip) -> pipelineTest(pip), "ResizeLinearTest");
  }

  @Test
  public void cubicTest() {
    test(() -> {
      setup("Cubic");
      return true;
    }, (pip) -> pipelineTest(pip), "ResizeCubicTest");
  }

  @Test
  public void lanczosTest() {
    test(() -> {
      setup("Lanczos");
      return true;
    }, (pip) -> pipelineTest(pip), "ResizeLanczosTest");
  }

  @Test
  public void areaTest() {
    test(() -> {
      setup("Area");
      return true;
    }, (pip) -> pipelineTest(pip), "ResizeAreaTest");
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
    Mat genMat = (Mat) pip.getOutput("Resize_Image_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 16.0);
  }
}
