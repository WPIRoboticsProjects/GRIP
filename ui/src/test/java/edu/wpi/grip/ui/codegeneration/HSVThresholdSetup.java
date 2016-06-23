package edu.wpi.grip.ui.codegeneration;

import java.util.ArrayList;

import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.HSVThresholdOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.util.Files;

public class HSVThresholdSetup {
	static void setup(AbstractGenerationTest caller){
		ArrayList<Number> hVal = new ArrayList<Number>();
		ArrayList<Number> sVal = new ArrayList<Number>();
		ArrayList<Number> vVal = new ArrayList<Number>();
		hVal.add(new Double(50.0));
		hVal.add(new Double(180.0));
		sVal.add(new Double(0.0));
		sVal.add(new Double(255.0));
		vVal.add(new Double(0.0));
		vVal.add(new Double(255.0));
		Step hsv = caller.gen.addStep(new OperationMetaData(
				HSVThresholdOperation.DESCRIPTION, 
				() -> new HSVThresholdOperation(caller.isf, caller.osf)
				));
		ImageFileSource img = caller.loadImage(Files.imageFile);
		OutputSocket imgOut = caller.pipeline.getSources().get(0).getOutputSockets().get(0);
		for(InputSocket sock : hsv.getInputSockets()){
			if(sock.getSocketHint().getIdentifier().equals("Input")){
				caller.gen.connect(imgOut, sock);
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
}
