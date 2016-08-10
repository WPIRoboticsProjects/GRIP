package edu.wpi.grip.core.http;

import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;

public class NoContextHandlerTest {

  private final ContextStore contextStore = new ContextStore();
  private final HandlerCollection handlers = new HandlerCollection();
  private final NoContextHandler handler = new NoContextHandler(contextStore);
  private Server server;
  private DefaultHttpClient client;

  @Before
  public void setUp() throws Exception {
    client = new DefaultHttpClient();
    client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
    handlers.addHandler(handler);
    handlers.addHandler(new ClaimingHandler("/someClaimedPath", true));
    server = new Server(0);
    server.setHandler(handlers);
    server.start();
  }

  @Test
  public void testNoContext() throws IOException {
    CloseableHttpResponse response;
    response = sendHttpRequest("/someUnclaimedPath");
    assertEquals("NoContextHandler should have run and sent 404 status",
        404,
        response.getStatusLine().getStatusCode());
    EntityUtils.consume(response.getEntity());

    response = sendHttpRequest("/someClaimedPath");
    assertEquals("ClaimingHandler should have run and sent 200 status",
        200,
        response.getStatusLine().getStatusCode());
    EntityUtils.consume(response.getEntity());
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
    for (Handler h : handlers.getHandlers()) {
      ((GenericHandler) h).releaseContext();
    }
  }

  private CloseableHttpResponse sendHttpRequest(String path) throws IOException {
    HttpPost post = new HttpPost("http://localhost:" + getServerPort() + path);
    BasicHttpEntity httpEntity = new BasicHttpEntity();
    httpEntity.setContent(
        new ByteArrayInputStream("http_request_bytes".getBytes(StandardCharsets.UTF_8)));
    post.setEntity(httpEntity);
    return client.execute(post);
  }

  private int getServerPort() {
    return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
  }

  private class ClaimingHandler extends PedanticHandler {

    private ClaimingHandler(String context, boolean doClaim) {
      super(contextStore, context, doClaim);
    }

    @Override
    protected void handleIfPassed(String target,
                                  Request baseRequest,
                                  HttpServletRequest request,
                                  HttpServletResponse response)
        throws IOException, ServletException {
      baseRequest.setHandled(true);
      response.setStatus(HttpServletResponse.SC_OK);
    }
  }
}
