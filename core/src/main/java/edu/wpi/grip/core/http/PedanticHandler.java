package edu.wpi.grip.core.http;

import edu.wpi.grip.core.exception.GripServerException;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A handler that will only run if a request is on the same path as its context.
 */
public abstract class PedanticHandler extends GenericHandler {

  /**
   * Creates a new handler for the given context. That context will not be claimed.
   *
   * @param store   the {@code ContextStore} to store this context in
   * @param context the context for this handler
   * @see GenericHandler#GenericHandler(ContextStore, String)
   */
  protected PedanticHandler(ContextStore store, String context) {
    super(store, context);
  }

  /**
   * Creates a new handler for the given context.
   *
   * @param store   the {@code ContextStore} to store this context in
   * @param context the context for this handler
   * @param doClaim if the context should be claimed
   * @see GenericHandler#GenericHandler(ContextStore, String, boolean)
   */
  protected PedanticHandler(ContextStore store, String context, boolean doClaim) {
    super(store, context, doClaim);
  }

  @Override
  public final void handle(String target,
                           Request baseRequest,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException, ServletException {
    if (!this.context.equals(target)) {
      return;
    }
    try {
      handleIfPassed(target, baseRequest, request, response);
    } catch (RuntimeException ex) {
      Logger.getLogger(getClass().getName())
          .log(Level.SEVERE, "Exception when handling HTTP request", ex);
      throw new GripServerException("Exception when handling HTTP request", ex);
    }
  }

  /**
   * Handles an HTTP request if the target is the same as the one for this handler.
   *
   * @param target      the target of the HTTP request (e.g. a request on "localhost:8080/foo/bar"
   *                    has a target of "foo/bar")
   * @param baseRequest the base HTTP request
   * @param request     the request after being wrapped or filtered by other handlers
   * @param response    the HTTP response to send to the client
   * @see AbstractHandler#handle(String, Request, HttpServletRequest, HttpServletResponse)
   */
  protected abstract void handleIfPassed(String target,
                                         Request baseRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response)
      throws IOException, ServletException;

}
