package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.ValveOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import javax.inject.Inject;
import org.junit.Test;
public class ValveTest extends AbstractGenerationTest {
	@Inject
	private Exporter exporter;
	private Step valve;
	boolean setup(Object value){
		valve = gen.addStep(new OperationMetaData(ValveOperation.DESCRIPTION, 
				() -> new ValveOperation(isf, osf)));
		for(InputSocket sock: valve.getInputSockets()){
			String socketHint = sock.getSocketHint().getIdentifier();
			if(socketHint.equalsIgnoreCase("Input")){
				sock.setValue(value);
			}
		}
		exporter.export(pipeline, Language.JAVA, new File("../../Valve.java"), true);
		return true;
	}
	@Test
	public void valveNumTest(){
		Double value = Math.PI;
		test( () -> setup(value), (pip) -> validate(pip,value), "ValveObjTest");
	}
	private void validate(PipelineInterfacer pip, Object val){
		pip.setValve(0, true);
		pip.process();
		assertEquals("Valve did not trigger true properly", val, pip.getOutput(0));
		pip.setValve(0, false);
		pip.process();
		assertEquals("Valve did not trigger false properly", null, pip.getOutput(0));
	}
}
