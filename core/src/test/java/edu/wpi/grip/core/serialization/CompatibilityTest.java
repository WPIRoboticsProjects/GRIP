package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.operations.OperationsFactory;
import edu.wpi.grip.util.Files;
import edu.wpi.grip.util.GRIPCoreTestModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * This tests for backwards compatibility by opening a project file containing ALL the steps of GRIP
 * at the time of this file's creation. Please note that this test requires a webcam at position 0.
 */
@Ignore("OpenCV operations were removed. Remove this annotation after they are added back.")
public class CompatibilityTest {

    private static final URI testphotoURI = Files.testphotoURI; //The location of the photo source for the test
    private static final URI testprojectURI = Files.testprojectURI; //The location of the save file for the test

    private GRIPCoreTestModule testModule;
    private Pipeline pipeline;

    @Before
    public void setUp() throws Exception {
        testModule = new GRIPCoreTestModule();
        testModule.setUp();
        //Set up the stuff we need for the core functionality for GRIP
        final Injector injector = Guice.createInjector(testModule);

        final EventBus eventBus = injector.getInstance(EventBus.class);
        pipeline = injector.getInstance(Pipeline.class);
        final Project project = injector.getInstance(Project.class);

        //Add the operations so that GRIP will recognize them
        OperationsFactory.create(eventBus).addOperations();
//        CVOperations.addOperations(eventBus);

        //Set up the test project file to work with this machine
        String fileName = testprojectURI.toString().substring(5);
        String photoFileName = testphotoURI.toString().substring(5);

        //Open the project save file and read it into a string so that we can alter it
        File file = new File(fileName);

        Reader temp = new FileReader(file);
        BufferedReader reader = new BufferedReader(temp);
        String line = "", oldtext = "";
        while ((line = reader.readLine()) != null) {
            oldtext += line + "\r\n";
        }
        reader.close();
        String newtext = oldtext.replaceAll("REPLACEME", photoFileName);//This gives the correct location of the test photo needed to the project file

        //Write the altered project file text
        FileWriter writer2 = new FileWriter(file);
        writer2.write(newtext);

        writer2.close();

        //Open the test file as a project
        project.open(file);
    }

    @After
    public void tearDown() {
        testModule.tearDown();
    }


    @Test
    public void testCompatibilityIsPreserved() throws Exception {
        assertEquals("The expected number of steps were not found", 50, pipeline.getSteps().size());
        assertEquals("The expected number of sources were not found", 2, pipeline.getSources().size());
        pipeline.clear();
    }
}
