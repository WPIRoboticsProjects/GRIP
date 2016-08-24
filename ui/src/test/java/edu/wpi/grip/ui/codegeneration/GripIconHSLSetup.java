package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.HSLThresholdOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.util.Files;

import java.util.Arrays;
import java.util.List;

public class GripIconHSLSetup {
  private static final List<Number> defaultHVal;
  private static final List<Number> defaultSVal;
  private static final List<Number> defaultLVal;

  static {
    defaultHVal = Arrays.asList(0.0d, 49.0d);
    defaultSVal = Arrays.asList(0.0d, 41.0d);
    defaultLVal = Arrays.asList(0.0d, 67.0d);
  }

  public static void setup(AbstractGenerationTesting caller) {
    setup(caller, defaultHVal, defaultSVal, defaultLVal);
  }

  public static void setup(AbstractGenerationTesting caller, List<Number> hVal, List<Number> sVal,
                           List<Number> lVal) {
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

  public static List<Number> getHVal() {
    return defaultHVal;
  }

  public static List<Number> getSVal() {
    return defaultSVal;
  }

  public static List<Number> getLVal() {
    return defaultLVal;
  }


}
