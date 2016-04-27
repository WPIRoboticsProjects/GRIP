package edu.wpi.grip.core.http;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.wpi.grip.core.serialization.Project;
import edu.wpi.grip.core.util.GRIPMode;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;

import static edu.wpi.grip.core.util.GRIPMode.HEADLESS;

/**
 * Jetty handler responsible for loading pipelines sent over HTTP.
 */
@Singleton
public class HttpPipelineSwitcher extends PedanticHandler {

    private final Project project;
    private final GRIPMode mode;

    @Inject
    HttpPipelineSwitcher(Project project, GRIPMode mode) {
        super(GripServer.PIPELINE_UPLOAD_PATH, true);
        this.project = project;
        this.mode = mode;
    }

    @Override
    protected void handleIfPassed(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!isPost(request)) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            baseRequest.setHandled(true);
            return;
        }
        switch (mode) {
            case HEADLESS:
                project.open(new String(IOUtils.toByteArray(request.getInputStream()), "UTF-8"));
                response.setStatus(HttpServletResponse.SC_CREATED);
                baseRequest.setHandled(true);
                break;
            case GUI:
                // Don't run in GUI mode, it doesn't make much sense and can easily deadlock if pipelines are rapidly posted.
                // Intentional fall-through to default
            default:
                // Don't know the mode or the mode is unsupported; let the client know
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                sendTextContent(response, String.format("GRIP is not in the correct mode: should be %s, but is %s", HEADLESS, mode), CONTENT_TYPE_PLAIN_TEXT);
                baseRequest.setHandled(true);
                break;
        }
    }
}
