package edu.wpi.grip.ui.codegeneration.cv;


import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.opencv.NewPointOperation;
import edu.wpi.grip.core.sockets.InputSocket;
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

public class CVRectangle extends AbstractGenerationTesting {
  private static final double x0 = 20;
  private static final double y0 = 40;
  private static final double x1 = 100;
  private static final double y1 = 120;
  private static final double thickness = 2;
  private static final double shift = 3;


  boolean setup(String type) {
    Step step0 = gen.addStep(new OperationMetaData(NewPointOperation.DESCRIPTION, () -> new
        NewPointOperation(isf, osf)));
    for (InputSocket sock : step0.getInputSockets()) {
      if (sock.getSocketHint().getIdentifier().equals("x")) {
        sock.setValue(x0);
      } else if (sock.getSocketHint().getIdentifier().equals("y")) {
        sock.setValue(y0);
      }
    }

    Step step1 = gen.addStep(new OperationMetaData(NewPointOperation.DESCRIPTION, () -> new
        NewPointOperation(isf, osf)));
    for (InputSocket sock : step1.getInputSockets()) {
      if (sock.getSocketHint().getIdentifier().equals("x")) {
        sock.setValue(x1);
      } else if (sock.getSocketHint().getIdentifier().equals("y")) {
        sock.setValue(y1);
      }
    }

    Step step = gen.addStep(opUtil.getMetaData("CV Rectangle"));
    loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
    OutputSocket point0 = step0.getOutputSockets().get(0);
    OutputSocket point1 = step1.getOutputSockets().get(0);
    gen.connect(imgOut, step.getInputSockets().get(0));
    gen.connect(point0, step.getInputSockets().get(1));
    gen.connect(point1, step.getInputSockets().get(2));
    step.getInputSockets().get(4).setValue(thickness);
    HelperTools.setEnumSocket(step.getInputSockets().get(5), type);
    step.getInputSockets().get(6).setValue(shift);
    return true;
  }

  @Test
  public void rect8Test() {
    test(() -> setup("LINE_8"), (pip) -> validate(pip), "cvRect8Test");
  }

  @Test
  public void rect4Test() {
    test(() -> setup("LINE_4"), (pip) -> validate(pip), "cvRect4Test");
  }

  @Test
  public void rectATest() {
    test(() -> setup("LINE_AA"), (pip) -> validate(pip), "cvRectATest");
  }

  @Test
  public void rectFTest() {
    test(() -> setup("FILLED"), (pip) -> validate(pip), "cvRectFTest");
  }


  void validate(PipelineInterfacer pip) {
    ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
    runner.runPipeline();
    pip.setMatSource(0, Files.gompeiJpegFile.file);
    pip.process();
    Optional out = pipeline.getSteps().get(2).getOutputSockets().get(0).getValue();
    assertTrue("Pipeline did not process", out.isPresent());
    assertFalse("Pipeline output is empty", ((org.bytedeco.javacpp.opencv_core.Mat) out.get())
        .empty());
    Mat genMat = (Mat) pip.getOutput("CV_Rectangle_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 1.0);
  }
}
