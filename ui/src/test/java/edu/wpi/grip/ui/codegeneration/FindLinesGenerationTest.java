package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.FindLinesOperation;
import edu.wpi.grip.core.operations.composite.HSLThresholdOperation;
import edu.wpi.grip.core.operations.composite.LinesReport;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.ui.codegeneration.AbstractGenerationTest;
import edu.wpi.grip.ui.codegeneration.GenerationTest;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.GripLine;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.ui.codegeneration.tools.TestLine;
import edu.wpi.grip.util.Files;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category(GenerationTest.class)
public class FindLinesGenerationTest extends AbstractGenerationTest {
  private List<Number> hVal = new ArrayList<Number>();
  private List<Number> sVal = new ArrayList<Number>();
  private List<Number> lVal = new ArrayList<Number>();

  public FindLinesGenerationTest() {
    hVal.add(new Double(1.2));
    hVal.add(new Double(51.0));
    sVal.add(new Double(2.2));
    sVal.add(new Double(83.2));
    lVal.add(new Double(1.0));
    lVal.add(new Double(101.0));
  }

  void generatePipeline() {
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

    Step step1 = gen.addStep(new OperationMetaData(FindLinesOperation.DESCRIPTION, () -> new
        FindLinesOperation(isf, osf)));
    OutputSocket imgOut1 = pipeline.getSteps().get(0).getOutputSockets().get(0);
    for (InputSocket sock : step1.getInputSockets()) {
      if (sock.getSocketHint().isCompatibleWith(imgOut1.getSocketHint())) {
        gen.connect(imgOut1, sock);
      }
    }
  }

  @Test
  public void findLinesTest() {
    test(() -> {
      generatePipeline();
      return true;
    }, (pip) -> testPipeline(pip), "FindLinesTest");
  }


  void testPipeline(PipelineInterfacer pip) {
    ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
    runner.runPipeline();
    Optional out1 = pipeline.getSteps().get(1).getOutputSockets().get(0).getValue();
    assertTrue("Pipeline did not process", out1.isPresent());
    LinesReport linOut = (LinesReport) out1.get();

    System.out.println(linOut.getLines().size());
    pip.setMatSource(0, Files.imageFile.file);
    pip.process();
    List<TestLine> gripLin = GripLine.convertReport(linOut);
    List<TestLine> genLin = (List<TestLine>) pip.getOutput(1, GenType.LINES);
    assertTrue("Number of lines is not the same. grip: " + gripLin.size() + " gen: "
        + genLin.size(), (linOut.getLines().size() - genLin.size()) < 5);
    for (int idx = 0; idx < genLin.size(); idx++) {
        assertTrue("griplin does not contain: " + genLin.get(idx),
            TestLine.containsLin(genLin.get(idx), gripLin));
    }

  }

  private double getLength(Object line) {
    try {
      return (double) line.getClass().getMethod("length").invoke(line);
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      e.printStackTrace();
      fail("length is not valid for class " + line.getClass().getSimpleName());
      return 0.0;
    }
  }

  private double getAngle(Object line) {
    try {
      return (double) line.getClass().getMethod("angle").invoke(line);
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      e.printStackTrace();
      fail("length is not valid for class " + line.getClass().getSimpleName());
      return 0.0;
    }
  }


}
