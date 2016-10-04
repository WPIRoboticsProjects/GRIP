package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.DistanceTransformOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.Files;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(GenerationTesting.class)
public class DistanceTransformGenerationTesting extends AbstractGenerationTesting {
  String distType;
  String maskSize;
  static String[][] params = new String[9][2];

  static {
    String[] type = {"CV_DIST_L1", "CV_DIST_L2", "CV_DIST_C"};
    String[] size = {"0x0", "3x3", "5x5"};
    for (int typeIdx = 0; typeIdx < 3; typeIdx++) {
      for (int sizeIdx = 0; sizeIdx < 3; sizeIdx++) {
        int idx = 3 * typeIdx + sizeIdx;
        params[idx][0] = type[typeIdx];
        params[idx][1] = size[sizeIdx];
      }
    }
  }

  public boolean init() {
    ArrayList<Number> lVal = new ArrayList<Number>();
    lVal.add(new Double(0.0));
    lVal.add(new Double(250.0));
    GripIconHSLSetup.setup(this, GripIconHSLSetup.getHVal(), GripIconHSLSetup.getSVal(), lVal);
    Step dist = gen.addStep(new OperationMetaData(DistanceTransformOperation.DESCRIPTION,
        () -> new DistanceTransformOperation(isf, osf)));
    // output from HSL
    OutputSocket hslImg = pipeline.getSteps().get(0).getOutputSockets().get(0);
    for (InputSocket sock : dist.getInputSockets()) {
      if (sock.getSocketHint().getIdentifier().equals("Input")) {
        gen.connect(hslImg, sock);
      } else if (sock.getSocketHint().getIdentifier().equals("Type")) {
        HelperTools.setEnumSocket(sock, distType);
      } else if (sock.getSocketHint().getIdentifier().equals("Mask size")) {
        HelperTools.setEnumSocket(sock, maskSize);
      }
    }
    return true;
  }

  public void distanceTransformTest(int num) {
    distType = params[num][0];
    maskSize = params[num][1];
    test(() -> init(), (pip) -> validate(pip),
        ("DistTrans" + distType + maskSize + "Test").replace(" ", "").replace("_", ""));
  }

  void validate(PipelineInterfacer pip) {
    new ManualPipelineRunner(eventBus, pipeline).runPipeline();
    Optional out = pipeline.getSteps().get(1).getOutputSockets().get(0).getValue();
    assertTrue("Pipeline did not process", out.isPresent());
    assertFalse("Pipeline output is empty",
        ((org.bytedeco.javacpp.opencv_core.Mat) out.get()).empty());
    pip.setMatSource(0, Files.imageFile.file);
    pip.process();
    Mat genMat = (Mat) pip.getOutput("Distance_Transform_Output", GenType.IMAGE);
    Mat gripMat = new Mat();
    (HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get()))
        .convertTo(gripMat, CvType.CV_32F); // distance transform outputs a 1 channel 32F Mat but
    // grip outputs a 1 channel 8U Mat
    // HelperTools.displayMats(genMat, gripMat);
    gripMat.convertTo(genMat, gripMat.type());
    assertMatWithin(genMat, gripMat, 10.0);
  }


  @Test
  public void testL10() {
    distanceTransformTest(0);
  }

  @Test
  public void testL13() {
    distanceTransformTest(1);
  }

  @Test
  public void testL15() {
    distanceTransformTest(2);
  }

  @Test
  public void testL20() {
    distanceTransformTest(3);
  }

  @Test
  public void testL23() {
    distanceTransformTest(4);
  }

  @Test
  public void testL25() {
    distanceTransformTest(5);
  }

  @Test
  public void testC0() {
    distanceTransformTest(6);
  }

  @Test
  public void testC3() {
    distanceTransformTest(7);
  }

  @Test
  public void testC5() {
    distanceTransformTest(8);
  }
}
