package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.ContoursReport;
import edu.wpi.grip.core.operations.composite.FindContoursOperation;
import edu.wpi.grip.core.operations.composite.HSLThresholdOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.Files;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgproc;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertTrue;

@Category(GenerationTesting.class)
public class FindContoursGenerationTesting extends AbstractGenerationTesting {
  private final List<Number> hVal = new ArrayList<Number>();
  private final List<Number> sVal = new ArrayList<Number>();
  private final List<Number> lVal = new ArrayList<Number>();

  public FindContoursGenerationTesting() {
    hVal.add(new Double(1.2));
    hVal.add(new Double(51.0));
    sVal.add(new Double(2.2));
    sVal.add(new Double(83.2));
    lVal.add(new Double(1.0));
    lVal.add(new Double(101.0));
  }

  void generatePipeline(boolean externalBool) {
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
  }

  @Test
  public void findContoursWOExternalTest() {
    test(() -> {
      generatePipeline(false);
      return true;
    }, (pip) -> pipelineTest(pip), "FindContoursTest");
  }

  @Test
  public void findContoursExternalTest() {
    test(() -> {
      generatePipeline(true);
      return true;
    }, (pip) -> pipelineTest(pip), "FindExternalContoursTest");
  }


  void pipelineTest(PipelineInterfacer pip) {
    ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
    runner.runPipeline();
    Optional out1 = pipeline.getSteps().get(1).getOutputSockets().get(0).getValue();
    assertTrue("Pipeline did not process", out1.isPresent());
    ContoursReport conOut = (ContoursReport) out1.get();
    opencv_core.Mat matOut = new opencv_core.Mat();
    matOut.create(conOut.getRows(), conOut.getCols(), opencv_core.CV_8UC3);
    opencv_core.bitwise_xor(matOut, matOut, matOut);
    opencv_imgproc.drawContours(matOut, conOut.getContours(), -1,
        opencv_core.Scalar.WHITE);

    // exporter.export(pipeline, Language.JAVA,new File() , false);
    pip.setMatSource(0, Files.imageFile.file);
    pip.process();
    Mat genMat =
        new Mat(conOut.getRows(), conOut.getCols(), opencv_core.CV_8UC3, new Scalar(0, 0, 0));
    List<MatOfPoint> gen =
        (List<MatOfPoint>) pip.getOutput("Find_Contours_Output", GenType.CONTOURS);

    Imgproc.drawContours(genMat, gen, -1, new Scalar(255, 255, 255));

    Mat gripMat = HelperTools.bytedecoMatToCVMat(matOut);
    // HelperTools.displayMats(genMat,gripMat);
    assertMatWithin(genMat, gripMat, 8.0);
    assertSame("Number of Contours is not the same. grip: " + conOut.getContours().size() + " gen: "
        + gen.size(), (int) conOut.getContours().size(), gen.size());

  }
}
