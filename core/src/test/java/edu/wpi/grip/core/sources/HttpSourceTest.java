
package edu.wpi.grip.core.sources;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.util.MockExceptionWitness;
import edu.wpi.grip.util.Files;
import edu.wpi.grip.util.GRIPCoreTestModule;

import java.io.File;
import java.io.IOException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class HttpSourceTest {

    private File logoFile;

    private GripServer server;
    private GRIPCoreTestModule coreTestModule;
    private HttpSource source;
    private CloseableHttpClient postClient;

    @Before
    public void setup() throws Exception {
        coreTestModule = new GRIPCoreTestModule();
        coreTestModule.setUp();
        final Injector injector = Guice.createInjector(coreTestModule);

        final EventBus eventBus = new EventBus();
        class UnhandledExceptionWitness {

            @Subscribe
            public void onUnexpectedThrowableEvent(UnexpectedThrowableEvent event) {
                event.handleSafely((throwable, message, isFatal) -> {
                    throwable.printStackTrace();
                });
            }
        }
        eventBus.register(new UnhandledExceptionWitness());

        server = injector.getInstance(GripServer.class);
        server.start();
        source = new HttpSource(origin -> new MockExceptionWitness(eventBus, origin), eventBus, server);

        logoFile = new File(Files.class.getResource("/edu/wpi/grip/images/GRIP_Logo.png").toURI());
        postClient = HttpClients.createDefault();
    }

    @Test
    public void testPostImage() throws IOException, InterruptedException {
        server.addPostHandler(GripServer.IMAGE_UPLOAD_PATH, source);
        OutputSocket<Mat> imageSource = source.getOutputSockets()[0];

        // We have to manually update the output sockets to get the image
        source.updateOutputSockets();
        assertTrue("The value should not be present if the source hasn't been initialized and no image POSTed", imageSource.getValue().get().empty());

        source.initialize(); // adds the source as a PostHandler to the server
        source.updateOutputSockets();
        assertTrue("The value should not be present since the source has been initialized but no image POSTed", imageSource.getValue().get().empty());

        doPost(GripServer.IMAGE_UPLOAD_PATH, logoFile);
        Thread.sleep(100); // wait for the image to be uploaded
        source.updateOutputSockets();
        assertFalse("The value should now be present after POSTing the image", imageSource.getValue().get().empty());
    }

    // POSTs the given image file to the given path on the server
    private void doPost(String path, File imageFile) throws IOException {
        final String uri = "http://localhost:" + server.getPort() + path;
        final HttpPost post = new HttpPost(uri);
        // POST will hang if timeouts aren't given and the port is wrong
        post.setConfig(RequestConfig
                .copy(RequestConfig.DEFAULT)
                .setSocketTimeout(100)
                .setConnectTimeout(100)
                .setConnectionRequestTimeout(100)
                .build());
        final FileEntity entity = new FileEntity(imageFile);
        post.setEntity(entity);
        postClient.execute(post);
    }

    @After
    public void tearDown() throws IOException {
        coreTestModule.tearDown();
        server.stop();
        postClient.close();
    }

}
