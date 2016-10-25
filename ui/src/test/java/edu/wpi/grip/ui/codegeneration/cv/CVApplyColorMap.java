package edu.wpi.grip.ui.codegeneration.cv;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.generated.opencv_imgproc.enumeration.ColormapTypesEnum;
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

public class CVApplyColorMap extends AbstractGenerationTesting {

  boolean set(ColormapTypesEnum map) {
    Step step = gen.addStep(opUtil.getMetaData("CV applyColorMap"));
    loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
    gen.connect(imgOut, step.getInputSockets().get(0));
    step.getInputSockets().get(1).setValue(map);
    
    return true;
  }

  @Test
  public void testAutumn() {
    helpTest(ColormapTypesEnum.COLORMAP_AUTUMN, "AutumnTest");
  }
  
  @Test
  public void testBone() {
    helpTest(ColormapTypesEnum.COLORMAP_BONE, "BoneTest");
  }
  
  @Test
  public void testJet() {
    helpTest(ColormapTypesEnum.COLORMAP_JET, "JetTest");
  }
  
  @Test
  public void testWinter() {
    helpTest(ColormapTypesEnum.COLORMAP_WINTER, "WinterTest");
  }
  
  @Test
  public void testRainbow() {
    helpTest(ColormapTypesEnum.COLORMAP_RAINBOW, "RainbowTest");
  }
  
  @Test
  public void testOcean() {
    helpTest(ColormapTypesEnum.COLORMAP_OCEAN, "OceanTest");
  }
  
  @Test
  public void testSummer() {
    helpTest(ColormapTypesEnum.COLORMAP_SUMMER, "SummerTest");
  }
  
  @Test
  public void testSpring() {
    helpTest(ColormapTypesEnum.COLORMAP_SPRING, "SpringTest");
  }
  
  @Test
  public void testCool() {
    helpTest(ColormapTypesEnum.COLORMAP_COOL, "CoolTest");
  }
  
  @Test
  public void testHSV() {
    helpTest(ColormapTypesEnum.COLORMAP_HSV, "HSVTest");
  }
  
  @Test
  public void testPink() {
    helpTest(ColormapTypesEnum.COLORMAP_PINK, "PinkTest");
  }
  
  @Test
  public void testHot() {
    helpTest(ColormapTypesEnum.COLORMAP_HOT, "HotTest");
  }
  
  @Test
  public void testParula() {
    helpTest(ColormapTypesEnum.COLORMAP_PARULA, "ParulaTest");
  }
  
  void helpTest(ColormapTypesEnum map, String testName) {
    test(() -> set(map), (pip) -> validate(pip), "cvColorMap" + testName);
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
    Mat genMat = (Mat) pip.getOutput("CV_ApplyColorMap_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 1.0);
  }
}
