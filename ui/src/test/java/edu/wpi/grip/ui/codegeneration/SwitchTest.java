package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.SwitchOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sources.MockNumberSource;
import edu.wpi.grip.core.util.ExceptionWitness;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;

import org.junit.Test;

import javax.inject.Inject;


import static org.junit.Assert.assertEquals;

public class SwitchTest extends AbstractGenerationTest {
  
  private int onTrueSourceNum = 1;
  private int onFalseSourceNum = 2;
  @Inject
  ExceptionWitness.Factory ewf;
  
  boolean setupNum(Number onTrue, Number onFalse, Boolean initVal) {
    MockNumberSource srcTrue = new MockNumberSource(ewf, onTrue.doubleValue(), osf);
    MockNumberSource srcFalse = new MockNumberSource(ewf, onFalse.doubleValue(), osf);
    return setup(srcTrue, srcFalse, initVal);
  }

  boolean setup(Source onTrue, Source onFalse, Boolean initVal) {
    Step step = gen.addStep(new OperationMetaData(
        SwitchOperation.DESCRIPTION, () -> new SwitchOperation(isf, osf)));
    for (InputSocket sock : step.getInputSockets()) {
      String sockHint = sock.getSocketHint().getIdentifier();
      if (sockHint.equalsIgnoreCase("if True")) {
        gen.connect(onTrue.getOutputSockets().get(0), sock);
      } else if (sockHint.equalsIgnoreCase("if False")) {
        gen.connect(onFalse.getOutputSockets().get(0), sock);
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
    test(() -> setupNum(onTrue, onFalse, initVal),
        (pip) -> validateNum(pip, onTrue, onFalse, initVal),
        "SwitchNumTrueTest");
  }

  @Test
  public void testNumberFalseInit() {
    Number onTrue = new Double(Math.PI);
    Number onFalse = new Double(Math.E);
    Boolean initVal = new Boolean(false);
    test(() -> setupNum(onTrue, onFalse, initVal),
        (pip) -> validateNum(pip, onTrue, onFalse, initVal),
        "SwitchNumFalseTest");
  }

  void validateNum(PipelineInterfacer pip, Number onTrue, Number onFalse, Boolean initVal) {
    System.out.println("On True is socket:" + onTrueSourceNum);
    System.out.println("On False is socket:" + onFalseSourceNum);
    pip.setNumSource(onTrueSourceNum, onTrue);
    pip.setNumSource(onFalseSourceNum, onFalse);
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
