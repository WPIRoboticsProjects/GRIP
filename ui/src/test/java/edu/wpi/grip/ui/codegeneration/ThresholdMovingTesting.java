package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.BlurOperation;
import edu.wpi.grip.core.operations.composite.SwitchOperation;
import edu.wpi.grip.core.operations.composite.ThresholdMoving;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.Files;

import org.junit.Test;
import org.opencv.core.Mat;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class ThresholdMovingTesting extends AbstractGenerationTesting {
  ThresholdSwitch[] threshs = null;

  /**
   * Sets up the pipeline with given number of moving thresholds.
   *
   * @param num number of moving thresholds to put in pipeline, 1 indexed.
   * @return an array of the switch steps used to trigger the moving thresholds.
   */
  public ThresholdSwitch[] setupThreshold(int num) {
    loadImage(Files.gompeiJpegFile);
    ThresholdSwitch[] threshs = new ThresholdSwitch[num];
    OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
    for (int idx = 0; idx < num; idx++) {
      Step blur = gen.addStep(
          new OperationMetaData(BlurOperation.DESCRIPTION, () -> new BlurOperation(isf, osf)));
      for (InputSocket sock : blur.getInputSockets()) {
        String socketHint = sock.getSocketHint().getIdentifier();
        if ("Input".equals(socketHint)) {
          gen.connect(imgOut, sock);
        } else if ("Radius".equals(socketHint)) {
          sock.setValue(new Double(Math.PI * (idx + 1)));
        }
      } // end of blur
      Step swtch = gen.addStep(
          new OperationMetaData(SwitchOperation.DESCRIPTION, () -> new SwitchOperation(isf, osf)));
      for (InputSocket sock : swtch.getInputSockets()) {
        String sockHint = sock.getSocketHint().getIdentifier();
        if ("If True".equals(sockHint)) {
          gen.connect(imgOut, sock);
        } else if ("If False".equals(sockHint)) {
          gen.connect(blur.getOutputSockets().get(0), sock);
        }
      }
      Step move = gen.addStep(
          new OperationMetaData(ThresholdMoving.DESCRIPTION, () -> new ThresholdMoving(isf, osf)));
      for (InputSocket sock : move.getInputSockets()) {
        if (sock.getSocketHint().getIdentifier().equalsIgnoreCase("image")) {
          gen.connect(swtch.getOutputSockets().get(0), sock);
        }
      }
      threshs[idx] = new ThresholdSwitch(swtch, move);
    }
    return threshs;
  }

  @Test
  public void oneThreshMoving() {
    int num = 1;
    test(() -> {
      threshs = setupThreshold(num);
      return threshs.length == num;
    }, (pip) -> validate(pip, threshs), "ThreshMovingOneTest");
  }

  @Test
  public void twoThreshMoving() {
    int num = 2;
    test(() -> {
      threshs = setupThreshold(num);
      return threshs.length == num;
    }, (pip) -> validate(pip, threshs), "ThreshMovingTwoTest");
  }

  @Test
  public void threeThreshMoving() {
    int num = 3;
    test(() -> {
      threshs = setupThreshold(num);
      return threshs.length == num;
    }, (pip) -> validate(pip, threshs), "ThreshMovingThreeTest");
  }

  void validate(PipelineInterfacer pip, ThresholdSwitch[] threshs) {
    ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
    runner.runPipeline();
    pip.setMatSource(0, Files.gompeiJpegFile.file);
    pip.process();
    for (int idx = 0; idx < threshs.length; idx++) {
      boolean value = threshs[idx].toggle();
      pip.setSwitch("Switch" + idx, value);
    }
    runner.runPipeline();
    pip.process();
    for (int idx = 0; idx < threshs.length; idx++) {
      boolean value = threshs[idx].toggle();
      pip.setSwitch("Switch" + idx, value);
    }
    runner.runPipeline();
    pip.process();
    if (threshs.length > 1) {
      for (int idx = 0; idx < threshs.length; idx++) {
        Mat genMat = (Mat) pip.getOutput("Threshold_Moving_" + idx + "_Output", GenType.IMAGE);
        Mat gripMat = threshs[idx].getOutput();
        // HelperTools.displayMats(genMat, gripMat);
        assertMatWithin(genMat, gripMat, 5.0);
      }
    } else {
      Mat genMat = (Mat) pip.getOutput("Threshold_Moving_Output", GenType.IMAGE);
    }
  }


  static class ThresholdSwitch {
    Step swi;
    Step thresh;

    public ThresholdSwitch(Step swi, Step thresh) {
      this.swi = swi;
      this.thresh = thresh;
    }

    public boolean toggle() {
      boolean result = false;
      for (InputSocket sock : swi.getInputSockets()) {
        if (sock.getSocketHint().getIdentifier().equals("switch")) {
          Boolean val = (Boolean) sock.getValue().get();
          sock.setValue(!val.booleanValue());
          result = !val.booleanValue();
        }
      }
      return result;
    }

    public Mat getOutput() {
      Optional out = thresh.getOutputSockets().get(0).getValue();
      assertTrue("Pipeline did not process", out.isPresent());
      return HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat) out.get());
    }
  }
}
