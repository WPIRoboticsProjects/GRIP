package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.HSLThresholdOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.util.Files;

import java.util.Arrays;
import java.util.List;

public class GripIconHSLSetup {
  public static List<Number> defaultHVal;
  public static List<Number> defaultSVal;
  public static List<Number> defaultLVal;

  static {
    defaultHVal = Arrays.asList(0.0d, 49.0d);
    defaultSVal = Arrays.asList(0.0d, 41.0d);
    defaultLVal = Arrays.asList(0.0d, 67.0d);
  }

  public static void setup(AbstractGenerationTest caller) {
    setup(caller, defaultHVal, defaultSVal, defaultLVal);
  }

  public static void setup(AbstractGenerationTest caller, List<Number> hVal, List<Number> sVal,
                           List<Number> lVal) {
    Step hsl = caller.gen.addStep(new OperationMetaData(HSLThresholdOperation.DESCRIPTION,
        () -> new HSLThresholdOperation(caller.isf, caller.osf)));
    ImageFileSource img = caller.loadImage(Files.imageFile);
    OutputSocket imgOut = caller.pipeline.getSources().get(0).getOutputSockets().get(0);
    for (InputSocket sock : hsl.getInputSockets()) {
      if (sock.getSocketHint().getIdentifier().equals("Input")) {
        caller.gen.connect(imgOut, sock);
      } else if (sock.getSocketHint().getIdentifier().equals("Hue")) {
        sock.setValue(hVal);
      } else if (sock.getSocketHint().getIdentifier().equals("Saturation")) {
        sock.setValue(sVal);
      } else if (sock.getSocketHint().getIdentifier().equals("Luminance")) {
        sock.setValue(lVal);
      }
    }
  }
}
