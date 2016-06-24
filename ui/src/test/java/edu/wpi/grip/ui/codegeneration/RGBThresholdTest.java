package edu.wpi.grip.ui.codegeneration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.opencv.core.Mat;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.RGBThresholdOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.Files;

public class RGBThresholdTest extends AbstractGenerationTest {
	
	boolean setup(){
		Step rgb = gen.addStep(
				new OperationMetaData(RGBThresholdOperation.DESCRIPTION,
						() -> new RGBThresholdOperation(isf, osf)));
		ImageFileSource img = loadImage(Files.gompeiJpegFile);
		OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
		List<Double> rVal = new ArrayList<Double>();
		List<Double> gVal = new ArrayList<Double>();
		List<Double> bVal = new ArrayList<Double>();
		rVal.add(new Double(46));
		rVal.add(new Double(188));
		gVal.add(new Double(0));
		gVal.add(new Double(110));
		bVal.add(new Double(0));
		bVal.add(new Double(110));
		for(InputSocket sock : rgb.getInputSockets()){
			String sockHint = sock.getSocketHint().getIdentifier();
			if(sockHint.equals("Input")){
				gen.connect(imgOut, sock);
			}
			else if(sockHint.equals("Red")){
				sock.setValue(rVal);
			}
			else if(sockHint.equals("Green")){
				sock.setValue(gVal);
			}
			else if(sockHint.equals("Blue")){
				sock.setValue(bVal);
			}
		}
		return true;
	}
	
	void testPipeline(PipelineInterfacer pip){
		ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
		runner.runPipeline();
		Optional out = pipeline.getSteps().get(0).getOutputSockets().get(0).getValue();
		assertTrue("Pipeline did not process", out.isPresent());
		assertFalse("Pipeline output is empty", ((org.bytedeco.javacpp.opencv_core.Mat)out.get()).empty());
		pip.setMatSource(0, Files.gompeiJpegFile.file);
		pip.process();
		Mat genMat = (Mat) pip.getOutput(0);
		Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat)out.get());
		assertMatWithin(genMat, gripMat, 2.0);
	}
}
