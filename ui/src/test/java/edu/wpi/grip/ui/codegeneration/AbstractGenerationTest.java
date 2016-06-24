package edu.wpi.grip.ui.codegeneration;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opencv.core.Mat;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import javax.inject.Inject;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.events.SourceAddedEvent;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.PipelineCreator;
import edu.wpi.grip.ui.codegeneration.tools.PipelineGenerator;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.GRIPCoreTestModule;
import edu.wpi.grip.util.ImageWithData;
@Category(GenerationTest.class)
public abstract class AbstractGenerationTest {
    private GRIPCoreTestModule testModule;
    @Inject
    protected EventBus eventBus;
    @Inject
    protected Pipeline pipeline;
    @Inject
    protected InputSocket.Factory isf;
    @Inject
    protected OutputSocket.Factory osf;
    @Inject
    protected ImageFileSource.Factory imgfac;
    protected PipelineGenerator gen;
    @Before
    public void setUp(){
        testModule = new GRIPCoreTestModule();
        testModule.setUp();
        final Injector injector = Guice.createInjector(testModule);
        injector.injectMembers(this);
        gen = new PipelineGenerator();
        injector.injectMembers(gen);
    }
   
    protected final void test(BooleanSupplier setup, Consumer<PipelineInterfacer> test, String testName){
    	assertTrue("Setup for "+ testName+" reported an issue.", setup.getAsBoolean());
    	String fileName = testName+".java";
    	gen.export(fileName);
    	PipelineInterfacer pip = new PipelineInterfacer(fileName);
    	test.accept(pip);
    }
	@After
	public void tearDown(){
		testModule.tearDown();
	}
	/**
	 * Cleans up generated test java and class files.
	 */
	@AfterClass
	public static void tearDownClass(){
		PipelineCreator.cleanClasses();
	}
	protected ImageFileSource loadImage(ImageWithData img){
		ImageFileSource out = imgfac.create(img.file);
		try{
		out.initialize();
		} catch(IOException e){
			e.printStackTrace();
			fail("IO Exception occurred while loading Image " + img.file.getName());
		}
		eventBus.post(new SourceAddedEvent(out));
		return out;
	}
	protected void assertMatWithin(Mat gen, Mat grip, double tolerance){
		double diff = Math.abs(HelperTools.matAvgDiff(gen, grip));
		assertTrue("Difference between two Mats was: " + diff +
				", which is greater than tolerance of: "+ tolerance,
				diff<=tolerance);
	}
}
