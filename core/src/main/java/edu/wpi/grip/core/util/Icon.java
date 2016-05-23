package edu.wpi.grip.core.util;

import java.io.InputStream;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class for fetching icon streams.
 */
public final class Icon {
    private final Supplier<InputStream> streamSupplier;

    private Icon(Supplier<InputStream> streamSupplier) {
        this.streamSupplier = streamSupplier;
    }

    /**
     * Get a newly constructed stream.
     */
    public InputStream getStream() {
        return streamSupplier.get();
    }

    /**
     * Gets an image stream for an icon.
     *
     * @param path the directory where the icon is located
     * @param name the name of the icon
     * @param type the type of the icon (".png", ".jpg", etc.)
     * @return a stream for the given icon, or {@code null} if no image by that name exists
     */
    public static Icon iconStream(String path, String name, String type) {
        checkNotNull(path);
        checkNotNull(name);
        checkNotNull(type);
        return new Icon(() -> Icon.class.getResourceAsStream(path + name + type));
    }


    /**
     * Gets an image stream for an icon.
     * Will look for the image at <code>/edu/wpi/grip/ui/icons/{name}.{type}</code>
     *
     * @param name the name of the icon
     * @param type the type of the icon (".png", ".jpg", etc.)
     * @return a stream for the given icon, or {@code null} if no image by that name exists
     */
    public static Icon iconStream(String name, String type) {
        return iconStream("/edu/wpi/grip/ui/icons/", name, type);
    }

    /**
     * Gets an image stream for an icon.
     * Will look for the image at <code>/edu/wpi/grip/ui/icons/{name}.png</code>
     *
     * @param name the name of the icon
     * @return a stream for the given icon, or {@code null} if no image by that name exists
     */
    public static Icon iconStream(String name) {
        return iconStream(name, ".png");
    }

}
