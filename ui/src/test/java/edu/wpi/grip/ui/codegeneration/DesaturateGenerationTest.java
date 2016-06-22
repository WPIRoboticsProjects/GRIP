package edu.wpi.grip.ui.codegeneration;

import java.io.IOException;
import java.util.Optional;

import org.opencv.core.Mat;

import static org.junit.Assert.*;
import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.DesaturateOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.Files;

public class DesaturateGenerationTest extends AbstractGenerationTest {
	public DesaturateGenerationTest(){
		super("Desat.java");
	}
	@Override
	void generatePipeline(){
		Step desat = gen.addStep(new OperationMetaData(DesaturateOperation.DESCRIPTION, () -> new DesaturateOperation(isf,osf)));
		ImageFileSource img = loadImage(Files.gompeiJpegFile);
		OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
		for(InputSocket sock : desat.getInputSockets()){
			if(sock.getSocketHint().getIdentifier().equals("Input")){
				gen.connect(imgOut, sock);
			}
		}
	}

	@Override
	void testPipeline(PipelineInterfacer pip) {
		new ManualPipelineRunner(eventBus, pipeline).runPipeline();
		Optional out = pipeline.getSteps().get(0).getOutputSockets().get(0).getValue();
		assertTrue("Output is not present", out.isPresent());
		assertFalse("Output Mat is empty", ((org.bytedeco.javacpp.opencv_core.Mat)out.get()).empty());
		pip.setMatSource(0, Files.gompeiJpegFile.file);
		pip.process();
		Mat genMat = (Mat) pip.getOutput(0);
		Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat)out.get());
		HelperTools.displayMats(genMat, gripMat);
		assertMatWithin(genMat, gripMat, 10.0);
	}

}
