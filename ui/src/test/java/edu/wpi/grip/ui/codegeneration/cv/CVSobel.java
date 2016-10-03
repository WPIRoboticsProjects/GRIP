package edu.wpi.grip.ui.codegeneration.cv;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.codegeneration.AbstractGenerationTesting;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.Files;

import org.junit.Test;
import org.opencv.core.Mat;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CVSobel extends AbstractGenerationTesting {
  private static final double dx = 1;
  private static final double dy = 1;
  private static final double ksize = 3;
  private static final double scale = 10;
  private static final double delta = 100;

  boolean setup(String type) {
    Step step = gen.addStep(opUtil.getMetaData("CV sobel"));
    loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
    gen.connect(imgOut, step.getInputSockets().get(0));
    step.getInputSockets().get(1).setValue(dx);
    step.getInputSockets().get(2).setValue(dy);
    step.getInputSockets().get(3).setValue(ksize);
    step.getInputSockets().get(4).setValue(scale);
    step.getInputSockets().get(5).setValue(delta);
    HelperTools.setEnumSocket(step.getInputSockets().get(6), type);
    return true;
  }

  @Test
  public void sobelTest() {
    test(() -> setup("BOARDER_DEFAULT"), (pip) -> validate(pip), "cvSobelTest");
  }

  @Test
  public void sobelConTest() {
    test(() -> setup("BOARDER_CONSTANT"), (pip) -> validate(pip), "cvSobelConTest");
  }

  @Test
  public void sobelRepTest() {
    test(() -> setup("BOARDER_REPLICATE"), (pip) -> validate(pip), "cvSobelRepTest");
  }

  @Test
  public void sobelRefTest() {
    test(() -> setup("BOARDER_REFLECT"), (pip) -> validate(pip), "cvSobelRefTest");
  }

  @Test
  public void sobelWrTest() {
    test(() -> setup("BOARDER_WRAP"), (pip) -> validate(pip), "cvSobelWrTest");
  }

  @Test
  public void sobelRef1Test() {
    test(() -> setup("BOARDER_REFLECT_101"), (pip) -> validate(pip), "cvSobelRef1Test");
  }

  @Test
  public void sobelRefN1Test() {
    test(() -> setup("BOARDER_REFLECT101"), (pip) -> validate(pip), "cvSobelRefN1Test");
  }

  @Test
  public void sobelTrTest() {
    test(() -> setup("BOARDER_TRANSPARENT"), (pip) -> validate(pip), "cvSobelTrTest");
  }

  @Test
  public void sobelIsoTest() {
    test(() -> setup("BOARDER_ISOLATED"), (pip) -> validate(pip), "cvSobelIsoTest");
  }


  void validate(PipelineInterfacer pip) {
    ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
    runner.runPipeline();
    pip.setMatSource(0, Files.gompeiJpegFile.file);
    pip.process();
    Optional out = pipeline.getSteps().get(0).getOutputSockets().get(0).getValue();
    assertTrue("Pipeline did not process", out.isPresent());
    assertFalse("Pipeline output is empty", ((org.bytedeco.javacpp.opencv_core.Mat) out.get())
        .empty());
    Mat genMat = (Mat) pip.getOutput("CV_Sobel_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 1.0);
  }
}
