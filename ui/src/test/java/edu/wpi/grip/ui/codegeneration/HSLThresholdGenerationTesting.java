package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.Files;

import org.junit.Test;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HSLThresholdGenerationTesting extends AbstractGenerationTesting {
  // H 0-49
  // S 0-41
  // L 0-67
  private final List<Number> hVal = new ArrayList<Number>();
  private final List<Number> sVal = new ArrayList<Number>();
  private final List<Number> lVal = new ArrayList<Number>();

  public HSLThresholdGenerationTesting() {
    hVal.add(new Double(0.0));
    hVal.add(new Double(49.0));
    sVal.add(new Double(0.0));
    sVal.add(new Double(41.0));
    lVal.add(new Double(0.0));
    lVal.add(new Double(67.0));
  }

  @Test
  public void testHSL() {
    test(() -> {
      GripIconHSLSetup.setup(this);
      return true; // never can fail
    }, (pip) -> validate(pip), "HSLTest");
  }

  private void validate(PipelineInterfacer pip) {
    new ManualPipelineRunner(eventBus, pipeline).runPipeline();
    Optional out = pipeline.getSteps().get(0).getOutputSockets().get(0).getValue();
    assertTrue("Output is not present", out.isPresent());
    assertFalse("Output Mat is empty", ((org.bytedeco.javacpp.opencv_core.Mat) out.get()).empty());
    pip.setMatSource(0, Files.imageFile.file);
    pip.process();
    Mat genMat = (Mat) pip.getOutput("HSL_Threshold_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 10.0);
  }

}
