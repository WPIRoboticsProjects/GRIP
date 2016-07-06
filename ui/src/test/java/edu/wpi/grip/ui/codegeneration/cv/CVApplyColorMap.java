package edu.wpi.grip.ui.codegeneration.cv;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.generated.opencv_imgproc.enumeration.ColormapTypesEnum;
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

public class CVApplyColorMap extends AbstractGenerationTest {

  boolean setup(ColormapTypesEnum map) {
    Step step = gen.addStep(opUtil.getMetaData("CV applyColorMap"));
    ImageFileSource img = loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
    gen.connect(imgOut, step.getInputSockets().get(0));
    step.getInputSockets().get(1).setValue(map);
    
    return true;
  }

  @Test
  public void testAutumn() {
    testHelp(ColormapTypesEnum.COLORMAP_AUTUMN, "AutumnTest");
  }
  
  @Test
  public void testBone() {
    testHelp(ColormapTypesEnum.COLORMAP_BONE, "BoneTest");
  }
  
  @Test
  public void testJet() {
    testHelp(ColormapTypesEnum.COLORMAP_JET, "JetTest");
  }
  
  @Test
  public void testWinter() {
    testHelp(ColormapTypesEnum.COLORMAP_WINTER, "WinterTest");
  }
  
  @Test
  public void testRainbow() {
    testHelp(ColormapTypesEnum.COLORMAP_RAINBOW, "RainbowTest");
  }
  
  @Test
  public void testOcean() {
    testHelp(ColormapTypesEnum.COLORMAP_OCEAN, "OceanTest");
  }
  
  @Test
  public void testSummer() {
    testHelp(ColormapTypesEnum.COLORMAP_SUMMER, "SummerTest");
  }
  
  @Test
  public void testSpring() {
    testHelp(ColormapTypesEnum.COLORMAP_SPRING, "SpringTest");
  }
  
  @Test
  public void testCool() {
    testHelp(ColormapTypesEnum.COLORMAP_COOL, "CoolTest");
  }
  
  @Test
  public void testHSV() {
    testHelp(ColormapTypesEnum.COLORMAP_HSV, "HSVTest");
  }
  
  @Test
  public void testPink() {
    testHelp(ColormapTypesEnum.COLORMAP_PINK, "PinkTest");
  }
  
  @Test
  public void testHot() {
    testHelp(ColormapTypesEnum.COLORMAP_HOT, "HotTest");
  }
  
  @Test
  public void testParula() {
    testHelp(ColormapTypesEnum.COLORMAP_PARULA, "ParulaTest");
  }
  
  void testHelp(ColormapTypesEnum map, String testName) {
    test(() -> setup(map), (pip) -> validate(pip), "cvColorMap" + testName);
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
    Mat genMat = (Mat) pip.getOutput(0, GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 1.0);
  }
}
