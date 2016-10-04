package edu.wpi.grip.ui.codegeneration.cv;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.DesaturateOperation;
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

public class CVThreshold extends AbstractGenerationTesting {
  private static final double thresh = 50;
  private static final double maxval = 200;

  boolean setup(String type) {
    Step desat = gen.addStep(new OperationMetaData(DesaturateOperation.DESCRIPTION, () -> new
        DesaturateOperation(isf, osf)));
    loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
    for (InputSocket sock : desat.getInputSockets()) {
      if (sock.getSocketHint().getIdentifier().equals("Input")) {
        gen.connect(imgOut, sock);
      }
    }

    Step step = gen.addStep(opUtil.getMetaData("CV Threshold"));
    OutputSocket deImg = desat.getOutputSockets().get(0);
    gen.connect(deImg, step.getInputSockets().get(0));
    step.getInputSockets().get(1).setValue(thresh);
    step.getInputSockets().get(2).setValue(maxval);
    HelperTools.setEnumSocket(step.getInputSockets().get(3), type);
    return true;
  }

  @Test
  public void thresholdTest() {
    test(() -> setup("THRESH_BINARY"), (pip) -> validate(pip), "cvThresholdTest");
  }

  @Test
  public void thresholdInvTest() {
    test(() -> setup("THRESH_BINARY_INV"), (pip) -> validate(pip), "cvThresholdInvTest");
  }

  @Test
  public void thresholdTruncTest() {
    test(() -> setup("THRESH_TRUNC"), (pip) -> validate(pip), "cvThresholdTruncTest");
  }

  @Test
  public void thresholdZeroTest() {
    test(() -> setup("THRESH_TOZERO"), (pip) -> validate(pip), "cvThresholdZeroTest");
  }

  @Test
  public void thresholdZeroInvTest() {
    test(() -> setup("THRESH_TOZERO_Inv"), (pip) -> validate(pip), "cvThresholdZeroInvTest");
  }

  @Test
  public void thresholdOtsuTest() {
    test(() -> setup("THRESH_OTSU"), (pip) -> validate(pip), "cvThresholdOtsuTest");
  }

  @Test
  public void thresholdTriTest() {
    test(() -> setup("THRESH_TRIANGLE"), (pip) -> validate(pip), "cvThresholdTriangleTest");
  }

  void validate(PipelineInterfacer pip) {
    ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
    runner.runPipeline();
    pip.setMatSource(0, Files.gompeiJpegFile.file);
    pip.process();
    Optional out = pipeline.getSteps().get(1).getOutputSockets().get(0).getValue();
    assertTrue("Pipeline did not process", out.isPresent());
    assertFalse("Pipeline output is empty", ((org.bytedeco.javacpp.opencv_core.Mat) out.get())
        .empty());
    Mat genMat = (Mat) pip.getOutput("CV_Threshold_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 1.0);
  }
}
