package edu.wpi.grip.ui.util;

import com.google.common.base.StandardSystemProperty;
import javafx.stage.Screen;

/**
 * Utilities for determining pixel sizes in a way that should work well across operating systems and screen resolutions
 */
public class DPIUtility {

    private final static double HIDPI_SCALE = 2.0;

    public final static double FONT_SIZE = 11.0 * (isManualHiDPI() ? HIDPI_SCALE : 1.0);
    public final static double MINI_ICON_SIZE = 8.0 * (isManualHiDPI() ? HIDPI_SCALE : 1.0);
    public final static double SMALL_ICON_SIZE = 16.0 * (isManualHiDPI() ? HIDPI_SCALE : 1.0);
    public final static double LARGE_ICON_SIZE = 48.0 * (isManualHiDPI() ? HIDPI_SCALE : 1.0);
    public final static double STROKE_WIDTH = 2.0 * (isManualHiDPI() ? HIDPI_SCALE : 1.0);
    public final static double SETTINGS_DIALOG_SIZE = 400.0 * (isManualHiDPI() ? HIDPI_SCALE : 1.0);

    private static boolean isManualHiDPI() {
        // We need to do manual size adjustments for HiDPI on Linux.  JavaFX automatically does this on Windows and OSX
        final String osName = StandardSystemProperty.OS_NAME.value();
        return Screen.getPrimary().getDpi() >= 192.0 && "Linux".equals(osName);
    }
}
