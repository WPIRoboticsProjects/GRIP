
package edu.wpi.grip.core.http;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.net.httpserver.*;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.ProjectManager;
import edu.wpi.grip.core.events.ProjectSettingsChangedEvent;
import edu.wpi.grip.core.exception.GripException;
import edu.wpi.grip.core.serialization.Project;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * An internal HTTP server that can be used as an alternative to NetworkTables.
 */
@Singleton
public class GripServer {

    private static final Logger logger = Logger.getLogger(GripServer.class.getName());

    /**
     * The root path for all GRIP-related HTTP activity.
     */
    public static final String ROOT_PATH = "/GRIP";

    /**
     * The root path for uploading data to the server.
     */
    public static final String UPLOAD_PATH = ROOT_PATH + "/upload";

    /**
     * The path for uploading images. To upload an image, post an HTTP event to
     * {@code http://$grip_ip:$grip_port/GRIP/upload/image} (where
     * {@code $grip_ip} is the IP address of the machine that GRIP is running
     * on, and {@code $grip_port} is the port on that machine that this server
     * is running on), with the image bytes as the data.
     */
    public static final String IMAGE_UPLOAD_PATH = UPLOAD_PATH + "/image";

    /**
     * The path for setting which pipeline to run. To set the pipeline, post an
     * HTTP event to {@code http://$grip_ip:$grip_port/GRIP/upload/pipeline}
     * with the name of the pipeline to run as the data.
     */
    public static final String PIPELINE_UPLOAD_PATH = UPLOAD_PATH + "/pipeline";

    /**
     * The path for requesting data. Data will be returned as a json-formatted
     * map of the outputs of all requested data sets.
     * <p>
     * For example, performing a {@code GET} request on the path
     * {@code /GRIP/data?foo;bar} will return a map such as
     * <br>
     * <code><pre>
     * {
     *   'foo': {
     *        // data
     *      },
     *   'bar':
     *     {
     *       // data
     *     }
     * }
     * </pre></code>
     */
    public static final String DATA_PATH = ROOT_PATH + "/data";

    /* HTTP constants */
    private static final String HOSTNAME = "localhost";
    private static final int BACKLOG = 1;

    private static final String HEADER_ALLOW = "Allow";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final int STATUS_OK = 200;
    private static final int STATUS_METHOD_NOT_ALLOWED = 405;
    private static final int STATUS_INTERNAL_ERROR = 500;

    private static final int NO_RESPONSE_LENGTH = -1;

    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String ALLOWED_METHODS = String.join(", ", METHOD_GET, METHOD_POST, METHOD_OPTIONS);

    public interface HttpServerFactory {
        HttpServer create(int port);
    }

    public static class HttpServerFactoryImpl implements HttpServerFactory {
        @Override
        public HttpServer create(int port) {
            try {
                return HttpServer.create(new InetSocketAddress(HOSTNAME, port), BACKLOG);
            } catch (IOException ex) {
                throw new GripException("Could not create a server on port " + port, ex);
            }
        }
    }

    private final HttpServerFactory serverFactory;
    private HttpServer server;
    private final Map<String, GetHandler> getHandlers;
    private final Map<String, List<PostHandler>> postHandlers;
    private final Map<String, Supplier> dataSuppliers;

    private boolean started = false;
    private boolean stopped = false;

    private final Set<String> contextPaths = new HashSet<>();
    
    private final ProjectManager projectManager;

    @Inject
    GripServer(HttpServerFactory serverFactory, Pipeline pipeline, Project project) {
        int port = pipeline.getProjectSettings().getServerPort();
        this.serverFactory = serverFactory;
        this.server = serverFactory.create(port);
        this.projectManager = new ProjectManager(project);
        getHandlers = new HashMap<>();
        postHandlers = new HashMap<>();
        dataSuppliers = new HashMap<>();

        addGetHandler(DATA_PATH, this::createJson);
        addPostHandler(PIPELINE_UPLOAD_PATH, bytes -> {
            try {
                projectManager.openProject(new String(bytes));
            } catch (IOException ex) {
                return false;
            }
            return true;
        });

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    /**
     * Adds a {@code GetHandler} to the given path. Note that this will also
     * respond to any {@code GET} requests on subpaths unless they also have
     * their own {@code GetHandler} attached. This will do nothing if a {@code GetHandler}
     * is already added to the given path.
     *
     * @param path    the path to attach the handler to
     * @param handler the handler to attach
     */
    public void addGetHandler(String path, GetHandler handler) {
        getHandlers.putIfAbsent(path, handler);
        createContext(path);
    }

    /**
     * Removes the {@link GetHandler} associated with the given path. This will
     * have no effect if no such {@code GetHandler} exists.
     *
     * @param path the path to remove a {@code GetHandler} from
     */
    public void removeGetHandler(String path) {
        getHandlers.remove(path);
        createContext(path);
    }

    /**
     * Adds a {@code PostHandler} to the given path. This will convert the raw
     * bytes read from the connection and convert them to something useful e.g.
     * images, pipeline names, etc.
     *
     * @param path    the path to attach the handler to
     * @param handler the handler to attach
     */
    public void addPostHandler(String path, PostHandler handler) {
        postHandlers.computeIfAbsent(path, k -> new ArrayList<>()).add(handler);
        createContext(path);
    }

    /**
     * Removes all {@link PostHandler PostHandlers} associated with the given path.
     *
     * @param path the path to remove {@code PostHandlers} from
     */
    public void removePostHandlers(String path) {
        postHandlers.remove(path);
        createContext(path);
    }

    /**
     * Removes the given handler from all post events.
     * 
     * @param handler the handler to remove
     * @return true if the handler was removed from at least one path
     */
    public boolean removePostHandler(PostHandler handler) {
        return postHandlers.values()
                .stream()
                .map(list -> list.remove(handler))
                .anyMatch(b -> b);
    }

    private void createContext(String path) {
        if (contextPaths.contains(path)) {
            return;
        }
        contextPaths.add(path);
        server.createContext(path, makeContext(path));
    }

    private HttpHandler makeContext(final String path) {
        return he -> {
            final Headers headers = he.getResponseHeaders();
            final String requestMethod = he.getRequestMethod().toUpperCase();
            final Map<String, List<String>> requestParameters = getRequestParameters(he.getRequestURI());
            switch (requestMethod) {
                case METHOD_GET:
                    headers.set(HEADER_CONTENT_TYPE, String.format("application/json; charset=%s", CHARSET));
                    if (getHandlers.containsKey(path)) {
                        final String responseBody = getHandlers.get(path).createResponse(requestParameters);
                        final byte[] rawResponseBody = responseBody.getBytes(CHARSET);
                        he.sendResponseHeaders(STATUS_OK, rawResponseBody.length);
                        he.getResponseBody().write(rawResponseBody);
                        he.getResponseBody().close();
                    } else {
                        he.sendResponseHeaders(STATUS_OK, NO_RESPONSE_LENGTH);
                    }
                    break;
                case METHOD_POST:
                    final byte[] bytes = readBytes(he);
                    boolean success = true;
                    for (PostHandler handler : postHandlers.get(path)) {
                        success &= handler.convert(bytes);
                    }
                    if (success) {
                        he.sendResponseHeaders(STATUS_OK, NO_RESPONSE_LENGTH);
                    } else {
                        he.sendResponseHeaders(STATUS_INTERNAL_ERROR, NO_RESPONSE_LENGTH);
                    }
                    break;
                case METHOD_OPTIONS:
                    headers.set(HEADER_ALLOW, ALLOWED_METHODS);
                    he.sendResponseHeaders(STATUS_OK, NO_RESPONSE_LENGTH);
                    break;
                default:
                    headers.set(HEADER_ALLOW, ALLOWED_METHODS);
                    he.sendResponseHeaders(STATUS_METHOD_NOT_ALLOWED, NO_RESPONSE_LENGTH);
                    break;
            }
        };
    }

    /**
     * Adds a data supplier for data with the given name. Each name can only be
     * associated with a single supplier at a time, so this will remove the previous
     * supplier (if one exists).
     *
     * @param name     the name of the data
     * @param supplier the supplier of the data
     */
    public void addDataSupplier(String name, Supplier<?> supplier) {
        dataSuppliers.put(name, supplier);
    }

    /**
     * Checks if the given name is associated with a supplier.
     *
     * @param name the name to check
     * @return true if the name is associated with a supplier, false if not
     */
    public boolean hasDataSupplier(String name) {
        return dataSuppliers.containsKey(name);
    }

    /**
     * Removes the data supplier for data with the given name
     *
     * @param name the name of the data to remove
     */
    public void removeDataSupplier(String name) {
        dataSuppliers.remove(name);
    }

    private String createJson(Map<String, List<String>> params) {
        final Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeSpecialFloatingPointValues()
                .create();
        final Map<String, Object> data = 
                dataSuppliers
                .entrySet()
                .stream()
                .filter(e -> params.isEmpty() || params.containsKey(e.getKey())) // send everything if nothing is specified
                .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().get()));
        try {
            return gson.toJson(data);
        } catch (RuntimeException ex) {
            // If we don't catch this, it will be silently consumed by something up the call stack
            logger.log(Level.SEVERE, "Error generating json response", ex);
            return "{}"; // empty json object
        }
    }

    /**
     * Starts this server. Has no effect if the server has already been started.
     */
    public void start() {
        if (!started && !stopped) {
            // Don't call server.start() if it's already been started or stopped
            server.start();
            started = true;
            stopped = false;
        }
    }

    /**
     * Stops this server. Note that a shutdown hook has been registered to call
     * this method, so it's unlikely that this should need to be called.
     */
    @VisibleForTesting
    public void stop() {
        if(!stopped) {
            server.stop(0);
            started = false;
            stopped = true;
        }
    }
    
    
    /**
     * Restarts the server on the current port.
     * 
     * @throws GripException if the server was unable to be restarted.
     */
    public void restart() {
        try {
            stop();
            server = serverFactory.create(getPort());
            start();
        } catch (GripException | IllegalStateException ex) {
            throw new GripException("Could not restart GripServer", ex);
        }
    }
    
    /**
     * Gets the port this server is running on.
     */
    public int getPort() {
        return server.getAddress().getPort();
    }

    /**
     * Parses request parameters from a URI. For example, the URI
     * "http://roborio-190-frc.local:8080/GRIP/data?foo=1;bar=2" will be parsed to
     * {"foo"="1", "bar"="2"}.
     */
    private static Map<String, List<String>> getRequestParameters(final URI requestUri) {
        final Map<String, List<String>> requestParameters = new LinkedHashMap<>();
        final String requestQuery = requestUri.getRawQuery(); // foo=1&bar=2&...
        if (requestQuery != null) {
            final String[] rawRequestParameters = requestQuery.split("[&;]"); // [foo=1, bar=2, ...]
            for (final String rawRequestParameter : rawRequestParameters) {
                final String[] requestParameter = rawRequestParameter.split("=", 2); // [foo, 1]
                final String requestParameterName = decodeUrlComponent(requestParameter[0]);
                requestParameters.putIfAbsent(requestParameterName, new ArrayList<>());
                final String requestParameterValue = requestParameter.length > 1 ? decodeUrlComponent(requestParameter[1]) : null;
                requestParameters.get(requestParameterName).add(requestParameterValue);
            }
        }
        return requestParameters;
    }

    private static String decodeUrlComponent(final String urlComponent) {
        try {
            return URLDecoder.decode(urlComponent, CHARSET.name());
        } catch (final UnsupportedEncodingException ex) {
            // Will never happen, UTF-8 is supported by all JVMs
            throw new GripException(ex);
        }
    }

    /**
     * Reads all incoming bytes from an HTTP connection.
     */
    private static byte[] readBytes(final HttpExchange he) throws IOException {
        final InputStream in = he.getRequestBody();
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        for (int n = in.read(buf); n > 0; n = in.read(buf)) {
            b.write(buf, 0, n);
        }
        return b.toByteArray();
    }

    @Subscribe
    public void settingsChanged(ProjectSettingsChangedEvent event) {
        int port = event.getProjectSettings().getServerPort();
        if (port != getPort()) {
            stop();
            server = serverFactory.create(port);
            start();
            contextPaths.forEach(path -> server.createContext(path, makeContext(path)));
        }
    }

}
