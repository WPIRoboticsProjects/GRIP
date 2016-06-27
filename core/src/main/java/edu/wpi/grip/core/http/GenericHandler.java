package edu.wpi.grip.core.http;

import org.eclipse.jetty.server.handler.AbstractHandler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generic Jetty handler.
 *
 * <p>Instances of this class can either claim a context, preventing other instances from handling
 * events on that context, or not, in which case it will run on any context.
 */
public abstract class GenericHandler extends AbstractHandler {

  private final ContextStore contextStore;

  /**
   * The context that this handles.
   */
  protected final String context;

  /**
   * HTTP content type for json.
   */
  public static final String CONTENT_TYPE_JSON = "application/json";

  /**
   * HTTP content type for HTML.
   */
  public static final String CONTENT_TYPE_HTML = "text/html";

  /**
   * HTTP content type for plain text.
   */
  public static final String CONTENT_TYPE_PLAIN_TEXT = "text/plain";

  /**
   * Creates a generic handler for all contexts on the server.
   */
  protected GenericHandler() {
    super();
    contextStore = new ContextStore();
    context = null;
  }

  /**
   * Creates a generic handler that handles requests for the given context.
   * That context will not be claimed.
   *
   * <p>Note that the context <strong>is case sensitive</strong>.
   *
   * @param store   the context store to use to check for claimed contexts
   * @param context the context for this handler
   * @throws IllegalArgumentException if the given context has already been claimed
   */
  protected GenericHandler(ContextStore store, String context) {
    this(store, context, false);
  }

  /**
   * Creates a generic handler that handles requests for the given context.
   * <p>
   * Note that the context <strong>is case sensitive</strong>.
   * </p>
   *
   * @param store   the context store to use to check for claimed contexts
   * @param context the context for this handler
   * @param doClaim flag marking if the given context should be claimed
   * @throws IllegalArgumentException if the given context has already been claimed
   */
  protected GenericHandler(ContextStore store, String context, boolean doClaim) {
    super();
    checkNotNull(context);
    if (doClaim) {
      store.record(context);
    }
    this.contextStore = store;
    this.context = context;
  }

  /**
   * Releases the context that this handles, allowing it to be claimed by another handler.
   */
  protected void releaseContext() {
    contextStore.erase(context);
  }

  /**
   * Gets the context for this handler.
   */
  public String getContext() {
    return context;
  }

  // Static helper methods

  /**
   * Sends text content to the client.
   *
   * @param response    the response that will be sent to the client
   * @param content     the content to send
   * @param contentType the type of the content (e.g. "application/json", "text/html")
   * @throws IOException if content couldn't be written to the response
   */
  protected static void sendTextContent(HttpServletResponse response,
                                        String content,
                                        String contentType) throws IOException {
    response.setContentType(contentType);
    response.getWriter().print(content);
  }

  /**
   * Checks if the given request is a POST.
   */
  protected static boolean isPost(HttpServletRequest request) {
    return request.getMethod().equals("POST");
  }

  /**
   * Checks if the given request is a GET.
   */
  protected static boolean isGet(HttpServletRequest request) {
    return request.getMethod().equals("GET");
  }

}
