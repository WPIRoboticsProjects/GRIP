package edu.wpi.grip.core.sources;


import edu.wpi.grip.core.Source;

import java.io.IOException;

public abstract class LoadableSource extends Source {

    /**
     * Loads the source's data from the filesystem
     * @throws IOException
     */
    public abstract void load() throws IOException;
}
