package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.operations.Operations;
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

public class CompatibilityTest {

    private static final URI testphotoURI = Files.testphotoURI;
    private static final URI testprojectURI = Files.testprojectURI;

    private Connection.Factory<Object> connectionFactory;
    private ImageFileSource.Factory imageSourceFactory;
    private Step.Factory stepFactory;
    private Pipeline pipeline;
    private Project project;


    private List<Operation> operationList;
    private Optional<Throwable> throwableOptional;
    private EventBus eventBus;

    private class OperationGrabber {
        @Subscribe
        public void onOperationAddedEvent(OperationAddedEvent event) {
            operationList.add(event.getOperation());
        }
    }

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
        stepFactory = injector.getInstance(Step.Factory.class);

        Operations.addOperations(eventBus);
        CVOperations.addOperations(eventBus);

        //Set up the test project file to work with this machine
        String fileName = testprojectURI.toString().substring(5);
        String photoFileName = testphotoURI.toString().substring(5);

        File file = new File(fileName);

        Reader temp = new FileReader(file);
        BufferedReader reader = new BufferedReader(temp);
        String line = "", oldtext = "";
        while ((line = reader.readLine()) != null) {
            oldtext += line + "\r\n";
        }
        reader.close();
        String newtext = oldtext.replaceAll("REPLACEME", photoFileName);

        //TODO: replace "preferences" section to work with this machine
        FileWriter writer2 = new FileWriter(file);
        writer2.write(newtext);
        writer2.close();

        //Open the test file as a project
        project.open(file);
    }

    @Test
    public void testFoo() throws Exception {

        assertEquals("blarg", 3, 3);

    }
}
