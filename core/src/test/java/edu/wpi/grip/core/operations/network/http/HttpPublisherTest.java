package edu.wpi.grip.core.operations.network.http;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.events.RunStartedEvent;
import edu.wpi.grip.core.events.RunStoppedEvent;
import edu.wpi.grip.core.http.ContextStore;
import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.http.GripServerTest;
import edu.wpi.grip.core.operations.network.NumberPublishable;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.MockInputSocketFactory;

import com.google.common.eventbus.EventBus;

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

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
  private HttpPublishOperation<Number, NumberPublishable> operation;

  private HttpClient client;

  @Rule
  public final Timeout timeout = new Timeout(10000, TimeUnit.MILLISECONDS);

  @Before
  public void setUp() {
    eventBus = new EventBus();
    ContextStore contextStore = new ContextStore();
    InputSocket.Factory isf = new MockInputSocketFactory(eventBus);
    server = GripServerTest.makeServer(
        contextStore, new GripServerTest.TestServerFactory(), new Pipeline());
    dataHandler = new DataHandler(contextStore);
    eventBus.register(dataHandler);

    operation = new HttpPublishOperation<>(
        isf,
        Number.class,
        NumberPublishable.class,
        NumberPublishable::new,
        new HttpPublishManager(server, dataHandler)
    );

    client = HttpClients.createDefault();

    server.addHandler(dataHandler);
    server.start();
  }

  @SuppressWarnings("unchecked")
  private void perform() {
    List<InputSocket> inputs = operation.getInputSockets();
    inputs.get(0).setValue(1.0);
    inputs.get(1).setValue(name);
    operation.perform();
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
    // Stop the pipeline after (about) 500ms
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        eventBus.post(new RunStoppedEvent());
      }
    }, 500);
    // Start the pipeline. Will get stopped in a bit by the timer task
    eventBus.post(new RunStartedEvent());
    doGet(dataPath); // should block
    assertEquals("Data handler should have run", json, doGetText(dataPath));
  }

  @Test
  public void testNotPost() throws IOException {
    dataHandler.addDataSupplier("fail", () -> {
      fail("This should not have been called");
      return null;
    });
    HttpResponse response = doPost(dataPath);
    assertEquals("Server should have returned a 405 status",
        405,
        response.getStatusLine().getStatusCode());
  }

  @Test
  public void testDataSuppliers() throws IOException {
    perform();
    dataHandler.addDataSupplier("some_data", () -> "some_value");
    assertEquals(unexpectedResponseMsg,
        "{\n  \"foo\": 1.0,\n  \"some_data\": \"some_value\"\n}",
        doGetText(dataPath));
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
