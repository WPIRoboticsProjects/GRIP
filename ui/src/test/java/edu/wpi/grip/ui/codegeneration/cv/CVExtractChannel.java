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

public class CVExtractChannel extends AbstractGenerationTesting {

  boolean setup(int channel) {
    Step step = gen.addStep(opUtil.getMetaData("CV extractChannel"));
    loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
    gen.connect(imgOut, step.getInputSockets().get(0));
    step.getInputSockets().get(1).setValue(channel);
    return true;
  }

  @Test
  public void extractZeroTest() {
    test(() -> setup(0), (pip) -> validate(pip), "extractZeroTest");
  }

  @Test
  public void extractOneTest() {
    test(() -> setup(1), (pip) -> validate(pip), "extractOneTest");
  }
  
  @Test
  public void extractTwoTest() {
    test(() -> setup(2), (pip) -> validate(pip), "extractTwoTest");
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
    Mat genMat = (Mat) pip.getOutput("CV_ExtractChannel_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 1.0);
  }
}
