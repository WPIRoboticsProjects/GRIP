package edu.wpi.grip.ui.codegeneration.cv;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.BlurOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.generated.opencv_core.enumeration.CmpTypesEnum;
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

public class CVCompare extends AbstractGenerationTesting {

  boolean set(CmpTypesEnum val) {
    Step blur = gen.addStep(new OperationMetaData(BlurOperation.DESCRIPTION, () -> new
        BlurOperation(isf, osf)));
    loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
    for (InputSocket sock : blur.getInputSockets()) {
      String sockHint = sock.getSocketHint().getIdentifier();
      if ("Input".equals(sockHint)) {
        gen.connect(imgOut, sock);
      } else if ("Radius".equals(sockHint)) {
        sock.setValue(new Double(10.0));
      } else if (sock.getSocketHint().getIdentifier().equals("Type")) {
        HelperTools.setEnumSocket(sock, "Box Blur");
      }
    }
    Step cmp = gen.addStep(opUtil.getMetaData("CV Compare"));
    gen.connect(imgOut, cmp.getInputSockets().get(0));
    gen.connect(blur.getOutputSockets().get(0), cmp.getInputSockets().get(1));
    cmp.getInputSockets().get(2).setValue(val);
    return true;
  }
  
  @Test
  public void cvCompareEqTest() {
    test(() -> set(CmpTypesEnum.CMP_EQ), (pip) -> validate(pip), "CvCmpEqTest");
  }
  
  @Test
  public void cvCompareGtTest() {
    test(() -> set(CmpTypesEnum.CMP_GT), (pip) -> validate(pip), "CvCmpGtTest");
  }
  
  @Test
  public void cvCompareGeTest() {
    test(() -> set(CmpTypesEnum.CMP_GE), (pip) -> validate(pip), "CvCmpGeTest");
  }
  
  @Test
  public void cvCompareLtTest() {
    test(() -> set(CmpTypesEnum.CMP_LT), (pip) -> validate(pip), "CvCmpLtTest");
  }
  
  @Test
  public void cvCompareLeTest() {
    test(() -> set(CmpTypesEnum.CMP_LE), (pip) -> validate(pip), "CvCmpLeTest");
  }
  
  @Test
  public void cvCompareNeTest() {
    test(() -> set(CmpTypesEnum.CMP_NE), (pip) -> validate(pip), "CvCmpNeTest");
  }
  
  void validate(PipelineInterfacer pip) {
    ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
    runner.runPipeline();
    Optional out = pipeline.getSteps().get(1).getOutputSockets().get(0).getValue();
    assertTrue("Pipeline did not process", out.isPresent());
    assertFalse("Pipeline output is empty", ((org.bytedeco.javacpp.opencv_core.Mat) out.get())
        .empty());
    pip.setMatSource(0, Files.gompeiJpegFile.file);
    pip.process();
    Mat genMat = (Mat) pip.getOutput("CV_Compare_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 2.0);
  }
}
