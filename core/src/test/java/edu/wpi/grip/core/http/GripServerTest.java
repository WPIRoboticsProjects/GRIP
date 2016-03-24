
package edu.wpi.grip.core.http;

import com.sun.net.httpserver.HttpServer;
import edu.wpi.grip.core.MockPipeline;
import edu.wpi.grip.core.MockPipeline.MockProjectSettings;

import edu.wpi.grip.core.http.GripServer.HttpServerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.function.Supplier;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
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

    private HttpServer server;
    private int port;

    public static class TestServerFactory implements HttpServerFactory {

        @Override
        public HttpServer create(int port) {
            HttpServer server;
            for (int offset = 0;; offset++) {
                try {
                    server = HttpServer.create(new InetSocketAddress("localhost", port + offset), 1);
                    break;
                } catch (IOException e) {
                    continue;
                }
            }
            return server;
        }

    }

    // DON'T USE INJECTION FOR THIS
    private GripServer instance;

    public GripServerTest() {
        for (int portOffset = 0;; portOffset++) {
            try {
                server = HttpServer.create(new InetSocketAddress("localhost", GRIP_SERVER_TEST_PORT + portOffset), 1);
                port = GRIP_SERVER_TEST_PORT + portOffset;
                break;
            } catch (IOException ex) {
                // That port is taken -- keep trying different ports
            }
        }
        MockProjectSettings mockSettings = new MockProjectSettings();
        mockSettings.setServerPort(port);
        instance = new GripServer((ignore) -> server, new MockPipeline(mockSettings));
        instance.start();

        client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
    }

    /**
     * Test of GetHandler methods, of class GripServer.
     */
    @Test
    public void testGetHandlers() {
        String path = "/testGetHandlers";
        GetHandler handler = params -> path;
        instance.addGetHandler(path, handler);
        instance.addDataSupplier(path, () -> path);
        try {
            String data = doGet(path);
            assertEquals(data, path);
        } catch (IOException ex) {
            fail(ex.getMessage());
        } finally {
            instance.removeGetHandler(path); // cleanup
        }
    }

    /**
     * Test of addPostHandler method, of class GripServer.
     */
    @Test
    public void testAddPostHandler() {
        String path = "/testAddPostHandler";
        byte[] testBytes = "testAddPostHandler".getBytes();
        PostHandler handler = bytes -> Arrays.equals(bytes, testBytes);
        instance.addPostHandler(path, handler);
        try {
            doPost(path, testBytes);
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            instance.removePostHandler(handler); // cleanup
        }
    }

    /**
     * Test of data supplier methods
     */
    @Test
    public void testDataSuppliers() {
        String name = "testDataSuppliers";
        Supplier<?> supplier = () -> name;
        instance.addDataSupplier(name, supplier);
        assertTrue(instance.hasDataSupplier(name));
        instance.removeDataSupplier(name);
        assertFalse(instance.hasDataSupplier(name));
    }

    /**
     * Test of start and stop methods, of class GripServer.
     */
    @Test
    public void testStartStop() {
        try {
            instance.start(); // should do nothing since the server's already running
            instance.stop();  // stop the server so we know we can start it
            instance.stop();  // second call should do nothing
            instance.start(); // restart the server
            instance.start(); // second call should do nothing
            instance.restart(); // should stop and then start again
        } catch (Exception e) {
            // Starting or stopping when in an invalid state will throw an exception
            fail(e.getMessage());
        }
    }

    @Test
    public void testPort() {
        assertEquals(port, instance.getPort());
    }

    @After
    public void stopServer() {
        instance.stop();
    }

    private String doGet(String path) throws IOException {
        String uri = "http://localhost:" + port + path;
        HttpGet get = new HttpGet(uri);
        HttpResponse response = client.execute(get);
        return EntityUtils.toString(response.getEntity());
    }

    private void doPost(String path, byte[] bytes) throws IOException {
        HttpPost post = new HttpPost("http://localhost:" + port + path);
        BasicHttpEntity httpEntity = new BasicHttpEntity();
        httpEntity.setContent(new ByteArrayInputStream(bytes));
        post.setEntity(httpEntity);
        client.execute(post).close();
        client.close();
    }

}
