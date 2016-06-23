package edu.wpi.grip.ui.codegeneration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.Test;
import org.opencv.core.Mat;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.HSVThresholdOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.Files;

public class HSVThresholdTest extends AbstractGenerationTest {

	void setup(){
		ArrayList<Number> hVal = new ArrayList<Number>();
		ArrayList<Number> sVal = new ArrayList<Number>();
		ArrayList<Number> vVal = new ArrayList<Number>();
		hVal.add(new Double(50.0));
		hVal.add(new Double(180.0));
		sVal.add(new Double(0.0));
		sVal.add(new Double(255.0));
		vVal.add(new Double(0.0));
		vVal.add(new Double(255.0));
		Step hsv = gen.addStep(new OperationMetaData(
				HSVThresholdOperation.DESCRIPTION, 
				() -> new HSVThresholdOperation(isf, osf)
				));
		ImageFileSource img = loadImage(Files.imageFile);
		OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
		for(InputSocket sock : hsv.getInputSockets()){
			if(sock.getSocketHint().getIdentifier().equals("Input")){
				gen.connect(imgOut, sock);
			}
			else if(sock.getSocketHint().getIdentifier().equals("Hue")){
				sock.setValue(hVal);
			}
			else if(sock.getSocketHint().getIdentifier().equals("Saturation")){
				sock.setValue(sVal);
			}
			else if(sock.getSocketHint().getIdentifier().equals("Value")){
				sock.setValue(vVal);
			}
		}
	}
	
	@Test
	public void HSVTest(){
		test( () -> {setup(); return true;}, (pip) -> validate(pip), "HSVThreshold");
	}
	
	void validate(PipelineInterfacer pip){
		new ManualPipelineRunner(eventBus, pipeline).runPipeline();
		Optional out = pipeline.getSteps().get(0).getOutputSockets().get(0).getValue();
		assertTrue("Pipeline did not process", out.isPresent());
		assertFalse("Pipeline output is empty", ((org.bytedeco.javacpp.opencv_core.Mat)out.get()).empty());
		pip.setMatSource(0, Files.imageFile.file);
		pip.process();
		Mat genMat = (Mat) pip.getOutput(0);
		Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat)out.get());
		assertMatWithin(genMat, gripMat, 0.5);
	}
}
