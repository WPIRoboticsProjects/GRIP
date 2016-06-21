package edu.wpi.grip.ui.codegeneration;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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
import edu.wpi.grip.core.PipelineRunner;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.SourceAddedEvent;
import edu.wpi.grip.core.operations.Operations;
import edu.wpi.grip.core.operations.OperationsFactory;
import edu.wpi.grip.core.operations.composite.BlurOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineGenerator;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.GRIPCoreTestModule;
import edu.wpi.grip.core.sockets.SocketHints.Outputs;
import edu.wpi.grip.util.Files;
@Category(GenerationTest.class)
public class BlurGenerator {
    private GRIPCoreTestModule testModule;
    @Inject
    private EventBus eventBus;
    @Inject
    private Pipeline pipeline;
    @Inject
    private InputSocket.Factory isf;
    @Inject
    private OutputSocket.Factory osf;
    @Inject
    private ImageFileSource.Factory imgfac;
    private PipelineGenerator gen;
    @Before
    public void setUp(){
        testModule = new GRIPCoreTestModule();
        testModule.setUp();
        final Injector injector = Guice.createInjector(testModule);
        injector.injectMembers(this);
        gen = new PipelineGenerator();
        injector.injectMembers(gen);
    }
	@Test
	public void generateAndRun() throws IOException{
		Step step = gen.addStep(new OperationMetaData(BlurOperation.DESCRIPTION, () -> new BlurOperation(isf,osf)));
		ImageFileSource img = imgfac.create(Files.gompeiJpegFile.file);
		img.initialize();
		eventBus.post(new SourceAddedEvent(img));
		OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
		
		for(InputSocket sock : step.getInputSockets()){
			if(sock.getSocketHint().isCompatibleWith(imgOut.getSocketHint())){
				gen.connect(imgOut, sock);
			}
			else if(sock.getSocketHint().isCompatibleWith(Outputs.createNumberSocketHint("Number", new Double(5.1)))){
				sock.setValue(new Double(10.0));
			}
		}
		assertEquals("Connection was not added",1,pipeline.getConnections().size());
		gen.export("Blur.java");
		PipelineInterfacer pip = new PipelineInterfacer("Blur.java");
		ManualPipelineRunner runner = new ManualPipelineRunner(eventBus, pipeline);
		runner.runPipeline();
		Optional out = step.getOutputSockets().get(0).getValue();
		assertTrue("Pipeline did not process", out.isPresent());
		assertFalse("Pipeline has null output", out.get()==null);
		assertFalse("Pipeline output is empty", ((org.bytedeco.javacpp.opencv_core.Mat)out.get()).empty());
		pip.setMatSource(0, Files.gompeiJpegFile.file);
		pip.process();
		Mat genMat = (Mat) pip.getOutput(0);
		Mat gripMat = HelperTools.bytedecoMatToCVMat((org.bytedeco.javacpp.opencv_core.Mat)out.get());
		HelperTools.displayMats(genMat, gripMat);
		assertTrue("Mats are not similar", HelperTools.equalMatCheck(genMat, gripMat, 10.0));
	}
	@After
	public void tearDown(){
		testModule.tearDown();
	}
}
