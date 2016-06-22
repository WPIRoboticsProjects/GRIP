package edu.wpi.grip.ui.codegeneration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.opencv.core.Mat;

import edu.wpi.grip.core.ManualPipelineRunner;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.operations.composite.HSLThresholdOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.Files;

public class HSLThresholdGenerationTest extends AbstractGenerationTest {
	//H 0-49
	//S 0-41
	//L 0-67
	private List<Number> hVal = new ArrayList<Number>();
	private List<Number> sVal = new ArrayList<Number>();
	private List<Number> lVal = new ArrayList<Number>();
	
	public HSLThresholdGenerationTest() {
		hVal.add(new Double(0.0));
		hVal.add(new Double(49.0));
		sVal.add(new Double(0.0));
		sVal.add(new Double(41.0));
		lVal.add(new Double(0.0));
		lVal.add(new Double(67.0));
	}

	@Test
	public void testHSL() {
		test(() ->{
			GripIconHSLSetup.setup(this);
			return true;//never can fail
		}, (pip) ->{
		new ManualPipelineRunner(eventBus, pipeline).runPipeline();
		Optional out = pipeline.getSteps().get(0).getOutputSockets().get(0).getValue();
		assertTrue("Output is not present", out.isPresent());
		assertFalse("Output Mat is empty", ((org.bytedeco.javacpp.opencv_core.Mat)out.get()).empty());
		pip.setMatSource(0, Files.imageFile.file);
		pip.process();
		Mat genMat = (Mat) pip.getOutput(0);
		Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat)out.get());
		assertMatWithin(genMat, gripMat, 10.0);
		}, "HSLTest");
	}

}
