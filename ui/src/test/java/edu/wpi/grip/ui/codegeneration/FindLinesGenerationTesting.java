package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.FindLinesOperation;
import edu.wpi.grip.core.operations.composite.HSLThresholdOperation;
import edu.wpi.grip.core.operations.composite.LinesReport;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.GripLine;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.ui.codegeneration.tools.TestLine;
import edu.wpi.grip.util.Files;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

@Category(GenerationTesting.class)
public class FindLinesGenerationTesting extends AbstractGenerationTesting {
  private static final Logger logger = Logger.getLogger(FindLinesGenerationTesting.class.getName());

  private final List<Number> hVal = new ArrayList<Number>();
  private final List<Number> sVal = new ArrayList<Number>();
  private final List<Number> lVal = new ArrayList<Number>();

  public FindLinesGenerationTesting() {
    hVal.add(new Double(1.2));
    hVal.add(new Double(51.0));
    sVal.add(new Double(2.2));
    sVal.add(new Double(83.2));
    lVal.add(new Double(1.0));
    lVal.add(new Double(101.0));
  }

  @Before
  public void ignoreIfWindows() {
    assumeFalse("OpenCV JNI bindings crash in Windows using Line segment detector",
        System.getProperty("os.name").toLowerCase().contains("windows"));
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

    Step step1 = gen.addStep(new OperationMetaData(FindLinesOperation.DESCRIPTION,
        () -> new FindLinesOperation(isf, osf)));
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
    }, (pip) -> pipelineTest(pip), "FindLinesTest");
  }


  void pipelineTest(PipelineInterfacer pip) {
    ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
    runner.runPipeline();
    Optional out1 = pipeline.getSteps().get(1).getOutputSockets().get(0).getValue();
    assertTrue("Pipeline did not process", out1.isPresent());
    LinesReport linOut = (LinesReport) out1.get();
    pip.setMatSource(0, Files.imageFile.file);
    pip.process();
    List<TestLine> gripLin = GripLine.convertReport(linOut);
    List<TestLine> genLin = (List<TestLine>) pip.getOutput("Find_Lines_Output", GenType.LINES);
    assertTrue(
        "Number of lines is not the same. grip: " + gripLin.size() + " gen: " + genLin.size(),
        (linOut.getLines().size() - genLin.size()) < 5);
    for (int idx = 0; idx < genLin.size(); idx++) {
      assertTrue("griplin does not contain: " + genLin.get(idx),
          TestLine.containsLin(genLin.get(idx), gripLin));
    }
  }

}
