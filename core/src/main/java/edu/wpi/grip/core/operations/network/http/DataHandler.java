package edu.wpi.grip.core.operations.network.http;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Singleton;

import edu.wpi.grip.core.events.RunStartedEvent;
import edu.wpi.grip.core.events.RunStoppedEvent;
import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.http.PedanticHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

/**
 * Jetty handler for sending HTTP publishing data to a client.
 * Only one instance of this class should exist at a time.
 */
@Singleton
public final class DataHandler extends PedanticHandler {

    /**
     * Json serializer.
     */
    private final Gson gson;

    /**
     * Map of data supplier to their names.
     */
    private final Map<String, Supplier<?>> dataSuppliers;

    /**
     * Atomic flag set when the pipeline is running so that requests for data won't get stale data.
     */
    private final AtomicBoolean staleData;

    DataHandler() {
        super(GripServer.DATA_PATH, true);
        this.dataSuppliers = new HashMap<>();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeSpecialFloatingPointValues()
                .create();
        this.staleData = new AtomicBoolean(false);
    }

    @Override
    protected void handleIfPassed(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!isGet(request)) {
            // Only allow GET on the data path
            response.setStatus(SC_METHOD_NOT_ALLOWED);
            baseRequest.setHandled(true);
            return;
        }
        if (staleData.get()) {
            // Pipeline is running, don't hand out stale data
            response.setStatus(SC_SERVICE_UNAVAILABLE);
            baseRequest.setHandled(true);
            return;
        }
        final Map<String, String[]> uriParameters = request.getParameterMap();
        Map<String, ?> requestedData = dataSuppliers.entrySet()
                .stream()
                .filter(e -> uriParameters.isEmpty() || uriParameters.containsKey(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
        String json = gson.toJson(requestedData);
        sendTextContent(response, json, CONTENT_TYPE_JSON);
        response.setStatus(SC_OK);
        baseRequest.setHandled(true);
    }

    /**
     * Adds a supplier for data with the given name. The data will be published to {@code /GRIP/data} on the internal
     * HTTP server.
     *
     * @param name     the name of the data
     * @param supplier a supplier for the data
     */
    public void addDataSupplier(String name, Supplier<?> supplier) {
        checkNotNull(name, "name");
        checkNotNull(supplier, "supplier");
        dataSuppliers.put(name, supplier);
    }

    /**
     * Removes the supplier for data with the given name. Will do nothing if no such data exists,
     * or if {@code name} is null.
     *
     * @param name the name of the data to remove
     */
    public void removeDataSupplier(@Nullable String name) {
        dataSuppliers.remove(name);
    }

    @Subscribe
    public void onPipelineStart(@Nullable RunStartedEvent e) {
        staleData.set(true);
    }

    @Subscribe
    public void onPipelineStop(@Nullable RunStoppedEvent e) {
        staleData.set(false);
    }
}
