package edu.wpi.grip.ui.codegeneration;

import java.io.File;
import java.nio.file.Paths;
import javax.inject.Inject;
import com.google.common.eventbus.EventBus;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.util.ExceptionWitness;
import edu.wpi.grip.core.util.MockExceptionWitness;
public class PipelineGenerator {
	@Inject
	private Pipeline pipeline;
	@Inject
	private Exporter exporter;
	@Inject
	private EventBus eventBus;
	@Inject
	private Connection.Factory<Object> factory;
	public PipelineGenerator(){
		
	}
	final File codeDir = Paths.get("ui", "src","test","java","edu","wpi","grip","ui","codegeneration").toFile();
	
	public Step addStep(OperationMetaData data){
		Step step = new Step.Factory(MockExceptionWitness.MOCK_FACTORY).create(data);
		pipeline.addStep(step);
		return step;
	}
	
	public <T> void connect (OutputSocket<? extends T> out, InputSocket<T> inp ){
		eventBus.post(new ConnectionAddedEvent(factory.create(out, (InputSocket<Object>) inp)));
	}
	
	public void export(String fileName){
		exporter.export(pipeline, Language.JAVA, codeDir.toPath().resolve(fileName).toFile(), false);
	}

}
