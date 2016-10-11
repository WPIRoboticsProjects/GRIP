package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Range;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.HSVThresholdOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.util.Files;

public class HSVThresholdSetup {
  static void setup(AbstractGenerationTesting caller) {
    Range hVal = new Range(50, 180);
    Range sVal = new Range(0, 255);
    Range vVal = new Range(0, 255);

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
