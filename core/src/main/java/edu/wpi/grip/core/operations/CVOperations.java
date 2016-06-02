package edu.wpi.grip.core.operations;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
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
import edu.wpi.grip.generated.opencv_imgproc.enumeration.*;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_imgproc;

/**
 * A list of all of the raw opencv operations
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class CVOperations {
    private final EventBus eventBus;
    private final ImmutableList<OperationMetaData> coreOperations;
    private final ImmutableList<OperationMetaData> imgprocOperation;

    @Inject
    CVOperations(EventBus eventBus, InputSocket.Factory isf, OutputSocket.Factory osf) {
        this.eventBus = eventBus;
        final TemplateFactory templateFactory = new TemplateFactory(isf, osf);
        this.coreOperations = ImmutableList.of(
                new OperationMetaData(CVOperation.defaults("CV absdiff", "Calculate the per-element absolute difference of two images."),
                        templateFactory.createAllMatTwoSource(opencv_core::absdiff)),

                new OperationMetaData(CVOperation.defaults("CV add", "Calculate the per-pixel sum of two images."),
                        templateFactory.createAllMatTwoSource(opencv_core::add)),

                new OperationMetaData(CVOperation.defaults("CV addWeighted", "Calculate the weighted sum of two images."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src1", false),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("alpha", 0),
                                SocketHints.Inputs.createMatSocketHint("src2", false),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("beta", 0),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("gamma", 0),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src1, alpha, src2, beta, gamma, dst) -> {
                                    opencv_core.addWeighted(src1, alpha.doubleValue(), src2, beta.doubleValue(), gamma.doubleValue(), dst);
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV bitwise_and", "Calculate the per-element bitwise conjunction of two images."),
                        templateFactory.createAllMatTwoSource(opencv_core::bitwise_and)),

                new OperationMetaData(CVOperation.defaults("CV bitwise_not", "Calculate per-element bit-wise inversion of an image."),
                        templateFactory.createAllMatOneSource(opencv_core::bitwise_not)),

                new OperationMetaData(CVOperation.defaults("CV bitwise_or", "Calculate the per-element bit-wise disjunction of two images."),
                        templateFactory.createAllMatTwoSource(opencv_core::bitwise_or)),

                new OperationMetaData(CVOperation.defaults("CV bitwise_xor", "Calculate the per-element bit-wise \"exclusive or\" on two images."),
                        templateFactory.createAllMatTwoSource(opencv_core::bitwise_xor)),

                new OperationMetaData(CVOperation.defaults("CV compare", "Compare each pixel in two images using a given rule."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src1", false),
                                SocketHints.Inputs.createMatSocketHint("src2", false),
                                SocketHints.createEnumSocketHint("cmpop", CmpTypesEnum.CMP_EQ),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src1, src2, cmp, dst) -> {
                                    opencv_core.compare(src1, src2, dst, cmp.value);
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV divide", "Perform per-pixel division of two images."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src1", false),
                                SocketHints.Inputs.createMatSocketHint("src2", false),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("scale", 1.0, -Double.MAX_VALUE, Double.MAX_VALUE),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src1, src2, scale, dst) -> {
                                    opencv_core.divide(src1, src2, dst, scale.doubleValue(), -1);
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV extractChannel", "Extract a single channel from a image."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src", false),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("channel", 0, 0, Integer.MAX_VALUE),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src1, coi, dst) -> {
                                    opencv_core.extractChannel(src1, dst, coi.intValue());
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV flip", "Flip image around vertical, horizontal, or both axes."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src", false),
                                SocketHints.createEnumSocketHint("flipCode", FlipCode.Y_AXIS),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src, flipCode, dst) -> {
                                    opencv_core.flip(src, dst, flipCode.value);
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV max", "Calculate per-element maximum of two images."),
                        templateFactory.createAllMatTwoSource(opencv_core::max)),

                new OperationMetaData(CVOperation.defaults("CV min", "Calculate the per-element minimum of two images."),
                        templateFactory.createAllMatTwoSource(opencv_core::min)),

                new OperationMetaData(CVOperation.defaults("CV multiply", "Calculate the per-pixel scaled product of two images."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src1", false),
                                SocketHints.Inputs.createMatSocketHint("src2", false),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("scale", 1.0, Integer.MIN_VALUE, Integer.MAX_VALUE),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src1, src2, scale, dst) -> {
                                    opencv_core.multiply(src1, src2, dst, scale.doubleValue(), -1);
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV scaleAdd", "Calculate the sum of two images where one image is multiplied by a scalar."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src1", false),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("scale", 1.0),
                                SocketHints.Inputs.createMatSocketHint("src2", false),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src1, alpha, src2, dst) -> {
                                    opencv_core.scaleAdd(src1, alpha.doubleValue(), src2, dst);
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV subtract", "Calculate the per-pixel difference between two images."),
                        templateFactory.createAllMatTwoSource(opencv_core::subtract)),

                new OperationMetaData(CVOperation.defaults("CV transpose", "Calculate the transpose of an image."),
                        templateFactory.createAllMatOneSource(opencv_core::transpose))
        );

        this.imgprocOperation = ImmutableList.of(
                new OperationMetaData(CVOperation.defaults("CV adaptiveThreshold", "Transforms a grayscale image to a binary image)."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src", false),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("maxValue", 0.0),
                                SocketHints.createEnumSocketHint("adaptiveMethod", AdaptiveThresholdTypesEnum.ADAPTIVE_THRESH_MEAN_C),
                                SocketHints.createEnumSocketHint("thresholdType", ThresholdTypesEnum.THRESH_BINARY),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("blockSize", 0.0),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("C", 0.0),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src, maxValue, adaptiveMethod, thresholdType, blockSize, C, dst) -> {
                                    opencv_imgproc.adaptiveThreshold(src, dst, maxValue.doubleValue(), adaptiveMethod.value, thresholdType.value, blockSize.intValue(), C.doubleValue());
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV applyColorMap", "Apply a MATLAB equivalent colormap to an image."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src", false),
                                SocketHints.createEnumSocketHint("colormap", ColormapTypesEnum.COLORMAP_AUTUMN),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src, colormap, dst) -> {
                                    opencv_imgproc.applyColorMap(src, dst, colormap.value);
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV Canny", "Apply a \"canny edge detection\" algorithm to an image."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("image", false),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("threshold1", 0.0),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("threshold2", 0.0),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("apertureSize", 3),
                                SocketHints.Inputs.createCheckboxSocketHint("L2gradient", false),
                                SocketHints.Outputs.createMatSocketHint("edges"),
                                (image, threshold1, threshold2, apertureSize, L2gradient, edges) -> {
                                    opencv_imgproc.Canny(image, edges, threshold1.doubleValue(), threshold2.doubleValue(), apertureSize.intValue(), L2gradient);
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV cvtColor", "Convert an image from one color space to another."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src", false),
                                SocketHints.createEnumSocketHint("code", ColorConversionCodesEnum.COLOR_BGR2BGRA),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src, code, dst) -> {
                                    opencv_imgproc.cvtColor(src, dst, code.value);
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV dilate", "Expands areas of higher values in an image."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src", false),
                                SocketHints.Inputs.createMatSocketHint("kernel", true),
                                new SocketHint.Builder<>(Point.class).identifier("anchor").initialValueSupplier(() -> new Point(-1, -1)).build(),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("iterations", 1),
                                SocketHints.createEnumSocketHint("borderType", BorderTypesEnum.BORDER_CONSTANT),
                                new SocketHint.Builder<>(Scalar.class).identifier("borderValue").initialValueSupplier(opencv_imgproc::morphologyDefaultBorderValue).build(),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src, kernel, anchor, iterations, borderType, borderValue, dst) -> {
                                    opencv_imgproc.dilate(src, dst, kernel, anchor, iterations.intValue(), borderType.value, borderValue);
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV erode", "Expands areas of lower values in an image."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src", false),
                                SocketHints.Inputs.createMatSocketHint("kernel", true),
                                new SocketHint.Builder<>(Point.class).identifier("anchor").initialValueSupplier(() -> new Point(-1, -1)).build(),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("iterations", 1),
                                SocketHints.createEnumSocketHint("borderType", BorderTypesEnum.BORDER_CONSTANT),
                                new SocketHint.Builder<>(Scalar.class).identifier("borderValue").initialValueSupplier(opencv_imgproc::morphologyDefaultBorderValue).build(),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src, kernel, anchor, iterations, borderType, borderValue, dst) -> {
                                    opencv_imgproc.erode(src, dst, kernel, anchor, iterations.intValue(), borderType.value, borderValue);
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV GaussianBlur", "Apply a Gaussian blur to an image."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src", true),
                                new SocketHint.Builder<>(Size.class).identifier("ksize").initialValueSupplier(() -> new Size(1, 1)).build(),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("sigmaX", 0.0),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("sigmaY", 0.0), SocketHints.createEnumSocketHint("borderType", BorderTypesEnum.BORDER_DEFAULT),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src, ksize, sigmaX, sigmaY, borderType, dst) -> {
                                    opencv_imgproc.GaussianBlur(src, dst, ksize, sigmaX.doubleValue(), sigmaY.doubleValue(), borderType.value);
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV Laplacian", "Find edges by calculating the Laplacian for the given image."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src", false),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("ksize", 1),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("scale", 1.0),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("delta", 0.0),
                                SocketHints.createEnumSocketHint("borderType", BorderTypesEnum.BORDER_DEFAULT),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src, ksize, scale, delta, borderType, dst) -> {
                                    opencv_imgproc.Laplacian(src, dst, 0, ksize.intValue(), scale.doubleValue(), delta.doubleValue(), borderType.value);
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV medianBlur", "Apply a Median blur to an image."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src", false),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("ksize", 1, 1, Integer.MAX_VALUE),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src, ksize, dst) -> {
                                    opencv_imgproc.medianBlur(src, dst, ksize.intValue());
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV rectangle", "Draw a rectangle (outline or filled) on an image."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src", false),
                                SocketHints.Inputs.createPointSocketHint("pt1", 0, 0),
                                SocketHints.Inputs.createPointSocketHint("pt2", 0, 0),
                                new SocketHint.Builder<>(Scalar.class).identifier("color").initialValueSupplier(() -> Scalar.BLACK).build(),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("thickness", 0, Integer.MIN_VALUE, Integer.MAX_VALUE),
                                SocketHints.createEnumSocketHint("lineType", LineTypesEnum.LINE_8),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("shift", 0),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src, pt1, pt2, color, thickness, lineType, shift, dst) -> {
                                    // Rectangle only has one input and it modifies it so we have to copy the input image to the dst
                                    src.copyTo(dst);
                                    opencv_imgproc.rectangle(dst, pt1, pt2, color, thickness.intValue(), lineType.value, shift.intValue());
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV resize", "Resizes the image to the specified size."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src", false),
                                new SocketHint.Builder<>(Size.class).identifier("dsize").initialValueSupplier(() -> new Size(0, 0)).build(),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("fx", .25), SocketHints.Inputs.createNumberSpinnerSocketHint("fy", .25),
                                SocketHints.createEnumSocketHint("interpolation", InterpolationFlagsEnum.INTER_LINEAR),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src, dsize, fx, fy, interpolation, dst) -> {
                                    opencv_imgproc.resize(src, dst, dsize, fx.doubleValue(), fy.doubleValue(), interpolation.value);
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV Sobel", "Find edges by calculating the requested derivative order for the given image."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src", false),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("dx", 0),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("dy", 0),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("ksize", 3),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("scale", 1),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("delta", 0),
                                SocketHints.createEnumSocketHint("borderType", BorderTypesEnum.BORDER_DEFAULT),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src, dx, dy, ksize, scale, delta, borderType, dst) -> {
                                    opencv_imgproc.Sobel(src, dst, 0, dx.intValue(), dy.intValue(), ksize.intValue(), scale.doubleValue(), delta.doubleValue(), borderType.value);
                                }
                        )),

                new OperationMetaData(CVOperation.defaults("CV Threshold", "Apply a fixed-level threshold to each array element in an image."),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src", false),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("thresh", 0),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("maxval", 0),
                                SocketHints.createEnumSocketHint("type", ThresholdTypesEnum.THRESH_BINARY),
                                SocketHints.Outputs.createMatSocketHint("dst"),
                                (src, thresh, maxval, type, dst) -> {
                                    opencv_imgproc.threshold(src, dst, thresh.doubleValue(), maxval.doubleValue(), type.value);
                                }
                        ))
        );
    }

    /**
     * All of the operations that this list supplies
     */
    @VisibleForTesting
    ImmutableList<OperationMetaData> operations() {
        return ImmutableList.<OperationMetaData>builder().addAll(coreOperations).addAll(imgprocOperation).build();
    }

    public void addOperations() {
        coreOperations.stream()
                .map(OperationAddedEvent::new)
                .forEach(eventBus::post);
        imgprocOperation.stream()
                .map(OperationAddedEvent::new)
                .forEach(eventBus::post);
    }
}
