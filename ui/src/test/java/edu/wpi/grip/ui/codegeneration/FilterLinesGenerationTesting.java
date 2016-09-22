package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.FilterLinesOperation;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

@Category(GenerationTesting.class)
public class FilterLinesGenerationTesting extends AbstractGenerationTesting {
  private final List angleVal = Arrays.asList(160.0, 200.0);
  private static final int minLength = 30;

  private final List<Number> hVal = new ArrayList<Number>();
  private final List<Number> sVal = new ArrayList<Number>();
  private final List<Number> lVal = new ArrayList<Number>();

  public FilterLinesGenerationTesting() {
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

    Step step2 = gen.addStep(new OperationMetaData(FilterLinesOperation.DESCRIPTION,
        () -> new FilterLinesOperation(isf, osf)));
    OutputSocket imgOut2 = pipeline.getSteps().get(1).getOutputSockets().get(0);
    for (InputSocket sock : step2.getInputSockets()) {
      if (sock.getSocketHint().isCompatibleWith(imgOut2.getSocketHint())) {
        gen.connect(imgOut2, sock);
      } else if (sock.getSocketHint().getIdentifier().equals("Min Length")) {
        sock.setValue(minLength);
      } else if (sock.getSocketHint().getIdentifier().equals("Angle")) {
        sock.setValue(angleVal);
      }
    }
  }

  @Test
  public void filterLinesTest() {

    test(() -> {
      generatePipeline();
      return true;
    }, (pip) -> pipelineTest(pip), "FilterLinesTest");
  }


  void pipelineTest(PipelineInterfacer pip) {
    ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
    runner.runPipeline();
    Optional out2 = pipeline.getSteps().get(2).getOutputSockets().get(0).getValue();
    assertTrue("Pipeline did not process", out2.isPresent());
    LinesReport linOut = (LinesReport) out2.get();

    pip.setMatSource(0, Files.imageFile.file);
    pip.process();

    List<TestLine> gripLin = GripLine.convertReport(linOut);
    List<TestLine> genLin = (List<TestLine>) pip.getOutput("Filter_Lines_Output", GenType.LINES);
    assertTrue("Number of lines is not the same. grip: " + linOut.getLines().size() + " gen: "
        + genLin.size(), (linOut.getLines().size() - genLin.size()) < 5);
    for (int i = 0; i < genLin.size(); i++) {
      assertTrue("griplin does not contain: " + genLin.get(i),
          TestLine.containsLin(genLin.get(i), gripLin));
    }

  }

}
