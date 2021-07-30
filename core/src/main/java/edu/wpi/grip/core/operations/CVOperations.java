package edu.wpi.grip.core.operations;

import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.operations.opencv.CVOperation;
import edu.wpi.grip.core.operations.opencv.enumeration.FlipCode;
import edu.wpi.grip.core.operations.templated.TemplateFactory;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.generated.opencv_core.enumeration.BorderTypesEnum;
import edu.wpi.grip.generated.opencv_core.enumeration.CmpTypesEnum;
import edu.wpi.grip.generated.opencv_core.enumeration.LineTypesEnum;
import edu.wpi.grip.generated.opencv_imgproc.enumeration.AdaptiveThresholdTypesEnum;
import edu.wpi.grip.generated.opencv_imgproc.enumeration.ColorConversionCodesEnum;
import edu.wpi.grip.generated.opencv_imgproc.enumeration.ColormapTypesEnum;
import edu.wpi.grip.generated.opencv_imgproc.enumeration.InterpolationFlagsEnum;
import edu.wpi.grip.generated.opencv_imgproc.enumeration.ThresholdTypesEnum;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import org.bytedeco.opencv.global.opencv_cudaarithm;
import org.bytedeco.opencv.global.opencv_cudaimgproc;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_cudafilters.Filter;

import static org.bytedeco.opencv.global.opencv_core.absdiff;
import static org.bytedeco.opencv.global.opencv_core.add;
import static org.bytedeco.opencv.global.opencv_core.addWeighted;
import static org.bytedeco.opencv.global.opencv_core.bitwise_and;
import static org.bytedeco.opencv.global.opencv_core.bitwise_not;
import static org.bytedeco.opencv.global.opencv_core.bitwise_or;
import static org.bytedeco.opencv.global.opencv_core.bitwise_xor;
import static org.bytedeco.opencv.global.opencv_core.compare;
import static org.bytedeco.opencv.global.opencv_core.divide;
import static org.bytedeco.opencv.global.opencv_core.extractChannel;
import static org.bytedeco.opencv.global.opencv_core.flip;
import static org.bytedeco.opencv.global.opencv_core.max;
import static org.bytedeco.opencv.global.opencv_core.min;
import static org.bytedeco.opencv.global.opencv_core.multiply;
import static org.bytedeco.opencv.global.opencv_core.scaleAdd;
import static org.bytedeco.opencv.global.opencv_core.subtract;
import static org.bytedeco.opencv.global.opencv_core.transpose;
import static org.bytedeco.opencv.global.opencv_cudafilters.createSobelFilter;
import static org.bytedeco.opencv.global.opencv_imgproc.GaussianBlur;
import static org.bytedeco.opencv.global.opencv_imgproc.Laplacian;
import static org.bytedeco.opencv.global.opencv_imgproc.Sobel;
import static org.bytedeco.opencv.global.opencv_imgproc.adaptiveThreshold;
import static org.bytedeco.opencv.global.opencv_imgproc.applyColorMap;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.dilate;
import static org.bytedeco.opencv.global.opencv_imgproc.medianBlur;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;
import static org.bytedeco.opencv.global.opencv_imgproc.threshold;

/**
 * A list of all of the raw opencv operations.
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "CodeBlock2Expr"})
public class CVOperations {

  private final EventBus eventBus;
  private final ImmutableList<OperationMetaData> coreOperations;
  private final ImmutableList<OperationMetaData> imgprocOperation;

  @Inject
  @SuppressWarnings("PMD.ExcessiveMethodLength")
  CVOperations(EventBus eventBus, InputSocket.Factory isf, OutputSocket.Factory osf) {
    this.eventBus = eventBus;
    final TemplateFactory templateFactory = new TemplateFactory(isf, osf);
    this.coreOperations = ImmutableList.of(
        new OperationMetaData(CVOperation.defaults("CV absdiff",
            "Calculate the per-element absolute difference of two images."),
            templateFactory.createAllMatTwoSourceCuda((src1, src2, useCuda, dst) -> {
              if (useCuda) {
                opencv_cudaarithm.absdiff(src1.getGpu(), src2.getGpu(), dst.rawGpu());
              } else {
                absdiff(src1.getCpu(), src2.getCpu(), dst.rawCpu());
              }
            })),

        new OperationMetaData(CVOperation.defaults("CV add",
            "Calculate the per-pixel sum of two images."),
            templateFactory.createAllMatTwoSourceCuda((src1, src2, useCuda, dst) -> {
              if (useCuda) {
                opencv_cudaarithm.add(src1.getGpu(), src2.getGpu(), dst.rawGpu());
              } else {
                add(src1.getCpu(), src2.getCpu(), dst.rawCpu());
              }
            })),

        new OperationMetaData(CVOperation.defaults("CV addWeighted",
            "Calculate the weighted sum of two images."),
            templateFactory.createCuda(
                SocketHints.createImageSocketHint("src1"),
                SocketHints.Inputs.createNumberSpinnerSocketHint("alpha", 0),
                SocketHints.createImageSocketHint("src2"),
                SocketHints.Inputs.createNumberSpinnerSocketHint("beta", 0),
                SocketHints.Inputs.createNumberSpinnerSocketHint("gamma", 0),
                SocketHints.createImageSocketHint("dst"),
                (src1, alpha, src2, beta, gamma, useCuda, dst) -> {
                  if (useCuda) {
                    opencv_cudaarithm.addWeighted(src1.getGpu(), alpha.doubleValue(), src2.getGpu(),
                        beta.doubleValue(), gamma.doubleValue(), dst.rawGpu());
                  } else {
                    addWeighted(src1.getCpu(), alpha.doubleValue(), src2.getCpu(),
                        beta.doubleValue(), gamma.doubleValue(), dst.rawCpu());
                  }
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV bitwise_and",
            "Calculate the per-element bitwise conjunction of two images."),
            templateFactory.createAllMatTwoSourceCuda((src1, src2, useCuda, dst) -> {
              if (useCuda) {
                opencv_cudaarithm.bitwise_and(src1.getGpu(), src2.getGpu(), dst.rawGpu());
              } else {
                bitwise_and(src1.getCpu(), src2.getCpu(), dst.rawCpu());
              }
            })),

        new OperationMetaData(CVOperation.defaults("CV bitwise_not",
            "Calculate per-element bit-wise inversion of an image."),
            templateFactory.createAllMatOneSourceCuda((src, useCuda, dst) -> {
              if (useCuda) {
                opencv_cudaarithm.bitwise_not(src.getGpu(), dst.rawGpu());
              } else {
                bitwise_not(src.getCpu(), dst.rawCpu());
              }
            })),

        new OperationMetaData(CVOperation.defaults("CV bitwise_or",
            "Calculate the per-element bit-wise disjunction of two images."),
            templateFactory.createAllMatTwoSourceCuda((src1, src2, useCuda, dst) -> {
              if (useCuda) {
                opencv_cudaarithm.bitwise_or(src1.getGpu(), src2.getGpu(), dst.rawGpu());
              } else {
                bitwise_or(src1.getCpu(), src2.getCpu(), dst.rawCpu());
              }
            })),

        new OperationMetaData(CVOperation.defaults("CV bitwise_xor",
            "Calculate the per-element bit-wise \"exclusive or\" on two images."),
            templateFactory.createAllMatTwoSourceCuda((src1, src2, useCuda, dst) -> {
              if (useCuda) {
                opencv_cudaarithm.bitwise_xor(src1.getGpu(), src2.getGpu(), dst.rawGpu());
              } else {
                bitwise_xor(src1.getCpu(), src2.getCpu(), dst.rawCpu());
              }
            })),

        new OperationMetaData(CVOperation.defaults("CV compare",
            "Compare each pixel in two images using a given rule."),
            templateFactory.createCuda(
                SocketHints.createImageSocketHint("src1"),
                SocketHints.createImageSocketHint("src2"),
                SocketHints.createEnumSocketHint("cmpop", CmpTypesEnum.CMP_EQ),
                SocketHints.createImageSocketHint("dst"),
                (src1, src2, cmp, useCuda, dst) -> {
                  int cmpop = cmp.value;
                  if (useCuda) {
                    opencv_cudaarithm.compare(src1.getGpu(), src2.getGpu(), dst.rawGpu(), cmpop);
                  } else {
                    compare(src1.getCpu(), src2.getCpu(), dst.rawCpu(), cmpop);
                  }
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV divide",
            "Perform per-pixel division of two images."),
            templateFactory.create(
                SocketHints.createImageSocketHint("src1"),
                SocketHints.createImageSocketHint("src2"),
                SocketHints.Inputs.createNumberSpinnerSocketHint("scale", 1.0, -Double.MAX_VALUE,
                    Double.MAX_VALUE),
                SocketHints.createImageSocketHint("dst"),
                (src1, src2, scale, dst) -> {
                  divide(src1.getCpu(), src2.getCpu(), dst.rawCpu(), scale.doubleValue(), -1);
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV extractChannel",
            "Extract a single channel from a image."),
            templateFactory.create(
                SocketHints.createImageSocketHint("src"),
                SocketHints.Inputs.createNumberSpinnerSocketHint("channel", 0, 0, Integer
                    .MAX_VALUE),
                SocketHints.createImageSocketHint("dst"),
                (src1, coi, dst) -> {
                  extractChannel(src1.getCpu(), dst.rawCpu(), coi.intValue());
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV flip",
            "Flip image around vertical, horizontal, or both axes."),
            templateFactory.create(
                SocketHints.createImageSocketHint("src"),
                SocketHints.createEnumSocketHint("flipCode", FlipCode.Y_AXIS),
                SocketHints.createImageSocketHint("dst"),
                (src, flipCode, dst) -> {
                  flip(src.getCpu(), dst.rawCpu(), flipCode.value);
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV max",
            "Calculate per-element maximum of two images."),
            templateFactory.createAllMatTwoSource((src1, src2, dst) -> {
              max(src1.getCpu(), src2.getCpu(), dst.rawCpu());
            })),

        new OperationMetaData(CVOperation.defaults("CV min",
            "Calculate the per-element minimum of two images."),
            templateFactory.createAllMatTwoSource((src1, src2, dst) -> {
              min(src1.getCpu(), src2.getCpu(), dst.rawCpu());
            })),

        new OperationMetaData(CVOperation.defaults("CV multiply",
            "Calculate the per-pixel scaled product of two images."),
            templateFactory.create(
                SocketHints.createImageSocketHint("src1"),
                SocketHints.createImageSocketHint("src2"),
                SocketHints.Inputs.createNumberSpinnerSocketHint("scale", 1.0, Integer.MIN_VALUE,
                    Integer.MAX_VALUE),
                SocketHints.createImageSocketHint("dst"),
                (src1, src2, scale, dst) -> {
                  multiply(src1.getCpu(), src2.getCpu(), dst.getCpu(), scale.doubleValue(), -1);
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV scaleAdd",
            "Calculate the sum of two images where one image is multiplied by a scalar."),
            templateFactory.create(
                SocketHints.createImageSocketHint("src1"),
                SocketHints.Inputs.createNumberSpinnerSocketHint("scale", 1.0),
                SocketHints.createImageSocketHint("src2"),
                SocketHints.createImageSocketHint("dst"),
                (src1, alpha, src2, dst) -> {
                  scaleAdd(src1.getCpu(), alpha.doubleValue(), src2.getCpu(), dst.rawCpu());
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV subtract",
            "Calculate the per-pixel difference between two images."),
            templateFactory.createAllMatTwoSourceCuda((src1, src2, useCuda, dst) -> {
              if (useCuda) {
                opencv_cudaarithm.subtract(src1.getGpu(), src2.getGpu(), dst.rawGpu());
              } else {
                subtract(src1.getCpu(), src2.getCpu(), dst.rawCpu());
              }
            })),

        new OperationMetaData(CVOperation.defaults("CV transpose",
            "Calculate the transpose of an image."),
            templateFactory.createAllMatOneSource((src, dst) -> {
              transpose(src.getCpu(), dst.rawCpu());
            }))
    );

    this.imgprocOperation = ImmutableList.of(
        new OperationMetaData(CVOperation.defaults("CV adaptiveThreshold",
            "Transforms a grayscale image to a binary image)."),
            templateFactory.create(
                SocketHints.createImageSocketHint("src"),
                SocketHints.Inputs.createNumberSpinnerSocketHint("maxValue", 0.0),
                SocketHints.createEnumSocketHint("adaptiveMethod",
                    AdaptiveThresholdTypesEnum.ADAPTIVE_THRESH_MEAN_C),
                SocketHints.createEnumSocketHint("thresholdType",
                    CVAdaptThresholdTypesEnum.THRESH_BINARY),
                SocketHints.Inputs.createNumberSpinnerSocketHint("blockSize", 0.0),
                SocketHints.Inputs.createNumberSpinnerSocketHint("C", 0.0),
                SocketHints.createImageSocketHint("dst"),
                (src, maxValue, adaptiveMethod, thresholdType, blockSize, c, dst) -> {
                  adaptiveThreshold(src.getCpu(), dst.rawCpu(), maxValue.doubleValue(),
                      adaptiveMethod.value, thresholdType.value, blockSize.intValue(), c
                          .doubleValue());
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV applyColorMap",
            "Apply a MATLAB equivalent colormap to an image."),
            templateFactory.create(
                SocketHints.createImageSocketHint("src"),
                SocketHints.createEnumSocketHint("colormap", ColormapTypesEnum.COLORMAP_AUTUMN),
                SocketHints.createImageSocketHint("dst"),
                (src, colormap, dst) -> {
                  applyColorMap(src.getCpu(), dst.rawCpu(), colormap.value);
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV cvtColor",
            "Convert an image from one color space to another."),
            templateFactory.createCuda(
                SocketHints.createImageSocketHint("src"),
                SocketHints.createEnumSocketHint("code", ColorConversionCodesEnum.COLOR_BGR2BGRA),
                SocketHints.createImageSocketHint("dst"),
                (src, code, useCuda, dst) -> {
                  if (useCuda) {
                    opencv_cudaimgproc.cvtColor(dst.getGpu(), dst.rawGpu(), code.value);
                  } else {
                    cvtColor(src.getCpu(), dst.rawCpu(), code.value);
                  }
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV dilate",
            "Expands areas of higher values in an image."),
            templateFactory.create(
                SocketHints.createImageSocketHint("src"),
                SocketHints.createImageSocketHint("kernel"),
                new SocketHint.Builder<>(Point.class).identifier("anchor").initialValueSupplier(
                    () -> new Point(-1, -1)).build(),
                SocketHints.Inputs.createNumberSpinnerSocketHint("iterations", 1),
                SocketHints.createEnumSocketHint("borderType", BorderTypesEnum.BORDER_CONSTANT),
                new SocketHint.Builder<>(Scalar.class).identifier("borderValue")
                    .initialValueSupplier(opencv_imgproc::morphologyDefaultBorderValue).build(),
                SocketHints.createImageSocketHint("dst"),
                (src, kernel, anchor, iterations, borderType, borderValue, dst) -> {
                  dilate(src.getCpu(), dst.rawCpu(), kernel.getCpu(), anchor, iterations.intValue(),
                      borderType.value, borderValue);
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV erode",
            "Expands areas of lower values in an image."),
            templateFactory.create(
                SocketHints.createImageSocketHint("src"),
                SocketHints.createImageSocketHint("kernel"),
                new SocketHint.Builder<>(Point.class).identifier("anchor").initialValueSupplier(
                    () -> new Point(-1, -1)).build(),
                SocketHints.Inputs.createNumberSpinnerSocketHint("iterations", 1),
                SocketHints.createEnumSocketHint("borderType", BorderTypesEnum.BORDER_CONSTANT),
                new SocketHint.Builder<>(Scalar.class).identifier("borderValue")
                    .initialValueSupplier(opencv_imgproc::morphologyDefaultBorderValue).build(),
                SocketHints.createImageSocketHint("dst"),
                (src, kernel, anchor, iterations, borderType, borderValue, dst) -> {
                  opencv_imgproc.erode(src.getCpu(), dst.rawCpu(), kernel.getCpu(), anchor,
                      iterations.intValue(), borderType.value, borderValue);
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV GaussianBlur",
            "Apply a Gaussian blur to an image."),
            templateFactory.create(
                SocketHints.createImageSocketHint("src"),
                new SocketHint.Builder<>(Size.class).identifier("ksize").initialValueSupplier(()
                    -> new Size(1, 1)).build(),
                SocketHints.Inputs.createNumberSpinnerSocketHint("sigmaX", 0.0),
                SocketHints.Inputs.createNumberSpinnerSocketHint("sigmaY", 0.0),
                SocketHints.createEnumSocketHint("borderType", CVBorderTypesEnum.BORDER_DEFAULT),
                SocketHints.createImageSocketHint("dst"),
                (src, ksize, sigmaX, sigmaY, borderType, dst) -> {
                  GaussianBlur(src.getCpu(), dst.rawCpu(), ksize, sigmaX.doubleValue(), sigmaY
                      .doubleValue(), borderType.value);
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV Laplacian",
            "Find edges by calculating the Laplacian for the given image."),
            templateFactory.create(
                SocketHints.createImageSocketHint("src"),
                SocketHints.Inputs.createNumberSpinnerSocketHint("ksize", 1),
                SocketHints.Inputs.createNumberSpinnerSocketHint("scale", 1.0),
                SocketHints.Inputs.createNumberSpinnerSocketHint("delta", 0.0),
                SocketHints.createEnumSocketHint("borderType", BorderTypesEnum.BORDER_DEFAULT),
                SocketHints.createImageSocketHint("dst"),
                (src, ksize, scale, delta, borderType, dst) -> {
                  Laplacian(src.getCpu(), dst.rawCpu(), 0, ksize.intValue(), scale.doubleValue(),
                      delta.doubleValue(), borderType.value);
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV medianBlur",
            "Apply a Median blur to an image."),
            templateFactory.create(
                SocketHints.createImageSocketHint("src"),
                SocketHints.Inputs.createNumberSpinnerSocketHint("ksize", 1, 1, Integer.MAX_VALUE),
                SocketHints.createImageSocketHint("dst"),
                (src, ksize, dst) -> {
                  medianBlur(src.getCpu(), dst.rawCpu(), ksize.intValue());
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV rectangle",
            "Draw a rectangle (outline or filled) on an image."),
            templateFactory.create(
                SocketHints.createImageSocketHint("src"),
                SocketHints.Inputs.createPointSocketHint("pt1", 0, 0),
                SocketHints.Inputs.createPointSocketHint("pt2", 0, 0),
                new SocketHint.Builder<>(Scalar.class).identifier("color").initialValueSupplier(
                    () -> Scalar.BLACK).build(),
                SocketHints.Inputs.createNumberSpinnerSocketHint("thickness", 0, Integer
                    .MIN_VALUE, Integer.MAX_VALUE),
                SocketHints.createEnumSocketHint("lineType", LineTypesEnum.LINE_8),
                SocketHints.Inputs.createNumberSpinnerSocketHint("shift", 0),
                SocketHints.createImageSocketHint("dst"),
                (src, pt1, pt2, color, thickness, lineType, shift, dst) -> {
                  // Rectangle only has one input and it modifies it so we have to copy the input
                  // image to the dst
                  src.copyTo(dst);
                  rectangle(dst.rawCpu(), pt1, pt2, color, thickness.intValue(), lineType
                      .value, shift.intValue());
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV resize",
            "Resizes the image to the specified size."),
            templateFactory.create(
                SocketHints.createImageSocketHint("src"),
                new SocketHint.Builder<>(Size.class).identifier("dsize").initialValueSupplier(()
                    -> new Size(0, 0)).build(),
                SocketHints.Inputs.createNumberSpinnerSocketHint("fx", .25), SocketHints.Inputs
                    .createNumberSpinnerSocketHint("fy", .25),
                SocketHints.createEnumSocketHint("interpolation", InterpolationFlagsEnum
                    .INTER_LINEAR),
                SocketHints.createImageSocketHint("dst"),
                (src, dsize, fx, fy, interpolation, dst) -> {
                  resize(src.getCpu(), dst.rawCpu(), dsize, fx.doubleValue(), fy.doubleValue(),
                      interpolation.value);
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV Sobel",
            "Find edges by calculating the requested derivative order for the given image."),
            templateFactory.createCuda(
                SocketHints.createImageSocketHint("src"),
                SocketHints.Inputs.createNumberSpinnerSocketHint("dx", 0),
                SocketHints.Inputs.createNumberSpinnerSocketHint("dy", 0),
                SocketHints.Inputs.createNumberSpinnerSocketHint("ksize", 3),
                SocketHints.Inputs.createNumberSpinnerSocketHint("scale", 1),
                SocketHints.Inputs.createNumberSpinnerSocketHint("delta", 0),
                SocketHints.createEnumSocketHint("borderType", BorderTypesEnum.BORDER_DEFAULT),
                SocketHints.createImageSocketHint("dst"),
                (src, dx, dy, ksize, scale, delta, borderType, useCuda, dst) -> {
                  if (useCuda) {
                    try (Filter sobelFilter = createSobelFilter(
                        src.type(),
                        src.type(),
                        dx.intValue(),
                        dy.intValue(),
                        ksize.intValue(),
                        scale.doubleValue(),
                        borderType.value,
                        borderType.value)) {
                      sobelFilter.apply(src.getGpu(), dst.rawGpu());
                    }
                  } else {
                    Sobel(src.getCpu(), dst.rawCpu(), 0, dx.intValue(), dy.intValue(),
                        ksize.intValue(), scale.doubleValue(), delta.doubleValue(),
                        borderType.value);
                  }
                }
            )),

        new OperationMetaData(CVOperation.defaults("CV Threshold",
            "Apply a fixed-level threshold to each array element in an image.",
            "CV threshold"),
            templateFactory.createCuda(
                SocketHints.createImageSocketHint("src"),
                SocketHints.Inputs.createNumberSpinnerSocketHint("thresh", 0),
                SocketHints.Inputs.createNumberSpinnerSocketHint("maxval", 0),
                SocketHints.createEnumSocketHint("type", CVThresholdTypesEnum.THRESH_BINARY),
                SocketHints.createImageSocketHint("dst"),
                (src, thresh, maxval, type, useCuda, dst) -> {
                  if (useCuda) {
                    opencv_cudaarithm.threshold(
                        src.getGpu(),
                        dst.rawGpu(),
                        thresh.doubleValue(),
                        maxval.doubleValue(),
                        type.value
                    );
                  } else {
                    threshold(src.getCpu(), dst.rawCpu(), thresh.doubleValue(),
                        maxval.doubleValue(), type.value);
                  }
                }
            ))
    );
  }

  public enum CVThresholdTypesEnum {
    THRESH_BINARY(ThresholdTypesEnum.THRESH_BINARY.value),
    THRESH_BINARY_INV(ThresholdTypesEnum.THRESH_BINARY_INV.value),
    THRESH_TRUNC(ThresholdTypesEnum.THRESH_TRUNC.value),
    THRESH_TOZERO(ThresholdTypesEnum.THRESH_TOZERO.value),
    THRESH_TOZERO_INV(ThresholdTypesEnum.THRESH_TOZERO_INV.value),
    THRESH_OTSU(ThresholdTypesEnum.THRESH_OTSU.value),
    THRESH_TRIANGLE(ThresholdTypesEnum.THRESH_TRIANGLE.value);

    public final int value;

    CVThresholdTypesEnum(int value) {
      this.value = value;
    }
  }

  public enum CVAdaptThresholdTypesEnum {
    THRESH_BINARY(ThresholdTypesEnum.THRESH_BINARY.value),
    THRESH_BINARY_INV(ThresholdTypesEnum.THRESH_BINARY_INV.value);

    public final int value;

    CVAdaptThresholdTypesEnum(int value) {
      this.value = value;
    }
  }

  public enum CVBorderTypesEnum {

    BORDER_CONSTANT(BorderTypesEnum.BORDER_CONSTANT.value),
    R_REPLICATE(BorderTypesEnum.BORDER_REPLICATE.value),
    BORDER_REFLECT(BorderTypesEnum.BORDER_REFLECT.value),
    BORDER_REFLECT_101(BorderTypesEnum.BORDER_REFLECT_101.value),
    BORDER_REFLECT101(BorderTypesEnum.BORDER_REFLECT101.value),
    BORDER_DEFAULT(BorderTypesEnum.BORDER_DEFAULT.value),
    BORDER_ISOLATED(BorderTypesEnum.BORDER_ISOLATED.value);

    public final int value;

    CVBorderTypesEnum(int value) {
      this.value = value;
    }
  }


  /**
   * All of the operations that this list supplies.
   */
  @VisibleForTesting
  ImmutableList<OperationMetaData> operations() {
    return ImmutableList.<OperationMetaData>builder().addAll(coreOperations)
        .addAll(imgprocOperation).build();
  }

  /**
   * Submits all operations for addition on the {@link EventBus}.
   */
  public void addOperations() {
    coreOperations.stream()
        .map(OperationAddedEvent::new)
        .forEach(eventBus::post);
    imgprocOperation.stream()
        .map(OperationAddedEvent::new)
        .forEach(eventBus::post);
  }
}
