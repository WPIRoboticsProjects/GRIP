package edu.wpi.grip.ui.codegeneration.cv;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.opencv.NewSizeOperation;
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

public class CVGaussianBlur extends AbstractGenerationTesting {
  private static final double width = 15;
  private static final double height = 1;
  private static final double sigmax = 3;
  private static final double sigmay = 41;

  boolean setup(String type) {
    Step step0 = gen.addStep(new OperationMetaData(NewSizeOperation.DESCRIPTION, () -> new
        NewSizeOperation(isf, osf)));
    for (InputSocket sock : step0.getInputSockets()) {
      if (sock.getSocketHint().getIdentifier().equals("width")) {
        sock.setValue(width);
      } else if (sock.getSocketHint().getIdentifier().equals("height")) {
        sock.setValue(height);
      }
    }

    Step step = gen.addStep(opUtil.getMetaData("CV GaussianBlur"));
    loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
    OutputSocket size = step0.getOutputSockets().get(0);
    gen.connect(imgOut, step.getInputSockets().get(0));
    gen.connect(size, step.getInputSockets().get(1));
    step.getInputSockets().get(2).setValue(sigmax);
    step.getInputSockets().get(3).setValue(sigmay);
    HelperTools.setEnumSocket(step.getInputSockets().get(4), type);
    return true;
  }

  @Test
  public void gaussianTest() {
    test(() -> setup("BORDER_DEFAULT"), (pip) -> validate(pip), "cvgaussianTest");
  }

  @Test
  public void gaussianConTest() {
    test(() -> setup("BORDER_CONSTANT"), (pip) -> validate(pip), "cvgaussianConTest");
  }

  @Test
  public void gaussianRepTest() {
    test(() -> setup("BORDER_REPLICATE"), (pip) -> validate(pip), "cvgaussianRepTest");
  }

  @Test
  public void gaussianRefTest() {
    test(() -> setup("BORDER_REFLECT"), (pip) -> validate(pip), "cvgaussianRefTest");
  }

  @Test
  public void gaussianRef1Test() {
    test(() -> setup("BORDER_REFLECT_101"), (pip) -> validate(pip), "cvgaussianRef1Test");
  }

  @Test
  public void gaussianRefN1Test() {
    test(() -> setup("BORDER_REFLECT101"), (pip) -> validate(pip), "cvgaussianRefN1Test");
  }

  @Test
  public void gaussianIsoTest() {
    test(() -> setup("BORDER_ISOLATED"), (pip) -> validate(pip), "cvgaussianIsoTest");
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
    Mat genMat = (Mat) pip.getOutput("CV_GaussianBlur_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    //HelperTools.displayMats(genMat, gripMat);
    assertMatWithin(genMat, gripMat, 1.0);
  }
}
