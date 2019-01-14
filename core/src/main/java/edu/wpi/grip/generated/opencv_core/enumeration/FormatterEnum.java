package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum FormatterEnum {

    FMT_DEFAULT(opencv_core.Formatter.FMT_DEFAULT), FMT_MATLAB(opencv_core.Formatter.FMT_MATLAB), FMT_CSV(opencv_core.Formatter.FMT_CSV), FMT_PYTHON(opencv_core.Formatter.FMT_PYTHON), FMT_NUMPY(opencv_core.Formatter.FMT_NUMPY), FMT_C(opencv_core.Formatter.FMT_C);

    public final int value;

    FormatterEnum(int value) {
        this.value = value;
    }
}
