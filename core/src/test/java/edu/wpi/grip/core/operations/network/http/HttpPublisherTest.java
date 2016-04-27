package edu.wpi.grip.core.operations.network.http;

import com.google.common.eventbus.EventBus;

import edu.wpi.grip.core.events.RunStartedEvent;
import edu.wpi.grip.core.events.RunStoppedEvent;
import edu.wpi.grip.core.http.GenericHandler;
import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.http.GripServerTest;
import edu.wpi.grip.core.operations.network.NumberPublishable;
import edu.wpi.grip.core.settings.ProjectSettings;
import edu.wpi.grip.core.sockets.InputSocket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HttpPublisherTest {

    private static final String dataPath = "/GRIP/data";
    private static final String noDataPath = "/GRIP/data?no_data_here";
    private static final String empty = "{}";
    private static final String name = "foo";
    private static final String json = "{\n  \"foo\": 1.0\n}";
    private static final String unexpectedResponseMsg = "Unexpected response to data request";

    private EventBus eventBus;
    private GripServer server;
    private DataHandler dataHandler;
    private HttpPublishOperation<Number, NumberPublishable, Double> operation;

    private HttpClient client;

    @Before
    public void setUp() {
        eventBus = new EventBus();
        server = GripServerTest.makeServer(new GripServerTest.TestServerFactory(), ProjectSettings::new);
        unclaimDataHandler();
        dataHandler = new DataHandler(eventBus);

        operation = new HttpPublishOperation<Number, NumberPublishable, Double>(new HttpPublishManager(server, dataHandler), NumberPublishable::new) {};

        client = HttpClients.createDefault();

        server.addHandler(dataHandler);
        server.start();
    }

    private void unclaimDataHandler() {
        try {
            Field f = GenericHandler.class.getDeclaredField("claimedContexts");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            Set<String> claimedContexts = (Set<String>) f.get(null);
            claimedContexts.remove(GripServer.DATA_PATH);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            fail("Could not unclaim /GRIP/data");
        }
    }

    @SuppressWarnings("unchecked")
    private void perform() {
        InputSocket[] inputs = operation.createInputSockets(eventBus);
        inputs[0].setValue(1.0);
        inputs[1].setValue(name);
        operation.perform(
                inputs,
                operation.createOutputSockets(eventBus),
                operation.createData());
    }

    @Test
    public void testNoData() throws IOException {
        assertEquals("Data was not empty", empty, doGetText(dataPath));
        assertEquals("Shouldn't be data on this path", empty, doGetText(noDataPath));
    }

    @Test
    public void testWithData() throws IOException {
        perform();
        assertEquals(unexpectedResponseMsg, json, doGetText(dataPath));
        assertEquals("Shouldn't be data on this path", empty, doGetText(noDataPath));
    }

    @Test
    public void testWhenPipelineRunning() throws IOException {
        perform();
        eventBus.post(new RunStartedEvent());
        assertEquals("Server should have returned a 503 status", 503, doGet(dataPath).getStatusLine().getStatusCode());
        eventBus.post(new RunStoppedEvent());
        assertEquals("Data handler should have run", json, doGetText(dataPath));
    }

    @Test
    public void testNotPost() throws IOException {
        dataHandler.addDataSupplier("fail", () -> {
            fail("This should not have been called");
            return null;
        });
        HttpResponse response = doPost(dataPath);
        assertEquals("Server should have returned a 405 status", 405, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testDataSuppliers() throws IOException {
        perform();
        dataHandler.addDataSupplier("some_data", () -> "some_value");
        assertEquals(unexpectedResponseMsg, "{\n  \"foo\": 1.0,\n  \"some_data\": \"some_value\"\n}", doGetText(dataPath));
        assertEquals(unexpectedResponseMsg, json, doGetText("/GRIP/data?foo"));
        dataHandler.removeDataSupplier("some_data");
        assertEquals(unexpectedResponseMsg, json, doGetText("/GRIP/data?foo"));
        assertEquals(unexpectedResponseMsg, json, doGetText(dataPath));
    }

    @Test(expected = NullPointerException.class)
    public void testNullSupplierName() {
        dataHandler.addDataSupplier(null, () -> "null_supplier_name");
    }

    @Test(expected = NullPointerException.class)
    public void testNullSupplier() {
        dataHandler.addDataSupplier("null_supplier", null);
    }

    @After
    public void tearDown() {
        server.removeHandler(dataHandler);
        server.stop();
    }

    private String doGetText(String path) throws IOException {
        HttpEntity entity = doGet(path).getEntity();
        String s = EntityUtils.toString(entity);
        EntityUtils.consume(entity);
        return s;
    }

    private HttpResponse doGet(String path) throws IOException {
        String uri = "http://localhost:" + server.getPort() + path;
        HttpGet get = new HttpGet(uri);
        return client.execute(get);
    }

    private HttpResponse doPost(String path) throws IOException {
        String uri = "http://localhost:" + server.getPort() + path;
        HttpPost post = new HttpPost(uri);
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream(new byte[0]));
        post.setEntity(entity);
        return client.execute(post);
    }

}
