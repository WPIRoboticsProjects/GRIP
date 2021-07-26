package edu.wpi.grip.core.http;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.exception.GripServerException;
import edu.wpi.grip.core.settings.SettingsProvider;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class GripServerTest {

  private final DefaultHttpClient client;
  private final GripServer instance;

  public static class TestServerFactory implements GripServer.JettyServerFactory {

    @Override
    public Server create(int port) {
      org.eclipse.jetty.util.log.Log.setLog(new NoLogger());
      return new Server(0); // 0 -> some random open port, we don't care which
    }

  }

  /**
   * Public factory method for testing.
   */
  public static GripServer makeServer(ContextStore store,
                                      GripServer.JettyServerFactory factory,
                                      SettingsProvider settingsProvider) {
    return new GripServer(store, factory, settingsProvider);
  }

  public GripServerTest() {
    instance = new GripServer(new ContextStore(), new TestServerFactory(), new Pipeline());
    instance.start();

    client = new DefaultHttpClient();
    client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
  }

  @Test
  public void testAddRemoveHandler() throws IOException {
    String path = "/testAddRemoveHandler";
    boolean[] didRun = {false};
    Handler h = new TestHandler(path, () -> {
      didRun[0] = true;
    });
    instance.addHandler(h);
    HttpResponse response = doPost(path, path.getBytes(StandardCharsets.UTF_8));
    EntityUtils.consume(response.getEntity());
    assertTrue("Handler should have run", didRun[0]);
    didRun[0] = false;
    instance.removeHandler(h);
    response = doPost(path, path.getBytes(StandardCharsets.UTF_8));
    EntityUtils.consume(response.getEntity());
    assertFalse("Handler should not have run", didRun[0]);
  }

  @Test
  public void testSuccessfulHandler() throws IOException {
    String path = "/testSuccessfulHandler";
    boolean[] didRun = {false};
    Handler h = new TestHandler(path, () -> {
      didRun[0] = true;
    });
    instance.addHandler(h);
    doPost(path, path.getBytes(StandardCharsets.UTF_8));
    assertTrue("Handler should have run on " + path, didRun[0]);
  }

  @Test
  public void testUnsuccessfulPostHandler() throws Exception {
    String path = "/testUnsuccessfulPostHandler";
    boolean[] didRun = {false};
    Handler h = new TestHandler(path, () -> {
      didRun[0] = true;
      throw new GripServerException("Expected");
    });
    instance.addHandler(h);
    HttpResponse response = doPost(path, path.getBytes(StandardCharsets.UTF_8));
    assertEquals("Server should return an internal error (500)",
        500,
        response.getStatusLine().getStatusCode());
    assertTrue("Handler should have run", didRun[0]);
  }

  @Test
  @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
  public void testStartStop() throws GripServerException {
    instance.start(); // should do nothing since the server's already running
    instance.stop();  // stop the server so we know we can start it
    instance.stop();  // second call should do nothing
    instance.start(); // restart the server
    instance.start(); // second call should do nothing
    instance.restart(); // should stop and then start again
    // No asserts or fails -- if something goes wrong, it would have thrown an exception
  }

  @After
  public void tearDown() {
    if (instance.getState() == GripServer.State.RUNNING) {
      instance.stop();
    }
  }

  private HttpResponse doPost(String path, byte[] bytes) throws IOException {
    HttpPost post = new HttpPost("http://localhost:" + instance.getPort() + path);
    BasicHttpEntity httpEntity = new BasicHttpEntity();
    httpEntity.setContent(new ByteArrayInputStream(bytes));
    post.setEntity(httpEntity);
    return client.execute(post);
  }

  private static final class TestHandler extends PedanticHandler {

    private final Runnable runner;

    private TestHandler(String path, Runnable runner) {
      super(new ContextStore(), path);
      this.runner = runner;
    }

    @Override
    protected void handleIfPassed(String target,
                                  Request baseRequest,
                                  HttpServletRequest request,
                                  HttpServletResponse response)
        throws IOException, ServletException {
      runner.run();
      baseRequest.setHandled(true);
    }
  }

}
