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

import static junit.framework.TestCase.assertTrue;

public class SwitchTesting extends AbstractGenerationTesting {

  private static final int onTrueSourceNum = 0;
  private static final int onFalseSourceNum = 1;
  @Inject
  ExceptionWitness.Factory ewf;

  boolean setupNum(double onTrue, double onFalse, Boolean initVal) {
    MockNumberSource srcTrue = new MockNumberSource(ewf, onTrue, osf);
    MockNumberSource srcFalse = new MockNumberSource(ewf, onFalse, osf);
    return setup(srcTrue, srcFalse, initVal);
  }

  boolean setup(Source onTrue, Source onFalse, Boolean initVal) {
    Step step = gen.addStep(
        new OperationMetaData(SwitchOperation.DESCRIPTION, () -> new SwitchOperation(isf, osf)));
    for (InputSocket sock : step.getInputSockets()) {
      String sockHint = sock.getSocketHint().getIdentifier();
      if ("if True".equalsIgnoreCase(sockHint)) {
        gen.connect(onTrue.getOutputSockets().get(0), sock);
      } else if ("if False".equalsIgnoreCase(sockHint)) {
        gen.connect(onFalse.getOutputSockets().get(0), sock);
      } else if ("switch".equals(sockHint)) {
        sock.setValue(initVal);
      }
    }
    return true;
  }

  @Test
  public void testNumberTrueInit() {
    Double onTrue = Math.PI;
    Double onFalse = Math.E;
    Boolean initVal = true;
    test(() -> setupNum(onTrue, onFalse, initVal),
        (pip) -> validateNum(pip, onTrue, onFalse, initVal), "SwitchNumTrueTest");
  }

  @Test
  public void testNumberFalseInit() {
    Double onTrue = Math.PI;
    Double onFalse = Math.E;
    Boolean initVal = false;
    test(() -> setupNum(onTrue, onFalse, initVal),
        (pip) -> validateNum(pip, onTrue, onFalse, initVal), "SwitchNumFalseTest");
  }

  void validateNum(PipelineInterfacer pip, Double switchUp, Double offSwitch, Boolean initVal) {
    pip.setNumSource(onTrueSourceNum, switchUp);
    pip.setNumSource(onFalseSourceNum, offSwitch);
    pip.process();
    Object out = pip.getOutput("Switch_Output", GenType.NUMBER);
    if (initVal.booleanValue()) {
      assertTrue(errorMessage(true) + " initially", Math.abs(switchUp - (double) out) < 0.2);
      pip.setSwitch("Switch0", false);
      pip.process();
      out = pip.getOutput("Switch_Output", GenType.NUMBER);
      assertTrue(errorMessage(false), Math.abs(offSwitch - (double) out) < 0.2);
    } else {
      assertTrue(errorMessage(false) + " initially", Math.abs(offSwitch - (double) out) < 0.2);
      pip.setSwitch("Switch0", true);
      pip.process();
      out = pip.getOutput("Switch_Output", GenType.NUMBER);
      assertTrue(errorMessage(true), Math.abs(switchUp - (double) out) < 0.2);
    }
  }

  private String errorMessage(boolean expected) {
    return "Output was not the on " + expected + " value";
  }
}
