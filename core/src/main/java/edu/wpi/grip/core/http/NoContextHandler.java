package edu.wpi.grip.core.http;

import org.eclipse.jetty.server.Request;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Jetty handler for all contexts that are not explicitly claimed by another handler.
 * This will respond to HTTP requests with a {@code 404 Not Found} error and HTML page.
 */
class NoContextHandler extends GenericHandler {

  private final ContextStore store;

  private static final String notFoundMessage =
      "<h1>404 - Not Found</h1>There is no context for path: '%s'";

  /**
   * Creates a new {@code NoContextHandler} that handles every context not in the given
   * {@code ContextStore}.
   *
   * @param store Any context not in this {@code ContextStore} will get a
   *              {@code 404 Not Found} error.
   */
  public NoContextHandler(ContextStore store) {
    super();
    this.store = store;
  }

  @Override
  public void handle(String target,
                     Request baseRequest,
                     HttpServletRequest request,
                     HttpServletResponse response) throws IOException, ServletException {
    if (store.contains(target)) {
      // Let the appropriate handler handle this
      return;
    }
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    response.setContentType(CONTENT_TYPE_HTML);
    response.getWriter().printf(notFoundMessage, target);
    baseRequest.setHandled(true);
  }
}
