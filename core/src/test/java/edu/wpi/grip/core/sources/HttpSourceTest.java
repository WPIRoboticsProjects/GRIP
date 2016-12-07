package edu.wpi.grip.core.sources;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.http.ContextStore;
import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.http.GripServerTest;
import edu.wpi.grip.core.sockets.MockOutputSocketFactory;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.util.MockExceptionWitness;
import edu.wpi.grip.util.Files;

import com.google.common.eventbus.EventBus;

import org.apache.commons.httpclient.URIException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class HttpSourceTest {

  private File logoFile;

  private GripServer server;
  private HttpSource source;
  private CloseableHttpClient postClient;

  @Before
  public void setUp() throws URIException, URISyntaxException {
    GripServer.JettyServerFactory f = new GripServerTest.TestServerFactory();
    ContextStore contextStore = new ContextStore();
    server = GripServerTest.makeServer(contextStore, f, new Pipeline());
    server.start();
    EventBus eventBus = new EventBus();
    OutputSocket.Factory osf = new MockOutputSocketFactory(eventBus);
    source = new HttpSource(
        origin -> new MockExceptionWitness(eventBus, origin),
        eventBus,
        osf,
        server,
        contextStore,
        GripServer.IMAGE_UPLOAD_PATH);

    logoFile = new File(Files.class.getResource("/edu/wpi/grip/images/GRIP_Logo.png").toURI());
    postClient = HttpClients.createDefault();
  }

  @Test
  public void testPostImage() throws IOException, InterruptedException {
    OutputSocket<Mat> imageSource = source.getOutputSockets().get(0);

    // We have to manually update the output sockets to get the image
    source.updateOutputSockets();
    assertTrue(
        "The value should not be present if the source hasn't been initialized and no image POSTed",
        imageSource.getValue().get().empty());

    source.initialize(); // adds the source as a PostHandler to the server
    source.updateOutputSockets();
    assertTrue(
        "The value should not be present since the source has been initialized but no image POSTed",
        imageSource.getValue().get().empty());

    doPost(GripServer.IMAGE_UPLOAD_PATH, logoFile);
    source.updateOutputSockets();
    assertFalse(
        "The value should now be present after POSTing the image",
        imageSource.getValue().get().empty());
  }

  // POSTs the given image file to the given path on the server
  private void doPost(String path, File imageFile) throws IOException {
    final String uri = "http://localhost:" + server.getPort() + path;
    final HttpPost post = new HttpPost(uri);
    final FileEntity entity = new FileEntity(imageFile);
    post.setEntity(entity);
    CloseableHttpResponse response = postClient.execute(post);
    EntityUtils.consume(response.getEntity());
  }

  @After
  public void tearDown() throws IOException {
    server.stop();
    postClient.close();
  }

}
