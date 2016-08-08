package edu.wpi.grip.core.sources;

import edu.wpi.grip.core.http.ContextStore;
import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.http.PedanticHandler;

import org.apache.commons.io.IOUtils;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;

/**
 * Jetty handler for incoming images to be used by {@link HttpSource}.
 * Only one instance of this class can exist for a context.
 *
 * <p>This handler will return one of the following status codes to a request on
 * {@code /GRIP/upload/image}:
 * <ul>
 * <li>405 - Not Allowed: if the request is not a POST</li>
 * <li>202 - Accepted: if the image sent is the same as the previous one</li>
 * <li>201 - Created: if the image was successfully handled</li>
 * </ul>
 */
public final class HttpImageHandler extends PedanticHandler {

  /**
   * Callbacks that take OpenCV Mats. These will be called when a new image is posted.
   */
  private final List<Consumer<Mat>> callbacks;

  /**
   * The most recent image. Could be a local field, but this is more memory-efficient.
   */
  private Mat image;

  /**
   * The most recent bytes received.
   */
  private byte[] lastBytes;

  /**
   * Creates an image handler on the default upload path {@code /GRIP/upload/image}.
   */
  public HttpImageHandler(ContextStore store) {
    this(store, GripServer.IMAGE_UPLOAD_PATH);
  }

  /**
   * Creates an image handler on the given path.
   *
   * @param path the path on the server that images will be uploaded to
   */
  public HttpImageHandler(ContextStore store, String path) {
    super(store, path, true);
    callbacks = new ArrayList<>();
  }

  @Override
  protected void handleIfPassed(String target,
                                Request baseRequest,
                                HttpServletRequest request,
                                HttpServletResponse response) throws IOException, ServletException {
    if (!isPost(request)) {
      response.setStatus(SC_METHOD_NOT_ALLOWED);
      baseRequest.setHandled(true);
      return;
    }
    byte[] newBytes = IOUtils.toByteArray(request.getInputStream());
    if (Arrays.equals(lastBytes, newBytes)) {
      // no change
      response.setStatus(SC_ACCEPTED);
      baseRequest.setHandled(true);
      return;
    }
    image = new Mat(newBytes);
    lastBytes = newBytes;
    callbacks.forEach(c -> c.accept(image));
    response.setStatus(SC_CREATED);
    baseRequest.setHandled(true);
  }

  /**
   * Gets the most recently POSTed image.
   */
  public Optional<Mat> getImage() {
    return Optional.ofNullable(image);
  }

  /**
   * Adds a callback to this handler. The callback will be called when a new image is POSTed to
   * {@code /GRIP/upload/image} and can be removed later with {@link #removeCallback(Consumer)}.
   *
   * @param callback the callback to add
   * @see #removeCallback(Consumer)
   */
  public void addCallback(Consumer<Mat> callback) {
    callbacks.add(checkNotNull(callback));
  }

  /**
   * Removes the given callback from this handler. The callback will no longer be called when a new
   * image is POSTed to {@code /GRIP/upload/image}, unless it is re-added with
   * {@link #addCallback(Consumer)}. Does nothing if {@code callback} is {@code null}.
   *
   * @param callback the callback to remove
   * @see #addCallback(Consumer)
   */
  public void removeCallback(@Nullable Consumer<Mat> callback) {
    callbacks.remove(callback);
  }
}
