
package edu.wpi.grip.core.sources;

import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.Source;
import java.io.IOException;
import java.util.Properties;

/**
 * Provides a way to generate a constantly updated {@link Mat} from an HTTP
 * server.
 */
public class HttpSource extends Source {

    public HttpSource() {
        super(null);
    }

    @Override
    public String getName() {
        return "HTTP source";
    }

    @Override
    protected OutputSocket[] createOutputSockets() {
        return null;
    }

    @Override
    protected boolean updateOutputSockets() {
        return false;
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public void initialize() throws IOException {
    }

}
