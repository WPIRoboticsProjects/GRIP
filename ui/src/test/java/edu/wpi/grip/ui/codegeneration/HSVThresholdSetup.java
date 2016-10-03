package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.HSVThresholdOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.util.Files;

import java.util.Arrays;
import java.util.List;

public class HSVThresholdSetup {
  static void setup(AbstractGenerationTesting caller) {
    List<Number> hVal = Arrays.asList(50.0d, 180.0d);
    List<Number> sVal = Arrays.asList(0.0d, 255.0d);
    List<Number> vVal = Arrays.asList(0.0d, 255.0d);

    Step hsv = caller.gen.addStep(new OperationMetaData(HSVThresholdOperation.DESCRIPTION,
        () -> new HSVThresholdOperation(caller.isf, caller.osf)));
    caller.loadImage(Files.imageFile);
    OutputSocket imgOut = caller.pipeline.getSources().get(0).getOutputSockets().get(0);
    for (InputSocket sock : hsv.getInputSockets()) {
      if (sock.getSocketHint().getIdentifier().equals("Input")) {
        caller.gen.connect(imgOut, sock);
      } else if (sock.getSocketHint().getIdentifier().equals("Hue")) {
        sock.setValue(hVal);
      } else if (sock.getSocketHint().getIdentifier().equals("Saturation")) {
        sock.setValue(sVal);
      } else if (sock.getSocketHint().getIdentifier().equals("Value")) {
        sock.setValue(vVal);
      }
    }
  }
}
