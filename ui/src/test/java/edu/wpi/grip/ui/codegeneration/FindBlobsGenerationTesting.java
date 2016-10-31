package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.BlobsReport;
import edu.wpi.grip.core.operations.composite.FindBlobsOperation;
import edu.wpi.grip.core.operations.composite.HSLThresholdOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.Files;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opencv.core.KeyPoint;
import org.opencv.core.MatOfKeyPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

@Category(GenerationTesting.class)
public class FindBlobsGenerationTesting extends AbstractGenerationTesting {
  private final List<Number> hVal = new ArrayList<Number>();
  private final List<Number> sVal = new ArrayList<Number>();
  private final List<Number> lVal = new ArrayList<Number>();

  public FindBlobsGenerationTesting() {
    hVal.add(new Double(1.2));
    hVal.add(new Double(51.0));
    sVal.add(new Double(2.2));
    sVal.add(new Double(83.2));
    lVal.add(new Double(1.0));
    lVal.add(new Double(101.0));
  }

  void generatePipeline(boolean darkBool, double minArea, List<Double> circularity) {
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

    Step step1 = gen.addStep(new OperationMetaData(FindBlobsOperation.DESCRIPTION,
        () -> new FindBlobsOperation(isf, osf)));
    OutputSocket imgOut1 = pipeline.getSteps().get(0).getOutputSockets().get(0);
    for (InputSocket sock : step1.getInputSockets()) {
      if (sock.getSocketHint().isCompatibleWith(imgOut1.getSocketHint())) {
        gen.connect(imgOut1, sock);
      } else if (sock.getSocketHint().getIdentifier().equals("Dark Blobs")) {
        sock.setValue(darkBool);
      } else if (sock.getSocketHint().getIdentifier().equals("Min Area")) {
        sock.setValue(minArea);
      } else if (sock.getSocketHint().getIdentifier().equals("Circularity")) {
        sock.setValue(circularity);
      }
    }
  }

  @Test
  public void findBlobsTest() {
    test(() -> {
      generatePipeline(false, 0, Arrays.asList(0.0, 1.0));
      return true;
    }, (pip) -> pipelineTest(pip), "FindBlobsTest");
  }

  @Test
  public void findBlackBlobsTest() {
    test(() -> {
      generatePipeline(true, 0, Arrays.asList(0.0, 1.0));
      return true;
    }, (pip) -> pipelineTest(pip), "FindBlackBlobsTest");
  }

  @Test
  public void findSomeBlobsTest() {
    test(() -> {
      generatePipeline(false, 9, Arrays.asList(0.0, 0.9));
      return true;
    }, (pip) -> pipelineTest(pip), "FindSomeBlobsTest");
  }


  void pipelineTest(PipelineInterfacer pip) {
    ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
    runner.runPipeline();
    Optional out1 = pipeline.getSteps().get(1).getOutputSockets().get(0).getValue();
    assertTrue("Pipeline did not process", out1.isPresent());
    BlobsReport blobOut = (BlobsReport) out1.get();
    pip.setMatSource(0, Files.imageFile.file);
    pip.process();
    MatOfKeyPoint gen = (MatOfKeyPoint) pip.getOutput("Find_Blobs_Output", GenType.BLOBS);
    assertTrue("Number of Blobs is not the same. grip: " + blobOut.getBlobs().size() + " gen: "
        + gen.toList().size(), (blobOut.getBlobs().size() - gen.toList().size()) < 1);
    for (int i = 0; i < blobOut.getBlobs().size(); i++) {
      assertTrue("does not contain blob: " + gen.toList().get(i),
          containsBlob(blobOut.getBlobs(), gen.toList().get(i)));
    }
  }

  public boolean containsBlob(List<BlobsReport.Blob> blobs, KeyPoint blob) {
    for (int i = 0; i < blobs.size(); i++) {
      if ((blobs.get(i).x - blob.pt.x) <= 20 && (blobs.get(i).y - blob.pt.y) <= 20
          && (blobs.get(i).size - blob.size) <= 10) {
        return true;
      }
    }
    return false;
  }
}
