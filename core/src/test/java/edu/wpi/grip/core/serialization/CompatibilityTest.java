package edu.wpi.grip.core.serialization;

import com.google.common.base.Throwables;
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
import edu.wpi.grip.core.util.MockExceptionWitness;
import edu.wpi.grip.generated.CVOperations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;

public class CompatibilityTest {

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

        //Set up stuff we need to add the operations
        this.operationList = new ArrayList<>();
        this.throwableOptional = Optional.empty();
        this.eventBus = new EventBus((exception, context) -> throwableOptional = Optional.of(exception));

        //Set up the operation grabber to help us register the operations
        this.eventBus.register(new OperationGrabber());

        //Set up the test project file to work with this machine
        URL location = CompatibilityTest.class.getProtectionDomain().getCodeSource().getLocation();

        int numbOfFolders = Paths.get(location.toURI()).getNameCount();


        String fileName = "/"+ Paths.get(location.toURI()).subpath(0,(numbOfFolders-4)).toString()+ "/testALL.grip";
        String photoFileName = "/" + Paths.get(location.toURI()).subpath(0,(numbOfFolders-4)).toString()+ "/testphoto.png";

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

        //Add/register/create the operations
        CVOperations.addOperations(eventBus);
        for (Operation operation : operationList) {
            new Step.Factory(eventBus, (origin) -> new MockExceptionWitness(eventBus, origin)).create(operation);
        }

        Operations.addOperations(eventBus);
        for (Operation operation : operationList) {
            new Step.Factory(eventBus, (origin) -> new MockExceptionWitness(eventBus, origin)).create(operation);
        }

        //Open the test file as a project
        project.open(file);
    }

    @After
    public void afterTest() {
        //Throw any exceptions that weren't handled during the test
        if (throwableOptional.isPresent()) {
            throw Throwables.propagate(throwableOptional.get());
        }
    }

    @Test
    public void testFoo() throws Exception {

        assertEquals("blarg",
                3, 3);

    }
}
