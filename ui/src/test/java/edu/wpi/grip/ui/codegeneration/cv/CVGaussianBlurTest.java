package edu.wpi.grip.ui.codegeneration.cv;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.opencv.NewSizeOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.ui.codegeneration.AbstractGenerationTest;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.Files;

import org.junit.Test;
import org.opencv.core.Mat;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CVGaussianBlurTest extends AbstractGenerationTest {
  private final double width = 15;
  private final double height = 1;
  private final double sigmax = 3;
  private final double sigmay = 41;

  boolean setup(String type) {
    Step step0 = gen.addStep(new OperationMetaData(NewSizeOperation.DESCRIPTION, () -> new
        NewSizeOperation(isf, osf)));
    for (InputSocket sock : step0.getInputSockets()) {
      if (sock.getSocketHint().getIdentifier().equals("width")) {
        sock.setValue(width);
      } else if (sock.getSocketHint().getIdentifier().equals("height")) {
        sock.setValue(height);
      }
    }

    Step step = gen.addStep(opUtil.getMetaData("CV GaussianBlur"));
    ImageFileSource img = loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
    OutputSocket size = step0.getOutputSockets().get(0);
    gen.connect(imgOut, step.getInputSockets().get(0));
    gen.connect(size, step.getInputSockets().get(1));
    step.getInputSockets().get(2).setValue(sigmax);
    step.getInputSockets().get(3).setValue(sigmay);
    HelperTools.setEnumSocket(step.getInputSockets().get(4), type);
    return true;
  }

  @Test
  public void gaussianTest() {
    test(() -> setup("BOARDER_DEFAULT"), (pip) -> validate(pip), "cvgaussianTest");
  }

  @Test
  public void gaussianConTest() {
    test(() -> setup("BOARDER_CONSTANT"), (pip) -> validate(pip), "cvgaussianConTest");
  }

  @Test
  public void gaussianRepTest() {
    test(() -> setup("BOARDER_REPLICATE"), (pip) -> validate(pip), "cvgaussianRepTest");
  }

  @Test
  public void gaussianRefTest() {
    test(() -> setup("BOARDER_REFLECT"), (pip) -> validate(pip), "cvgaussianRefTest");
  }

  @Test
  public void gaussianWrTest() {
    test(() -> setup("BOARDER_WRAP"), (pip) -> validate(pip), "cvgaussianWrTest");
  }

  @Test
  public void gaussianRef1Test() {
    test(() -> setup("BOARDER_REFLECT_101"), (pip) -> validate(pip), "cvgaussianRef1Test");
  }

  @Test
  public void gaussianRefN1Test() {
    test(() -> setup("BOARDER_REFLECT101"), (pip) -> validate(pip), "cvgaussianRefN1Test");
  }

  @Test
  public void gaussianTrTest() {
    test(() -> setup("BOARDER_TRANSPARENT"), (pip) -> validate(pip), "cvgaussianTrTest");
  }

  @Test
  public void gaussianIsoTest() {
    test(() -> setup("BOARDER_ISOLATED"), (pip) -> validate(pip), "cvgaussianIsoTest");
  }


  void validate(PipelineInterfacer pip) {
    ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
    runner.runPipeline();
    pip.setMatSource(0, Files.gompeiJpegFile.file);
    pip.process();
    Optional out = pipeline.getSteps().get(1).getOutputSockets().get(0).getValue();
    assertTrue("Pipeline did not process", out.isPresent());
    assertFalse("Pipeline output is empty", ((org.bytedeco.javacpp.opencv_core.Mat) out.get())
        .empty());
    Mat genMat = (Mat) pip.getOutput(1, GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 1.0);
  }
}
