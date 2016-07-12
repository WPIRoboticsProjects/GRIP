package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.ContoursReport;
import edu.wpi.grip.core.operations.composite.FilterContoursOperation;
import edu.wpi.grip.core.operations.composite.FindContoursOperation;
import edu.wpi.grip.core.operations.composite.HSLThresholdOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.Files;

import org.bytedeco.javacpp.opencv_core;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

@Category(GenerationTest.class)
public class FilterContoursGenerationTest extends AbstractGenerationTest {
  private final boolean externalBool = false;
  private List<Number> hVal = new ArrayList<Number>();
  private List<Number> sVal = new ArrayList<Number>();
  private List<Number> lVal = new ArrayList<Number>();
  
  public FilterContoursGenerationTest() {
    hVal.add(new Double(1.2));
    hVal.add(new Double(51.0));
    sVal.add(new Double(2.2));
    sVal.add(new Double(83.2));
    lVal.add(new Double(1.0));
    lVal.add(new Double(101.0));
  }

  void generatePipeline(String socketName, Object value) {
    Step step0 = gen.addStep(new OperationMetaData(HSLThresholdOperation.DESCRIPTION, () -> new
        HSLThresholdOperation(isf, osf)));
    ImageFileSource img = loadImage(Files.imageFile);
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

    Step step1 = gen.addStep(new OperationMetaData(FindContoursOperation.DESCRIPTION, () -> new
        FindContoursOperation(isf, osf)));
    OutputSocket imgOut1 = pipeline.getSteps().get(0).getOutputSockets().get(0);
    for (InputSocket sock : step1.getInputSockets()) {
      if (sock.getSocketHint().isCompatibleWith(imgOut1.getSocketHint())) {
        gen.connect(imgOut1, sock);
      } else if (sock.getSocketHint().getIdentifier().equals("External Only")) {
        sock.setValue(externalBool);
      }
    }

    Step step2 = gen.addStep(new OperationMetaData(FilterContoursOperation.DESCRIPTION, () -> new
        FilterContoursOperation(isf, osf)));
    OutputSocket imgOut2 = pipeline.getSteps().get(1).getOutputSockets().get(0);
    for (InputSocket sock : step2.getInputSockets()) {
      if (sock.getSocketHint().isCompatibleWith(imgOut2.getSocketHint())) {
        gen.connect(imgOut2, sock);
      } else if (sock.getSocketHint().getIdentifier().equals(socketName)) {
        sock.setValue(value);
      }
    }
  }

  @Test
  public void filterContoursAreaTest() {
    test(() -> {
      generatePipeline("Min Area", new Double(50));
      return true;
    },
        (pip) -> testPipeline(pip), "FilterContoursAreaTest");
  }

  @Test
  public void filterContoursMinWidthTest() {
    test(() -> {
      generatePipeline("Min Width", new Double(50));
      return true;
    },
        (pip) -> testPipeline(pip), "FilterContoursMinWidthTest");
  }

  @Test
  public void filterContoursMaxHeightTest() {
    test(() -> {
      generatePipeline("Max Height", new Double(50));
      return true;
    },
        (pip) -> testPipeline(pip), "FilterContoursMaxHeightTest");
  }

  @Test
  public void filterContoursSolidityTest() {
    test(() -> {
      generatePipeline("Solidity", Arrays.asList(1.0, 50.0));
      return true;
    },
        (pip) -> testPipeline(pip), "FilterContoursSolidityTest");
  }

  @Test
  public void filterContoursMinVerticesTest() {
    test(() -> {
      generatePipeline("Min Vertices", new Double(10));
      return true;
    },
        (pip) -> testPipeline(pip), "FilterContoursMinVerticesTest");
  }

  @Ignore("Grip ratio rounds before it should. Generated code is more correct")
  @Test
  public void filterContoursMaxRatioTest() {
    test(() -> {
      generatePipeline("Min Ratio", new Double(0.1));
      return true;
    },
        (pip) -> testPipeline(pip), "FilterContoursMaxRatioTest");
  }

  void testPipeline(PipelineInterfacer pip) {
    ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
    runner.runPipeline();
    Optional out = pipeline.getSteps().get(2).getOutputSockets().get(0).getValue();
    assertTrue("Pipeline did not process", out.isPresent());
    ContoursReport conOut = (ContoursReport) out.get();
    opencv_core.Mat matOut = new opencv_core.Mat();
    matOut.create(conOut.getRows(), conOut.getCols(), opencv_core.CV_8UC3);
    opencv_core.bitwise_xor(matOut, matOut, matOut);
    org.bytedeco.javacpp.opencv_imgproc.drawContours(
        matOut, conOut.getContours(), -1, opencv_core.Scalar.WHITE);
    pip.setMatSource(0, Files.imageFile.file);
    pip.process();
    Mat genMat = new Mat(conOut.getRows(), conOut.getCols(), 
        opencv_core.CV_8UC3, new Scalar(0, 0, 0));
    List<MatOfPoint> gen = (List<MatOfPoint>) pip.getOutput(2, GenType.CONTOURS);
    Imgproc.drawContours(genMat, gen, -1, new Scalar(255, 255, 255));
    Mat gripMat = HelperTools.bytedecoMatToCVMat(matOut);
    //HelperTools.displayMats(genMat, gripMat);
    assertMatWithin(genMat, gripMat, 8.0);
    assertTrue("Number of Contours is not the same. grip: " 
        + conOut.getContours().size() + " gen: " + gen.size(), 
        Math.abs(conOut.getContours().size() - gen.size()) < 5);
  }
}
