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

public class CVDilate extends AbstractGenerationTesting {
  private static final double iterations = 4;


  boolean setup(String type) {
    Step step = gen.addStep(opUtil.getMetaData("CV dilate"));
    loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
    gen.connect(imgOut, step.getInputSockets().get(0));
    step.getInputSockets().get(3).setValue(iterations);
    HelperTools.setEnumSocket(step.getInputSockets().get(4), type);
    return true;
  }

  @Test
  public void dilateTest() {
    test(() -> setup("BOARDER_DEFAULT"), (pip) -> validate(pip), "cvdilateTest");
  }

  @Test
  public void dilateConTest() {
    test(() -> setup("BOARDER_CONSTANT"), (pip) -> validate(pip), "cvdilateConTest");
  }

  @Test
  public void dilateRepTest() {
    test(() -> setup("BOARDER_REPLICATE"), (pip) -> validate(pip), "cvdilateRepTest");
  }

  @Test
  public void dilateRefTest() {
    test(() -> setup("BOARDER_REFLECT"), (pip) -> validate(pip), "cvdilateRefTest");
  }

  @Test
  public void dilateWrTest() {
    test(() -> setup("BOARDER_WRAP"), (pip) -> validate(pip), "cvdilateWrTest");
  }

  @Test
  public void dilateRef1Test() {
    test(() -> setup("BOARDER_REFLECT_101"), (pip) -> validate(pip), "cvdilateRef1Test");
  }

  @Test
  public void dilateRefN1Test() {
    test(() -> setup("BOARDER_REFLECT101"), (pip) -> validate(pip), "cvdilateRefN1Test");
  }

  @Test
  public void dilateTrTest() {
    test(() -> setup("BOARDER_TRANSPARENT"), (pip) -> validate(pip), "cvdilateTrTest");
  }

  @Test
  public void dilateIsoTest() {
    test(() -> setup("BOARDER_ISOLATED"), (pip) -> validate(pip), "cvdilateIsoTest");
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
    Mat genMat = (Mat) pip.getOutput("CV_Dilate_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 10);
  }
}
