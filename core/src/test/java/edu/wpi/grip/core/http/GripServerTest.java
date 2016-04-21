
package edu.wpi.grip.core.http;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;

import edu.wpi.grip.core.exception.GripException;
import edu.wpi.grip.core.exception.GripServerException;
import edu.wpi.grip.core.http.GripServer.HttpServerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import edu.wpi.grip.core.serialization.Project;
import edu.wpi.grip.core.settings.ProjectSettings;
import edu.wpi.grip.core.settings.SettingsProvider;
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

    /**
     * Public factory method for testing.
     */
    public static GripServer makeServer(HttpServerFactory factory, SettingsProvider settingsProvider, Project project) {
        return new GripServer(factory, settingsProvider, project);
    }

    public GripServerTest() {
        ProjectSettings mockSettings = new ProjectSettings();
        mockSettings.setServerPort(GRIP_SERVER_TEST_PORT);
        this.serverFactory = new TestServerFactory();
        instance = new GripServer(serverFactory, () -> mockSettings, new Project());
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
        assertEquals("The data supplied on " + path + " was not correct", path, doGet(path));
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
        final String path = "/testSuccessfulPostHandler";
        final byte[] testBytes = "testAddPostHandler".getBytes();
        final boolean[] didHandle = {false};
        PostHandler handler = bytes -> {
            didHandle[0] = true;
            return Arrays.equals(bytes, testBytes);
        };

        instance.addPostHandler(path, handler);
        HttpResponse response = doPost(path, testBytes);
        assertEquals("The server should return an OK status (200)", 200, response.getStatusLine().getStatusCode());
        assertTrue("Handler should have run", didHandle[0]);
        assertTrue("The server should have removed the handler", instance.removePostHandler(handler));
    }

    @Test
    public void testUnsuccessfulPostHandler() throws IOException {
        final String path = "/testUnsuccessfulPostHandler";
        final byte[] testBytes = new byte[1];
        final boolean[] didHandle = {false};
        PostHandler handler = bytes -> {
            didHandle[0] = true;
            throw new GripServerException("Expected");
        };

        instance.addPostHandler(path, handler);
        HttpResponse response = doPost(path, testBytes);
        assertEquals("Server should return an internal error (500)", 500, response.getStatusLine().getStatusCode());
        assertTrue("Handler should have run", didHandle[0]);
    }

    @Test
    public void testEmptyPostData() throws IOException {
        final String path = "/testEmptyPostData";
        final byte[] testBytes = new byte[0];
        PostHandler handler = bytes -> {
            fail("Handler should not have run if there is no data");
            return false; // won't be reached
        };

        instance.addPostHandler(path, handler);
        HttpResponse response = doPost(path, testBytes);
        assertEquals("Server should return an internal error (500)", 500, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testAddRemoveDataSupplier() {
        String name = "testDataSuppliers";
        Supplier<?> supplier = () -> name;
        instance.addDataSupplier(name, supplier);
        assertTrue("Server should have a data supplier for " + name, instance.hasDataSupplier(name));
        instance.removeDataSupplier(name);
        assertFalse("Server should no longer have a data supplier for " + name, instance.hasDataSupplier(name));
    }

    @Test
    public void testNoSupplier() throws IOException {
        assertEquals("There shouldn't be any data on the server", "{}", doGet(GripServer.DATA_PATH));
        assertEquals("There shouldn't be any data on the server", "{}", doGet(GripServer.DATA_PATH + "?no_data_here"));
    }

    @Test
    public void testWithSupplier() throws IOException {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final String dataName = "testWithSupplier";
        final Map data = ImmutableMap.of("foo", "bar");
        instance.addDataSupplier(dataName, () -> data);
        assertEquals("Generated json was malformed", gson.toJson(ImmutableMap.of(dataName, data)), doGet(GripServer.DATA_PATH + "?" + dataName));
        assertEquals("Generated json was malformed", gson.toJson(ImmutableMap.of(dataName, data)), doGet(GripServer.DATA_PATH));
        assertEquals("There shouldn't be any data on this path", "{}", doGet(GripServer.DATA_PATH + "?no_data_here"));
    }

    @Test
    public void testStartStop() throws GripException, IllegalStateException {
        instance.start(); // should do nothing since the server's already running
        instance.stop();  // stop the server so we know we can start it
        instance.stop();  // second call should do nothing
        instance.start(); // restart the server
        instance.start(); // second call should do nothing
        instance.restart(); // should stop and then start again
        // No asserts or fails -- if something goes wrong, it would have thrown an exception
    }

    @Test
    public void testPort() {
        assertEquals("Server should have been created on port " + serverFactory.port,
                serverFactory.port, instance.getPort());
    }

    @After
    public void tearDown() {
        instance.stop();
    }

    private String doGet(String path) throws IOException {
        String uri = "http://localhost:" + instance.getPort() + path;
        HttpGet get = new HttpGet(uri);
        HttpResponse response = client.execute(get);
        return EntityUtils.toString(response.getEntity());
    }

    private HttpResponse doPost(String path, byte[] bytes) throws IOException {
        HttpPost post = new HttpPost("http://localhost:" + instance.getPort() + path);
        BasicHttpEntity httpEntity = new BasicHttpEntity();
        httpEntity.setContent(new ByteArrayInputStream(bytes));
        post.setEntity(httpEntity);
        return client.execute(post);
    }

}
