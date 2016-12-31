package edu.wpi.grip.core.http;

import edu.wpi.grip.core.serialization.Project;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Jetty handler responsible for loading pipelines sent over HTTP.
 */
@Singleton
public class HttpPipelineSwitcher extends PedanticHandler {

  private final Project project;

  @Inject
  HttpPipelineSwitcher(ContextStore store, Project project) {
    super(store, GripServer.PIPELINE_UPLOAD_PATH, true);
    this.project = project;
  }

  @Override
  protected void handleIfPassed(String target,
                                Request baseRequest,
                                HttpServletRequest request,
                                HttpServletResponse response) throws IOException, ServletException {
    if (!isPost(request)) {
      response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      baseRequest.setHandled(true);
      return;
    }
    project.open(new String(IOUtils.toByteArray(request.getInputStream()), "UTF-8"));
    response.setStatus(HttpServletResponse.SC_CREATED);
    baseRequest.setHandled(true);
  }
}
