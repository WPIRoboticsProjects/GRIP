package edu.wpi.grip.ui.codegeneration.cv;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.CVOperations;
import edu.wpi.grip.core.operations.composite.DesaturateOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.generated.opencv_imgproc.enumeration.AdaptiveThresholdTypesEnum;
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

public class CVAdaptiveThreshold extends AbstractGenerationTesting {

  boolean setup(AdaptiveThresholdTypesEnum adaptMethod,
                CVOperations.CVAdaptThresholdTypesEnum threshMethod) {
    Step desat = gen.addStep(new OperationMetaData(DesaturateOperation.DESCRIPTION, () -> new
        DesaturateOperation(isf, osf)));
    loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
    for (InputSocket sock : desat.getInputSockets()) {
      if (sock.getSocketHint().getIdentifier().equals("Input")) {
        gen.connect(imgOut, sock);
      }
    }

    Step step = gen.addStep(opUtil.getMetaData("CV adaptiveThreshold"));
    OutputSocket deImg = desat.getOutputSockets().get(0);
    gen.connect(deImg, step.getInputSockets().get(0));
    step.getInputSockets().get(1).setValue(new Double(100.0));
    step.getInputSockets().get(2).setValue(adaptMethod);
    step.getInputSockets().get(3).setValue(threshMethod);
    step.getInputSockets().get(4).setValue(new Double(5.0));
    return true;
  }

  @Test
  public void binaryMeanTest() {
    test(() -> setup(AdaptiveThresholdTypesEnum.ADAPTIVE_THRESH_MEAN_C, 
        CVOperations.CVAdaptThresholdTypesEnum.THRESH_BINARY),
        (pip) -> validate(pip), "cvBinaryMeanATTest");
  }

  @Test
  public void binaryInvMeanTest() {
    test(() -> setup(AdaptiveThresholdTypesEnum.ADAPTIVE_THRESH_MEAN_C,
        CVOperations.CVAdaptThresholdTypesEnum.THRESH_BINARY_INV),
        (pip) -> validate(pip), "cvBinaryInvMeanATTest");
  }

  @Test
  public void binaryGaussianTest() {
    test(() -> setup(AdaptiveThresholdTypesEnum.ADAPTIVE_THRESH_GAUSSIAN_C,
        CVOperations.CVAdaptThresholdTypesEnum.THRESH_BINARY),
        (pip) -> validate(pip), "cvBinaryGaussianATTest");
  }

  @Test
  public void binaryInvGaussianTest() {
    test(() -> setup(AdaptiveThresholdTypesEnum.ADAPTIVE_THRESH_GAUSSIAN_C,
        CVOperations.CVAdaptThresholdTypesEnum.THRESH_BINARY_INV),
        (pip) -> validate(pip), "cvBinaryInvGaussianATTest");
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
    Mat genMat = (Mat) pip.getOutput("CV_AdaptiveThreshold_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 1.0);
  }
}
