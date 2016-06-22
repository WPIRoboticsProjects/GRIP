package edu.wpi.grip.ui.codegeneration;

import java.util.ArrayList;
import java.util.List;

import com.google.common.eventbus.EventBus;

import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.HSLThresholdOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.ui.codegeneration.tools.PipelineGenerator;
import edu.wpi.grip.util.Files;

public class GripIconHSLSetup {
	public static void setup(AbstractGenerationTest caller){
		List<Number> hVal = new ArrayList<Number>();
		List<Number> sVal = new ArrayList<Number>();
		List<Number> lVal = new ArrayList<Number>();
		hVal.add(new Double(0.0));
		hVal.add(new Double(49.0));
		sVal.add(new Double(0.0));
		sVal.add(new Double(41.0));
		lVal.add(new Double(0.0));
		lVal.add(new Double(67.0));
		Step hsl = caller.gen.addStep(new OperationMetaData(HSLThresholdOperation.DESCRIPTION, 
				() -> new HSLThresholdOperation(caller.isf,caller.osf)));
		ImageFileSource img = caller.loadImage(Files.imageFile);
		OutputSocket imgOut = caller.pipeline.getSources().get(0).getOutputSockets().get(0);
		for(InputSocket sock : hsl.getInputSockets()){
			if(sock.getSocketHint().getIdentifier().equals("Input")){
				caller.gen.connect(imgOut, sock);
			}
			else if(sock.getSocketHint().getIdentifier().equals("Hue")){
				sock.setValue(hVal);
			}
			else if(sock.getSocketHint().getIdentifier().equals("Saturation")){
				sock.setValue(sVal);
			}
			else if(sock.getSocketHint().getIdentifier().equals("Luminance")){
				sock.setValue(lVal);
			}
		}
	}
}
