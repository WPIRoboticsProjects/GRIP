
package edu.wpi.grip.core.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import edu.wpi.grip.core.exception.GripException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 *
 */
public class GripServer {
    
    public interface Factory {
        public GripServer create(int port);
    }
    
    public class FactroyImpl implements Factory {

        @Override
        public GripServer create(int port) {
            return new GripServer(port);
        }
        
    }

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
    private static final String ALLOWED_METHODS = METHOD_GET + "," + METHOD_OPTIONS;

    private final HttpServer server;
    private final int port;
    private final Map<String, GetHandler> getHandlers;
    private final Map<String, PostHandler> postHandlers;
    private final Map<String, Supplier> dataSuppliers;

    @Inject
    public GripServer(int port) throws GripException {
        try {
            server = HttpServer.create(new InetSocketAddress(HOSTNAME, port), BACKLOG);
        } catch (IOException ex) {
            throw new GripException(ex);
        }

        this.port = port;

        getHandlers = new HashMap<>();
        postHandlers = new HashMap<>();
        dataSuppliers = new HashMap<>();

        addGetHandler(DATA_PATH, this::createJson);

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public int getPort() {
        return port;
    }

    /**
     * Adds a {@code GetHandler} to the given path. Note that this will also
     * respond to any {@code GET} requests on subpaths unless they also have
     * their own {@code GetHandler} attached.
     *
     * @param path the path to attach the handler to
     * @param handler the handler to attach
     */
    public void addGetHandler(String path, GetHandler handler) {
        getHandlers.put(path, handler);
        updateContext(path);
    }

    /**
     * Removes the {@link GetHandler} associated with the given path. This will
     * have no effect if no such {@code GetHandler} exists.
     *
     * @param path the path to remove a {@code GetHandler} from
     */
    public void removeGetHandler(String path) {
        getHandlers.remove(path);
        updateContext(path);
    }

    /**
     * Adds a {@code PostHandler} to the given path. This will convert the raw
     * bytes read from the connection and convert them to something useful e.g.
     * images, pipeline names, etc.
     *
     * @param path the path to attach the handler to
     * @param handler the handler to attach
     */
    public void addPostHandler(String path, PostHandler handler) {
        postHandlers.put(path, handler);
        updateContext(path);
    }

    /**
     * Removes the {@link PostHandler} associated with the given path. This will
     * have no effect if no such {@code PostHandler} exists.
     *
     * @param path the path to remove a {@code PostHandler} from
     */
    public void removePostHandler(String path) {
        postHandlers.remove(path);
        updateContext(path);
    }

    private void updateContext(String path) {
        server.createContext(path, he -> {
            final Headers headers = he.getResponseHeaders();
            final String requestMethod = he.getRequestMethod().toUpperCase();
            final Map<String, List<String>> requestParameters = getRequestParameters(he.getRequestURI());
            switch (requestMethod) {
                case METHOD_GET:
                    final String responseBody = getHandlers.get(path).createResponse(requestParameters);
                    headers.set(HEADER_CONTENT_TYPE, String.format("application/json; charset=%s", CHARSET));
                    final byte[] rawResponseBody = responseBody.getBytes(CHARSET);
                    he.sendResponseHeaders(STATUS_OK, rawResponseBody.length);
                    he.getResponseBody().write(rawResponseBody);
                    break;
                case METHOD_POST:
                    final byte[] bytes = readBytes(he);
                    boolean success = postHandlers.get(path).convert(bytes);
                    if (success) {
                        he.sendResponseHeaders(STATUS_OK, NO_RESPONSE_LENGTH);
                    } else {
                        he.sendResponseHeaders(STATUS_INTERNAL_ERROR, NO_RESPONSE_LENGTH);
                    }
                    break;
                case METHOD_OPTIONS:
                    he.getResponseHeaders().set(HEADER_ALLOW, ALLOWED_METHODS);
                    he.sendResponseHeaders(STATUS_OK, NO_RESPONSE_LENGTH);
                    break;
                default:
                    he.getResponseHeaders().set(HEADER_ALLOW, ALLOWED_METHODS);
                    he.sendResponseHeaders(STATUS_METHOD_NOT_ALLOWED, NO_RESPONSE_LENGTH);
                    break;
            }
        });
    }

    /**
     * Adds a data supplier for data with the given name. Each name can only be
     * associated with a single supplier at a time. If a supplier is already
     * associated with the given name, it will have to be removed with
     * {@link #removeDataSupplier removeDataSupplier} first.
     *
     * @param name the name of the data
     * @param supplier the supplier of the data
     */
    public void addDataSupplier(String name, Supplier<?> supplier) {
        dataSuppliers.putIfAbsent(name, supplier);
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
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final Map<String, Object> data = new HashMap<>();
        dataSuppliers.entrySet()
                .stream()
                .filter(e -> params.containsKey(e.getKey()))
                .forEach(e -> data.put(e.getKey(), e.getValue().get()));
        return gson.toJson(data);
    }

    /**
     * Starts this server.
     */
    public void start() {
        server.start();
    }

    /**
     * Stops this server. Note that a shutdown hook has been registered to call
     * this method, so it's unlikely that this should need to be called.
     */
    public void stop() {
        server.stop(0);
    }

    /*
     * Parses request parameters from a URI. For example, the URI
     * "http://roborio-190-frc.local:8080/GRIP/data?foo=1;bar=2" will be parsed to
     * {"foo"="1", "bar"="2"}.
     */
    private static Map<String, List<String>> getRequestParameters(final URI requestUri) {
        final Map<String, List<String>> requestParameters = new LinkedHashMap<>();
        final String requestQuery = requestUri.getRawQuery();
        if (requestQuery != null) {
            final String[] rawRequestParameters = requestQuery.split("[&;]", -1);
            for (final String rawRequestParameter : rawRequestParameters) {
                final String[] requestParameter = rawRequestParameter.split("=", 2);
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
            throw new GripException(ex);
        }
    }

    /*
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

}
