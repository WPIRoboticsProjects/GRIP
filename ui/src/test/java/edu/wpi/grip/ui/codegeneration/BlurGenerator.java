package edu.wpi.grip.ui.codegeneration;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.SourceAddedEvent;
import edu.wpi.grip.core.operations.Operations;
import edu.wpi.grip.core.operations.OperationsFactory;
import edu.wpi.grip.core.operations.composite.BlurOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.util.GRIPCoreTestModule;

public class BlurGenerator {
    private GRIPCoreTestModule testModule;
    private Step.Factory stepFactory;
    private EventBus eventBus;
    private Pipeline pipeline;
    private InputSocket.Factory isf;
    private OutputSocket.Factory osf;
    private Connection.Factory<Object> connFact;
    ImageFileSource.Factory imgfac;
    @Before
    public void setUp(){
        testModule = new GRIPCoreTestModule();
        testModule.setUp();
        System.out.println("About to configure CoreTestModule");
        final Injector injector = Guice.createInjector(testModule);
        stepFactory = injector.getInstance(Step.Factory.class);
        eventBus = injector.getInstance(EventBus.class);
        pipeline = injector.getInstance(Pipeline.class);
        isf = injector.getInstance(InputSocket.Factory.class);
        osf = injector.getInstance(OutputSocket.Factory.class);
        connFact = injector.getInstance(Connection.Factory.class);
        imgfac = injector.getInstance(ImageFileSource.Factory.class);
    }
	@Test
	public void GenerateAndRun() {
		PipelineGenerator gen = new PipelineGenerator(pipeline, eventBus, connFact);
		Step step = gen.addStep(new OperationMetaData(BlurOperation.DESCRIPTION, () -> new BlurOperation(isf,osf)));	
		eventBus.post(new SourceAddedEvent(imgfac.create(new File("src/test/resources/edu/wpi/grip/images/gompei.jpeg"))));
		OutputSocket imgOut = pipeline.getSources().get(0).getOutputSockets().get(0);
		for(InputSocket sock : step.getInputSockets()){
			if(sock.getSocketHint().isCompatibleWith(imgOut.getSocketHint())){
				gen.connect(imgOut, sock);
			}
		}
		assertEquals("Connection was not added",1,pipeline.getConnections().size());
	}
	@After
	public void tearDown(){
		testModule.tearDown();
	}
}
