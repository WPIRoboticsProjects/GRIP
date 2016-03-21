
package edu.wpi.grip.core.http;

import java.util.List;
import java.util.Map;

/**
 * Handler for creating responses to incoming HTTP {@code GET} requests.
 */
public interface GetHandler {

    public String createResponse(Map<String, List<String>> requestParameters);
}
