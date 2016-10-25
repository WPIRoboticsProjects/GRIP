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

public class CVLaplacian extends AbstractGenerationTesting {
  private static final double ksize = 5;
  private static final double scale = 1;
  private static final double delta = 50;

  boolean setup(String type) {
    Step step = gen.addStep(opUtil.getMetaData("CV laplacian"));
    loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
    gen.connect(imgOut, step.getInputSockets().get(0));
    step.getInputSockets().get(1).setValue(ksize);
    step.getInputSockets().get(2).setValue(scale);
    step.getInputSockets().get(3).setValue(delta);
    HelperTools.setEnumSocket(step.getInputSockets().get(4), type);
    return true;
  }

  @Test
  public void laplTest() {
    test(() -> setup("BOARDER_DEFAULT"), (pip) -> validate(pip), "laplTest");
  }

  @Test
  public void laplConTest() {
    test(() -> setup("BOARDER_CONSTANT"), (pip) -> validate(pip), "laplConTest");
  }

  @Test
  public void laplRepTest() {
    test(() -> setup("BOARDER_REPLICATE"), (pip) -> validate(pip), "laplRepTest");
  }

  @Test
  public void laplRefTest() {
    test(() -> setup("BOARDER_REFLECT"), (pip) -> validate(pip), "laplRefTest");
  }

  @Test
  public void laplWrTest() {
    test(() -> setup("BOARDER_WRAP"), (pip) -> validate(pip), "laplWrTest");
  }

  @Test
  public void laplRef1Test() {
    test(() -> setup("BOARDER_REFLECT_101"), (pip) -> validate(pip), "laplRef1Test");
  }

  @Test
  public void laplRefN1Test() {
    test(() -> setup("BOARDER_REFLECT101"), (pip) -> validate(pip), "laplRefN1Test");
  }

  @Test
  public void laplTrTest() {
    test(() -> setup("BOARDER_TRANSPARENT"), (pip) -> validate(pip), "laplTrTest");
  }

  @Test
  public void laplIsoTest() {
    test(() -> setup("BOARDER_ISOLATED"), (pip) -> validate(pip), "laplIsoTest");
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
    Mat genMat = (Mat) pip.getOutput("CV_Laplacian_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 1.0);
  }
}
