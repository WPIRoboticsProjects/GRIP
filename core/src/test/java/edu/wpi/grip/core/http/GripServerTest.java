
package edu.wpi.grip.core.http;

import com.sun.net.httpserver.HttpServer;

import edu.wpi.grip.core.MockPipeline;
import edu.wpi.grip.core.MockPipeline.MockProjectSettings;
import edu.wpi.grip.core.exception.GripException;
import edu.wpi.grip.core.http.GripServer.HttpServerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.function.Supplier;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class GripServerTest {

    private static final int GRIP_SERVER_TEST_PORT = 8080;
    private final DefaultHttpClient client;
    private final TestServerFactory serverFactory;
    private GripServer instance;

    public static class TestServerFactory implements HttpServerFactory {

        private int port;

        public int getPort() {
            return port;
        }

        @Override
        public HttpServer create(int port) {
            final int MAX_TRIES = 200;
            IOException lastException = null;
            for (int offset = 0; offset < MAX_TRIES; offset++) {
                try {
                    this.port = port + offset;
                    return HttpServer.create(new InetSocketAddress("localhost", this.port), 1);
                } catch (IOException e) {
                    // That port is taken -- keep trying different ports
                    lastException = e;
                }
            }
            throw new AssertionError(
                    String.format("Could not create a server on port %d after %d attempts", port, MAX_TRIES),
                    lastException);
        }

    }

    public GripServerTest() {
        MockProjectSettings mockSettings = new MockProjectSettings();
        mockSettings.setServerPort(GRIP_SERVER_TEST_PORT);
        this.serverFactory = new TestServerFactory();
        instance = new GripServer(serverFactory, new MockPipeline(mockSettings));
        instance.start();

        client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
    }

    /**
     * Test of GetHandler methods
     */
    @Test
    public void testGetHandlers() throws IOException {
        String path = "/testGetHandlers";
        GetHandler handler = params -> path;
        instance.addGetHandler(path, handler);
        instance.addDataSupplier(path, () -> path);
        assertEquals(doGet(path), path);
    }

    @Test
    public void testAddAndRemovePostHandler() throws IOException {
        final String path = "/testAddPostHandler";
        final byte[] testBytes = "testAddPostHandler".getBytes();
        final boolean[] didHandle = {false};
        PostHandler handler = bytes -> {
            didHandle[0] = true;
            return Arrays.equals(bytes, testBytes);
        };

        instance.addPostHandler(path, handler);
        assertTrue("The server should have removed the handler", instance.removePostHandler(handler));
        doPost(path, testBytes);
        assertFalse("Handler should not have run", didHandle[0]);
    }

    @Test
    public void testSuccessfulPostHandler() throws IOException {
        final String path = "/testAddPostHandler";
        final byte[] testBytes = "testAddPostHandler".getBytes();
        final boolean[] didHandle = {false};
        PostHandler handler = bytes -> {
            didHandle[0] = true;
            return Arrays.equals(bytes, testBytes);
        };

        instance.addPostHandler(path, handler);
        HttpResponse response = doPost(path, testBytes);
        assertEquals("The server should return an OK status (200)", response.getStatusLine().getStatusCode(), 200);
        assertTrue("Handler should have run", didHandle[0]);
        assertTrue("The server should have removed the handler", instance.removePostHandler(handler));
    }

    @Test
    public void testUnsuccessfulPostHandler() throws IOException {
        final String path = "/testAddPostHandler";
        final byte[] testBytes = new byte[0];
        final boolean[] didHandle = {false};
        PostHandler handler = bytes -> {
            didHandle[0] = true;
            return false;
        };

        instance.addPostHandler(path, handler);
        HttpResponse response = doPost(path, testBytes);
        assertEquals("Server should return an internal error (500)", response.getStatusLine().getStatusCode(), 500);
        assertTrue("Handler should have run", didHandle[0]);
    }

    /**
     * Test of data supplier methods
     */
    @Test
    public void testDataSuppliers() throws IOException {
        String name = "testDataSuppliers";
        Supplier<?> supplier = () -> name;
        instance.addDataSupplier(name, supplier);
        assertTrue(instance.hasDataSupplier(name));
        instance.removeDataSupplier(name);
        assertFalse(instance.hasDataSupplier(name));
    }

    @Test
    public void testStartStop() throws GripException, IllegalStateException {
        instance.start(); // should do nothing since the server's already running
        instance.stop();  // stop the server so we know we can start it
        instance.stop();  // second call should do nothing
        instance.start(); // restart the server
        instance.start(); // second call should do nothing
        instance.restart(); // should stop and then start again
    }

    @Test
    public void testPort() {
        assertEquals(serverFactory.port, instance.getPort());
    }

    @After
    public void tearDown() {
        instance.stop();
        client.close();
    }

    private String doGet(String path) throws IOException {
        String uri = "http://localhost:" + instance.getPort() + path;
        HttpGet get = new HttpGet(uri);
        HttpResponse response = client.execute(get);
        return EntityUtils.toString(response.getEntity());
    }

    private CloseableHttpResponse doPost(String path, byte[] bytes) throws IOException {
        HttpPost post = new HttpPost("http://localhost:" + instance.getPort() + path);
        BasicHttpEntity httpEntity = new BasicHttpEntity();
        httpEntity.setContent(new ByteArrayInputStream(bytes));
        post.setEntity(httpEntity);
        return client.execute(post);
    }

}
