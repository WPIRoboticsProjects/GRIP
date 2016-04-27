package edu.wpi.grip.core.http;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.handler.AbstractHandler;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generic Jetty handler.
 * <p>
 * Instances of this class can either claim a context, preventing other instances from handling
 * events on that context, or not, in which case it will run on any context. The second type may use
 * {@link #isClaimed(String) isClaimed} to check if a context has been claimed if it shouldn't run on a claimed context.
 */
public abstract class GenericHandler extends AbstractHandler {

    /**
     * Claimed contexts.
     */
    private static final Set<String> claimedContexts = new HashSet<>();

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
     * Creates a generic handler for all contexts on the server.
     */
    protected GenericHandler() {
        super();
        context = null;
    }

    /**
     * Creates a generic handler that handles requests for the given context. That context will not be claimed.
     * <p>
     * Note that the context <strong>is case sensitive</strong>.
     * </p>
     *
     * @param context the context for this handler
     * @throws IllegalArgumentException if the given context has already been claimed
     */
    protected GenericHandler(String context) {
        this(context, false);
    }

    /**
     * Creates a generic handler that handles requests for the given context.
     * <p>
     * Note that the context <strong>is case sensitive</strong>.
     * </p>
     *
     * @param context the context for this handler
     * @param doClaim flag marking if the given context should be claimed
     * @throws IllegalArgumentException if the given context has already been claimed
     */
    protected GenericHandler(String context, boolean doClaim) {
        super();
        checkNotNull(context);
        if (isClaimed(context)) {
            throw new IllegalArgumentException("The given context has already been claimed: " + context);
        }
        this.context = context;
        if (doClaim) {
            claimContext();
        }
    }

    /**
     * Claims the context. Fails if that context has already been claimed and not released.
     *
     * @return true if the context was claimed, false if it has already been claimed
     */
    protected boolean claimContext() {
        return claimedContexts.add(context);
    }

    /**
     * Releases the context that this handles, allowing it to be claimed by another handler.
     */
    protected void releaseContext() {
        claimedContexts.remove(context);
    }

    /**
     * Gets the context for this handler.
     */
    public String getContext() {
        return context;
    }

    /**
     * Checks if the given context has been claimed. Returns {@code false} if given {@code null}.
     *
     * @param context the context to check
     * @return true if the context has been claimed, false if not
     */
    protected static boolean isClaimed(@Nullable String context) {
        return claimedContexts.contains(context);
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
    protected static void sendTextContent(HttpServletResponse response, String content, String contentType) throws IOException {
        response.setContentType(contentType);
        response.getWriter().print(content);
    }

    /**
     * Checks if the given request is a POST
     */
    protected static boolean isPost(HttpServletRequest request) {
        return request.getMethod().equals("POST");
    }

    /**
     * Checks if the given request is a GET
     */
    protected static boolean isGet(HttpServletRequest request) {
        return request.getMethod().equals("GET");
    }

}
