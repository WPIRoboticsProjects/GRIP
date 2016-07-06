package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.SwitchOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;

import org.junit.Test;

import javax.inject.Inject;


import static org.junit.Assert.assertEquals;

public class SwitchTest extends AbstractGenerationTest {
  @Inject
  private Exporter exporter;

  boolean setup(Object onTrue, Object onFalse, Boolean initVal) {
    Step step = gen.addStep(new OperationMetaData(
        SwitchOperation.DESCRIPTION, () -> new SwitchOperation(isf, osf)));
    for (InputSocket sock : step.getInputSockets()) {
      String sockHint = sock.getSocketHint().getIdentifier();
      if (sockHint.equalsIgnoreCase("if True")) {
        sock.setValue(onTrue);
      } else if (sockHint.equalsIgnoreCase("if False")) {
        sock.setValue(onFalse);
      } else if (sockHint.equals("switch")) {
        sock.setValue(initVal);
      }
    }
    return true;
  }

  @Test
  public void testNumberTrueInit() {
    Number onTrue = new Double(Math.PI);
    Number onFalse = new Double(Math.E);
    Boolean initVal = new Boolean(true);
    test(() -> setup(onTrue, onFalse, initVal),
        (pip) -> validate(pip, onTrue, onFalse, initVal),
        "SwitchNumTrueTest");
  }

  @Test
  public void testNumberFalseInit() {
    Number onTrue = new Double(Math.PI);
    Number onFalse = new Double(Math.E);
    Boolean initVal = new Boolean(false);
    test(() -> setup(onTrue, onFalse, initVal),
        (pip) -> validate(pip, onTrue, onFalse, initVal),
        "SwitchNumFalseTest");
  }

  void validate(PipelineInterfacer pip, Object onTrue, Object onFalse, Boolean initVal) {
    pip.process();
    Object out = pip.getOutput(0, GenType.NUMBER);
    if (initVal.booleanValue()) {
      assertEquals(errorMessage(true) + " initially", onTrue, out);
      pip.setSwitch(0, false);
      pip.process();
      out = pip.getOutput(0, GenType.NUMBER);
      assertEquals(errorMessage(false), onFalse, out);
    } else {
      assertEquals(errorMessage(false) + " initially", onFalse, out);
      pip.setSwitch(0, true);
      pip.process();
      out = pip.getOutput(0, GenType.NUMBER);
      assertEquals(errorMessage(true), onTrue, out);
    }
  }

  private String errorMessage(boolean expected) {
    return "Output was not the on " + expected + " value";
  }
}
