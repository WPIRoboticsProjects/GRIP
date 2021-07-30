package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.opencv_core.Formatter;

public enum FormatterEnum {

  FMT_DEFAULT(Formatter.FMT_DEFAULT), FMT_MATLAB(Formatter.FMT_MATLAB), FMT_CSV(Formatter.FMT_CSV), FMT_PYTHON(Formatter.FMT_PYTHON), FMT_NUMPY(Formatter.FMT_NUMPY), FMT_C(Formatter.FMT_C);

  public final int value;

  FormatterEnum(int value) {
    this.value = value;
  }
}
