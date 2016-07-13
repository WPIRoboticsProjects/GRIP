package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.ValveOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sources.MockNumberSource;
import edu.wpi.grip.core.util.ExceptionWitness;
import edu.wpi.grip.ui.codegeneration.tools.GenType;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;

import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ValveTest extends AbstractGenerationTest {
  private Step valve;
  @Inject
  ExceptionWitness.Factory ewf;

  boolean setup(Number value) {
    valve = gen.addStep(new OperationMetaData(ValveOperation.DESCRIPTION,
        () -> new ValveOperation(isf, osf)));
    MockNumberSource src = new MockNumberSource(ewf, value.doubleValue(), osf);
    for (InputSocket sock : valve.getInputSockets()) {
      String socketHint = sock.getSocketHint().getIdentifier();
      if (socketHint.equalsIgnoreCase("Input")) {
        gen.connect(src.getOutputSockets().get(0), sock);
      }
    }
    return true;
  }

  @Test
  public void valveNumTest() {
    Double value = Math.PI;
    test(() -> setup(value), (pip) -> validate(pip, value), "ValveObjTest");
  }

  private void validate(PipelineInterfacer pip, Number val) {
    pip.setNumSource(0, val);
    pip.setValve("Valve0", true);
    pip.process();
    assertEquals("Valve did not trigger true properly", val, pip.getOutput("Valve0Output0",
        GenType.NUMBER));
    pip.setValve("Valve0", false);
    pip.process();
    assertFalse("Valve did not trigger false properly", val.equals(pip.getOutput("Valve0Output0",
        GenType.NUMBER)));
  }
}
