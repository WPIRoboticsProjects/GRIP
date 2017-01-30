package edu.wpi.grip.core.http;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.serialization.Project;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HttpPipelineSwitcherTest {

  private GripServer server;
  private Project project;
  private HttpPipelineSwitcher pipelineSwitcher;
  private HttpClient client;

  @Before
  public void setUp() {
    server = new GripServer(new ContextStore(), new GripServerTest.TestServerFactory(),
        new Pipeline());
    client = HttpClients.createDefault();
    server.start();
  }

  @Test
  public void testNotPost() throws IOException {
    project = new Project() {
      @Override
      public void open(String projectXml) {
        fail("This should not have been called");
      }
    };
    pipelineSwitcher = makePipelineSwitcher();
    server.addHandler(pipelineSwitcher);
    HttpResponse response = doGet(GripServer.PIPELINE_UPLOAD_PATH);
    EntityUtils.consume(response.getEntity());
  }

  @Test
  public void testByteConversion() throws IOException {
    String payload = "Lorem ipsum";
    boolean[] didRun = {false};
    project = new Project() {
      @Override
      public void open(String projectXml) {
        didRun[0] = true;
        assertEquals("Payload was not converted correctly", payload, projectXml);
      }
    };
    pipelineSwitcher = makePipelineSwitcher();
    server.addHandler(pipelineSwitcher);
    HttpResponse response = doPost(GripServer.PIPELINE_UPLOAD_PATH, payload);
    EntityUtils.consume(response.getEntity());
    assertTrue("Project was not opened", didRun[0]);
  }

  @Test
  public void testHeadless() throws IOException {
    boolean[] didRun = {false};
    project = new Project() {
      @Override
      public void open(String projectXml) {
        didRun[0] = true;
      }
    };
    pipelineSwitcher = makePipelineSwitcher();
    server.addHandler(pipelineSwitcher);
    HttpResponse response = doPost(GripServer.PIPELINE_UPLOAD_PATH, "dummy data");
    assertEquals("HTTP status should be 201 Created",
        201,
        response.getStatusLine().getStatusCode());
    EntityUtils.consume(response.getEntity());
    assertTrue("Project was not opened", didRun[0]);
  }

  @After
  public void tearDown() {
    server.stop();
    server.removeHandler(pipelineSwitcher);
    pipelineSwitcher.releaseContext();
  }

  private HttpPipelineSwitcher makePipelineSwitcher() {
    return new HttpPipelineSwitcher(new ContextStore(), project);
  }

  private HttpResponse doGet(String path) throws IOException {
    String uri = "http://localhost:" + server.getPort() + path;
    HttpGet get = new HttpGet(uri);
    return client.execute(get);
  }

  private HttpResponse doPost(String path, String text) throws IOException {
    HttpPost post = new HttpPost("http://localhost:" + server.getPort() + path);
    BasicHttpEntity httpEntity = new BasicHttpEntity();
    httpEntity.setContent(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    post.setEntity(httpEntity);
    return client.execute(post);
  }

}
