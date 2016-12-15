package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.RGBThresholdOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.Files;

import org.junit.Test;
import org.opencv.core.Mat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("PMD.JUnitSpelling")
public class RGBThresholdTesting extends AbstractGenerationTesting {

  boolean setup() {
    final Step rgb = gen.addStep(new OperationMetaData(RGBThresholdOperation.DESCRIPTION,
        () -> new RGBThresholdOperation(isf, osf)));
    loadImage(Files.gompeiJpegFile);
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
    List<Double> rVal = Arrays.asList(46d, 188d);
    List<Double> gVal = Arrays.asList(0d, 110d);
    List<Double> bVal = Arrays.asList(0d, 110d);
    for (InputSocket sock : rgb.getInputSockets()) {
      String sockHint = sock.getSocketHint().getIdentifier();
      if ("Input".equals(sockHint)) {
        gen.connect(imgOut, sock);
      } else if ("Red".equals(sockHint)) {
        sock.setValue(rVal);
      } else if ("Green".equals(sockHint)) {
        sock.setValue(gVal);
      } else if ("Blue".equals(sockHint)) {
        sock.setValue(bVal);
      }
    }
    return true;
  }

  void pipelineTest(PipelineInterfacer pip) {
    ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
    runner.runPipeline();
    Optional out = pipeline.getSteps().get(0).getOutputSockets().get(0).getValue();
    assertTrue("Pipeline did not process", out.isPresent());
    assertFalse("Pipeline output is empty",
        ((org.bytedeco.javacpp.opencv_core.Mat) out.get()).empty());
    pip.setMatSource(0, Files.gompeiJpegFile.file);
    pip.process();
    Mat genMat = (Mat) pip.getOutput("RGB_Threshold_Output", GenType.IMAGE);
    Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    assertMatWithin(genMat, gripMat, 2.0);
  }

  @Test
  public void test() {
    test(this::setup, this::pipelineTest, "RGBThresholdTesting");
  }

}
