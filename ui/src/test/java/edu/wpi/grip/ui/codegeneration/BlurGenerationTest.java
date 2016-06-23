package edu.wpi.grip.ui.codegeneration;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javax.inject.Inject;

import org.opencv.core.Mat;

import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.SourceAddedEvent;
import edu.wpi.grip.core.operations.composite.BlurOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineGenerator;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.core.sockets.SocketHints.Outputs;
import edu.wpi.grip.util.Files;
@Category(GenerationTest.class)
public class BlurGenerationTest extends AbstractGenerationTest{
    private Double blurRatio = new Double(10.0);

	void generatePipeline(String blurType) {
		Step step = gen.addStep(new OperationMetaData(BlurOperation.DESCRIPTION, () -> new BlurOperation(isf,osf)));
		ImageFileSource img = loadImage(Files.gompeiJpegFile);
		OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
		
		for(InputSocket sock : step.getInputSockets()){
			if(sock.getSocketHint().isCompatibleWith(imgOut.getSocketHint())){
				gen.connect(imgOut, sock);
			}
			else if(sock.getSocketHint().isCompatibleWith(Outputs.createNumberSocketHint("Number", blurRatio))){
				sock.setValue(blurRatio);
			}
			else if(sock.getSocketHint().getIdentifier().equals("Type")){
				HelperTools.setEnumSocket(sock, blurType);
			}
		}
	}
	@Test
	public void boxBlurTest(){
		test( () -> {generatePipeline("Box Blur"); return true;},
				(pip) -> testPipeline(pip),"BoxBlur");
	}
	@Test
	public void gaussianBlurTest(){
		test( ()-> {generatePipeline("Gaussian Blur"); return true;},
				(pip) -> testPipeline(pip), "GaussianBlur"
		);
	}
	@Test
	public void medianFilterTest(){
		test( () -> {generatePipeline("Median Filter"); return true;},
				(pip) -> testPipeline(pip), "MedianFilter");
	}
	@Test
	public void bilateralFilterTest(){
		test( () -> {generatePipeline("Bilateral Filter"); return true;},
				(pip) -> testPipeline(pip), "BilateralFilter");
	}
	void testPipeline(PipelineInterfacer pip) {
		ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
		runner.runPipeline();
		Optional out = pipeline.getSteps().get(0).getOutputSockets().get(0).getValue();
		assertTrue("Pipeline did not process", out.isPresent());
		assertFalse("Pipeline output is empty", ((org.bytedeco.javacpp.opencv_core.Mat)out.get()).empty());
		pip.setMatSource(0, Files.gompeiJpegFile.file);
		pip.process();
		Mat genMat = (Mat) pip.getOutput(0);
		Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat)out.get());
		assertMatWithin(genMat, gripMat, 10.0);
	}
}
