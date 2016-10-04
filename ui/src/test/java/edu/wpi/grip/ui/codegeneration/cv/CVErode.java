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

public class CVErode extends AbstractGenerationTesting {
  private static final double iterations = 4;


  boolean setup(String type) {
    Step step = gen.addStep(opUtil.getMetaData("CV erode"));
    loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
    gen.connect(imgOut, step.getInputSockets().get(0));
    step.getInputSockets().get(3).setValue(iterations);
    HelperTools.setEnumSocket(step.getInputSockets().get(4), type);
    return true;
  }

  @Test
  public void erodeTest() {
    test(() -> setup("BOARDER_DEFAULT"), (pip) -> validate(pip), "cverodeTest");
  }

  @Test
  public void erodeConTest() {
    test(() -> setup("BOARDER_CONSTANT"), (pip) -> validate(pip), "cverodeConTest");
  }

  @Test
  public void erodeRepTest() {
    test(() -> setup("BOARDER_REPLICATE"), (pip) -> validate(pip), "cverodeRepTest");
  }

  @Test
  public void erodeRefTest() {
    test(() -> setup("BOARDER_REFLECT"), (pip) -> validate(pip), "cverodeRefTest");
  }

  @Test
  public void erodeWrTest() {
    test(() -> setup("BOARDER_WRAP"), (pip) -> validate(pip), "cverodeWrTest");
  }

  @Test
  public void erodeRef1Test() {
    test(() -> setup("BOARDER_REFLECT_101"), (pip) -> validate(pip), "cverodeRef1Test");
  }

  @Test
  public void erodeRefN1Test() {
    test(() -> setup("BOARDER_REFLECT101"), (pip) -> validate(pip), "cverodeRefN1Test");
  }

  @Test
  public void erodeTrTest() {
    test(() -> setup("BOARDER_TRANSPARENT"), (pip) -> validate(pip), "cverodeTrTest");
  }

  @Test
  public void erodeIsoTest() {
    test(() -> setup("BOARDER_ISOLATED"), (pip) -> validate(pip), "cverodeIsoTest");
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
    Mat genMat = (Mat) pip.getOutput("CV_Erode_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 10);
  }
}
