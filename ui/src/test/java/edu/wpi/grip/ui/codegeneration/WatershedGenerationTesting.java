package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.FindContoursOperation;
import edu.wpi.grip.core.operations.composite.HSLThresholdOperation;
import edu.wpi.grip.core.operations.composite.WatershedOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.Files;

import org.junit.Ignore;
import org.junit.Test;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class WatershedGenerationTesting extends AbstractGenerationTesting {
  private static final boolean externalBool = false;
  private final List<Number> hVal = new ArrayList<Number>();
  private final List<Number> sVal = new ArrayList<Number>();
  private final List<Number> lVal = new ArrayList<Number>();

  public WatershedGenerationTesting() {
    hVal.add(new Double(1.2));
    hVal.add(new Double(51.0));
    sVal.add(new Double(2.2));
    sVal.add(new Double(83.2));
    lVal.add(new Double(1.0));
    lVal.add(new Double(101.0));
  }

  void generatePipeline() {
    Step step0 = gen.addStep(new OperationMetaData(HSLThresholdOperation.DESCRIPTION,
        () -> new HSLThresholdOperation(isf, osf)));
    loadImage(Files.imageFile);
    OutputSocket imgOut0 = pipeline.getSources().get(0).getOutputSockets().get(0);

    for (InputSocket sock : step0.getInputSockets()) {
      if (sock.getSocketHint().isCompatibleWith(imgOut0.getSocketHint())) {
        gen.connect(imgOut0, sock);
      } else if (sock.getSocketHint().getIdentifier().equals("Hue")) {
        sock.setValue(hVal);
      } else if (sock.getSocketHint().getIdentifier().equals("Saturation")) {
        sock.setValue(sVal);
      } else if (sock.getSocketHint().getIdentifier().equals("Luminance")) {
        sock.setValue(lVal);
      }
    }

    Step step1 = gen.addStep(new OperationMetaData(FindContoursOperation.DESCRIPTION,
        () -> new FindContoursOperation(isf, osf)));
    OutputSocket imgOut1 = pipeline.getSteps().get(0).getOutputSockets().get(0);
    for (InputSocket sock : step1.getInputSockets()) {
      if (sock.getSocketHint().isCompatibleWith(imgOut1.getSocketHint())) {
        gen.connect(imgOut1, sock);
      } else if (sock.getSocketHint().getIdentifier().equals("External Only")) {
        sock.setValue(externalBool);
      }
    }

    Step step2 = gen.addStep(new OperationMetaData(WatershedOperation.DESCRIPTION,
        () -> new WatershedOperation(isf, osf)));
    OutputSocket imgOut2 = pipeline.getSteps().get(1).getOutputSockets().get(0);
    for (InputSocket sock : step2.getInputSockets()) {
      if (sock.getSocketHint().isCompatibleWith(imgOut2.getSocketHint())) {
        gen.connect(imgOut2, sock);
      } else if (sock.getSocketHint().isCompatibleWith(imgOut0.getSocketHint())) {
        gen.connect(imgOut0, sock);
      }
    }
  }

  @Ignore("Grip Watershed is broken so it doesn't make sense to fix the generated watershed.")
  @Test
  public void watershedTest() {
    test(() -> {
      generatePipeline();
      return true;
    }, (pip) -> pipelineTest(pip), "WatershedTest");
  }

  void pipelineTest(PipelineInterfacer pip) {
    ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
    runner.runPipeline();
    Optional out = pipeline.getSteps().get(2).getOutputSockets().get(0).getValue();
    assertTrue("Pipeline did not process", out.isPresent());
    org.bytedeco.javacpp.opencv_core.Mat matOut = (org.bytedeco.javacpp.opencv_core.Mat) out.get();
    Mat gripMat = HelperTools.bytedecoMatToCVMat(matOut);

    pip.setMatSource(0, Files.imageFile.file);
    pip.process();
    Mat genMat = (Mat) pip.getOutput("Watershed_Output", GenType.IMAGE);

    HelperTools.displayMats(genMat, gripMat);
    assertMatWithin(genMat, gripMat, 8.0);

  }
}
