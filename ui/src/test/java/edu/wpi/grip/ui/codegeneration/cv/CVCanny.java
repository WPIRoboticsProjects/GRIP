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

public class CVCanny extends AbstractGenerationTesting {
  private static final double thresh1 = 50;
  private static final double thresh2 = 60;
  private static final double apertureSize = 3;
  private static final boolean grad = true;


  boolean set() {
    Step step = gen.addStep(opUtil.getMetaData("CV canny"));
    loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
    gen.connect(imgOut, step.getInputSockets().get(0));
    step.getInputSockets().get(1).setValue(thresh1);
    step.getInputSockets().get(2).setValue(thresh2);
    step.getInputSockets().get(3).setValue(apertureSize);
    step.getInputSockets().get(4).setValue(grad);

    return true;
  }

  @Test
  public void cannyTest() {
    test(() -> set(), (pip) -> validate(pip), "cvcannyTest");
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
    Mat genMat = (Mat) pip.getOutput("CV_Canny_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 1.0);
  }
}
