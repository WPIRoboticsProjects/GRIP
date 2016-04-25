package edu.wpi.grip.core.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PedanticHandlerTest {

    private HandlerCollection handlers;
    private MockHandler handler1, handler2, handler3;
    private Server server;
    private DefaultHttpClient client;

    @Before
    public void setUp() throws Exception {
        client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        handlers = new HandlerCollection(true);
        server = new Server(0);
        server.setHandler(handlers);
        server.start();
    }

    @Test
    public void testNoClaims() throws IOException, ServletException {
        // Handlers 1 and 2 on one path, handler 3 on another
        handler1 = new MockHandler("/path/1");
        handler2 = new MockHandler("/path/1");
        handler3 = new MockHandler("/path/3");
        handlers.setHandlers(arr(handler1, handler2, handler3));

        sendHttpRequest("/path/1");
        assertTrue("Handler 1 should have run", handler1.didRun);
        assertTrue("Handler 2 should have run", handler2.didRun);
        assertFalse("Handler 3 should not have run", handler3.didRun);
    }

    @Test
    public void testWithClaims() throws IOException {
        // all handlers on separate paths
        handler1 = new MockHandler("/path/1", true);
        handler2 = new MockHandler("/path/2", true);
        handler3 = new MockHandler("/path/3", true);
        handlers.setHandlers(arr(handler1, handler2, handler3));

        sendHttpRequest("/path/1");
        assertTrue("Handler 1 should have run", handler1.didRun);
        assertFalse("Handler 2 should not have run", handler2.didRun);
        assertFalse("Handler 3 should not have run", handler3.didRun);
    }

    @Test
    public void testPedantry() throws IOException {
        handler1 = new MockHandler("/path");
        handler2 = new MockHandler("/path/");
        handler3 = new MockHandler("/path/3");
        handlers.setHandlers(arr(handler1, handler2, handler3));
        sendHttpRequest("/path");
        assertTrue("Handler 1 should have run", handler1.didRun);
        assertFalse("Handler 2 should not have run", handler2.didRun);
        assertFalse("Handler 3 should not have run", handler3.didRun);
    }

    @After
    public void tearDown() throws Exception {
        handler1.releaseContext();
        handler2.releaseContext();
        handler3.releaseContext();
        server.stop();
    }

    private <T> T[] arr(T... a) {
        return a;
    }

    private void sendHttpRequest(String path) throws IOException {
        HttpPost post = new HttpPost("http://localhost:" + getServerPort() + path);
        BasicHttpEntity httpEntity = new BasicHttpEntity();
        httpEntity.setContent(new ByteArrayInputStream("http_request_bytes".getBytes()));
        post.setEntity(httpEntity);
        CloseableHttpResponse resp = client.execute(post);
        EntityUtils.consume(resp.getEntity());
    }

    private int getServerPort() {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    private class MockHandler extends PedanticHandler {

        private boolean didRun = false;

        MockHandler(String context) {
            super(context);
        }

        MockHandler(String context, boolean doClaim) {
            super(context, doClaim);
        }

        @Override
        protected void handleIfPassed(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            didRun = true;
        }
    }

}
