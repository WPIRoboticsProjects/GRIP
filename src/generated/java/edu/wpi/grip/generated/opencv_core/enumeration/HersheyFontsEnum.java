package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum HersheyFontsEnum {

    /** normal size sans-serif font */
    FONT_HERSHEY_SIMPLEX(opencv_core.FONT_HERSHEY_SIMPLEX), /** small size sans-serif font */
    FONT_HERSHEY_PLAIN(opencv_core.FONT_HERSHEY_PLAIN), /** normal size sans-serif font (more complex than FONT_HERSHEY_SIMPLEX) */
    FONT_HERSHEY_DUPLEX(opencv_core.FONT_HERSHEY_DUPLEX), /** normal size serif font */
    FONT_HERSHEY_COMPLEX(opencv_core.FONT_HERSHEY_COMPLEX), /** normal size serif font (more complex than FONT_HERSHEY_COMPLEX) */
    FONT_HERSHEY_TRIPLEX(opencv_core.FONT_HERSHEY_TRIPLEX), /** smaller version of FONT_HERSHEY_COMPLEX */
    FONT_HERSHEY_COMPLEX_SMALL(opencv_core.FONT_HERSHEY_COMPLEX_SMALL), /** hand-writing style font */
    FONT_HERSHEY_SCRIPT_SIMPLEX(opencv_core.FONT_HERSHEY_SCRIPT_SIMPLEX), /** more complex variant of FONT_HERSHEY_SCRIPT_SIMPLEX */
    FONT_HERSHEY_SCRIPT_COMPLEX(opencv_core.FONT_HERSHEY_SCRIPT_COMPLEX), /** flag for italic font */
    FONT_ITALIC(opencv_core.FONT_ITALIC);

    public final int value;

    HersheyFontsEnum(int value) {
        this.value = value;
    }
}
