package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Range;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.HSLThresholdOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.util.Files;

public class GripIconHSLSetup {
  private static final Range defaultHVal = new Range(0, 49);
  private static final Range defaultSVal = new Range(0, 41);
  private static final Range defaultLVal = new Range(0, 67);

  public static void setup(AbstractGenerationTesting caller) {
    setup(caller, defaultHVal, defaultSVal, defaultLVal);
  }

  public static void setup(AbstractGenerationTesting caller, Range hVal, Range sVal,
                           Range lVal) {
    Step hsl = caller.gen.addStep(new OperationMetaData(HSLThresholdOperation.DESCRIPTION,
        () -> new HSLThresholdOperation(caller.isf, caller.osf)));
    caller.loadImage(Files.imageFile);
    OutputSocket imgOut = caller.pipeline.getSources().get(0).getOutputSockets().get(0);
    for (InputSocket sock : hsl.getInputSockets()) {
      if ("Input".equals(sock.getSocketHint().getIdentifier())) {
        caller.gen.connect(imgOut, sock);
      } else if ("Hue".equals(sock.getSocketHint().getIdentifier())) {
        sock.setValue(hVal);
      } else if ("Saturation".equals(sock.getSocketHint().getIdentifier())) {
        sock.setValue(sVal);
      } else if ("Luminance".equals(sock.getSocketHint().getIdentifier())) {
        sock.setValue(lVal);
      }
    }
  }

  public static Range getHVal() {
    return defaultHVal;
  }

  public static Range getSVal() {
    return defaultSVal;
  }

  public static Range getLVal() {
    return defaultLVal;
  }


}
