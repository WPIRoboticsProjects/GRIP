package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.operations.Operations;
import edu.wpi.grip.core.settings.ProjectSettings;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.generated.CVOperations;
import edu.wpi.grip.util.Files;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;

/**
 * This tests for backwards compatibility by opening a project file containing ALL the steps of GRIP
 * at the time of this file's creation.
 */
public class CompatibilityTest {

    private static final URI testphotoURI = Files.testphotoURI; //The location of the photo source for the test
    private static final URI testprojectURI = Files.testprojectURI; //The location of the save file for the test

    private Connection.Factory<Object> connectionFactory;
    private ImageFileSource.Factory imageSourceFactory;
    private Step.Factory stepFactory;
    private Pipeline pipeline;
    private Project project;
    private ProjectSettings settings;

    private List<Operation> operationList;
    private Optional<Throwable> throwableOptional;
    private EventBus eventBus;

    @Before
    public void setUp() throws Exception {

        //Set up the stuff we need for the core functionality for GRIP
        final Injector injector = Guice.createInjector(new GRIPCoreModule());
        connectionFactory = injector
                .getInstance(Key.get(new TypeLiteral<Connection.Factory<Object>>() {
                }));
        imageSourceFactory = injector
                .getInstance(ImageFileSource.Factory.class);
        eventBus = injector.getInstance(EventBus.class);
        pipeline = injector.getInstance(Pipeline.class);
        project = injector.getInstance(Project.class);
        settings = injector.getInstance(ProjectSettings.class);
        stepFactory = injector.getInstance(Step.Factory.class);

        //Add the operations so that GRIP will recognize them
        Operations.addOperations(eventBus);
        CVOperations.addOperations(eventBus);

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
        //The following alters the project settings in the project file to the defaults given in the "settings" variable
        newtext = newtext.replaceAll("<teamNumber>.*</teamNumber>", "<teamNumber>" + settings.getTeamNumber() + "</teamNumber>");
        newtext = newtext.replaceAll("<publishAddress>.*</publishAddress>", "<publishAddress>" + settings.getPublishAddress() + "</publishAddress>");
        newtext = newtext.replaceAll("<deployAddress>.*</deployAddress>", "<deployAddress>" + settings.getDeployAddress() + "</deployAddress>");
        newtext = newtext.replaceAll("<deployDir>.*</deployDir>", "<deployDir>" + settings.getDeployDir() + "</deployDir>");
        newtext = newtext.replaceAll("<deployUser>.*</deployUser>", "<deployUser>" + settings.getDeployUser() + "</deployUser>");
        newtext = newtext.replaceAll("<deployJavaHome>.*</deployJavaHome>", "<deployJavaHome>" + settings.getDeployJavaHome() + "</deployJavaHome>");

        //Write the altered project file text
        FileWriter writer2 = new FileWriter(file);
        writer2.write(newtext);

        writer2.close();

        //Open the test file as a project
        project.open(file);
    }

    @Test
    public void testFoo() throws Exception {
        assertEquals("The expected number of steps were not found", 50, pipeline.getSteps().size());
        assertEquals("The expected number of sources were not found", 2, pipeline.getSources().size());
    }
}
