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

import javafx.application.Platform;

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
        String projectXml = new String(IOUtils.toByteArray(request.getInputStream()), "UTF-8");
        // Need to be careful - this will cause a deadlock if not called from the JavaFX application thread
        // TODO change code in UI module to make this safe -- there shouldn't be any references to UI code in the core module
        switch (mode) {
            case HEADLESS:
                project.open(projectXml);
                break;
            case GUI:
                // Since this will never be called when on the RoboRIO, the lack of the JavaFX jar won't be an issue.
                Platform.runLater(() -> project.open(projectXml));
                break;
            default:
                // Will never happen unless a new entry is added to GRIPMode (unlikely)
                throw new IllegalStateException("Unknown GRIP mode: " + mode);
        }
        response.setStatus(HttpServletResponse.SC_CREATED);
        baseRequest.setHandled(true);
    }
}
