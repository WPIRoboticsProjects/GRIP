package edu.wpi.gripgenerator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import edu.wpi.gripgenerator.defaults.DefaultValueCollector;
import edu.wpi.gripgenerator.defaults.EnumDefaultValue;
import edu.wpi.gripgenerator.defaults.ObjectDefaultValue;
import edu.wpi.gripgenerator.defaults.PrimitiveDefaultValue;
import edu.wpi.gripgenerator.settings.DefinedMethod;
import edu.wpi.gripgenerator.settings.DefinedMethodCollection;
import edu.wpi.gripgenerator.settings.DefinedParamType;
import edu.wpi.gripgenerator.templates.OperationList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FileParser {
    /**
     * Regex splits the parameter into three distinct capture groups.
     * <ol>
     * <li>The type and the param with optional varargs.</li>
     * <li>The comment that is after the parameter.</li>
     * <li>The various ways that the parameter can end.</li>
     * </ol>
     */
    protected static final String methodReorderPattern = "([A-Za-z1-9]+ (?:\\.\\.\\.)?[a-z][A-Za-z0-9_]*)(/\\*=[^ ]*\\*/)((?:,)|(?:\\s*\\)))";

    /**
     * Reorders the {@link FileParser#methodReorderPattern} capture groups so the JavaParser can correctly
     * associate the params with their respective comments.
     */
    protected static final String methodNewOrder = "$2$1$3";

    /**
     * There is a bug in the JavaParser that will incorrectly associate comments after a parameter but
     * before a comma will incorrectly associate that comment with the next param in the method's params.
     *
     * @param stream The original input file.
     * @return The processed output stream.
     * @see <a href="https://github.com/javaparser/javaparser/issues/199">Javaparser Issue:199</a>
     */
    private static InputStream preProcessStream(InputStream stream) {
        //FIXME: This is a hack around. This should be removed once the above noted issue is resolved.
        java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
        String input = s.hasNext() ? s.next() : "";
        input = input.replaceAll(methodReorderPattern, methodNewOrder);
        return new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    }

    private static CompilationUnit readFile(URL url) {
        try {
            return JavaParser.parse(preProcessStream(url.openStream()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Generates all of the source code from the opencv bindings
     *
     * @return A map of the filename with the compilation units
     */
    public static Map<String, CompilationUnit> generateAllSourceCode() {
        URL INPUT_URL = FileParser.class.getResource("/org/bytedeco/javacpp/opencv_core.txt");
        CompilationUnit compilationUnit = readFile(INPUT_URL);
        Map<String, CompilationUnit> returnMap = new HashMap<>();
        DefaultValueCollector collector = new DefaultValueCollector();
        collector.add(new PrimitiveDefaultValue(new PrimitiveType(PrimitiveType.Primitive.Double)){
            @Override
            protected Set<String> getDefaultValues() {
                return Collections.singleton("CV_PI");
            }

            @Override
            public Expression getDefaultValue(String defaultValue) {
                return new FieldAccessExpr(
                        new NameExpr("Math"),
                        "PI"
                );
            }
        });

        collector.add(new EnumDefaultValue("edu.wpi.grip.core.operations.opencv.enumeration", "FlipCode", "X_AXIS", "Y_AXIS", "BOTH_AXIS"));

        OperationList operationList = new OperationList(
                new ImportDeclaration(new NameExpr("edu.wpi.grip.generated.opencv_core"), false, true),
                new ImportDeclaration(new NameExpr("edu.wpi.grip.generated.opencv_imgproc"), false, true)
        );

        if (compilationUnit != null) {
            returnMap.putAll(parseOpenCVCore(compilationUnit, collector, operationList));
        } else {
            System.err.print("Invalid File input");
        }
        URL INPUT_URL2 = FileParser.class.getResource("/org/bytedeco/javacpp/opencv_imgproc.txt");
        compilationUnit = readFile(INPUT_URL2);
        if (compilationUnit != null) {
            returnMap.putAll(parseOpenImgprc(compilationUnit, collector, operationList));

        }

        // Generate the Operation List class last
        returnMap.put(operationList.getClassName(), operationList.getDeclaration());
        return returnMap;
    }

    public static Map<String, CompilationUnit> parseOpenImgprc(CompilationUnit imgprocDeclaration, DefaultValueCollector collector, OperationList operations) {
        Map<String, CompilationUnit> compilationUnits = new HashMap<>();
        final String baseClassName = "opencv_imgproc";

        OpenCVEnumVisitor enumVisitor = new OpenCVEnumVisitor(baseClassName, collector);
        enumVisitor.visit(imgprocDeclaration, compilationUnits);
        compilationUnits.putAll(enumVisitor.generateCompilationUnits());

        DefinedMethodCollection collection = new DefinedMethodCollection(baseClassName,
                new DefinedMethod("Sobel", false, "Mat", "Mat"),
                new DefinedMethod("medianBlur", false, "Mat", "Mat"),
                new DefinedMethod("GaussianBlur", false,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Size").setDefaultValue(new ObjectDefaultValue("Size", "1", "1"))
                ),
                new DefinedMethod("Laplacian", "Mat", "Mat"),
                new DefinedMethod("dilate", false, "Mat", "Mat"),
                new DefinedMethod("Canny", false, new DefinedParamType("Mat"), new DefinedParamType("Mat", DefinedParamType.DefinedParamState.OUTPUT)),
                new DefinedMethod("cornerMinEigenVal", false, "Mat", "Mat"),
                new DefinedMethod("cornerHarris", false, "Mat", "Mat"),
                new DefinedMethod("cornerEigenValsAndVecs", false, "Mat", "Mat"),
                new DefinedMethod("threshold", false,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("double"),
                        new DefinedParamType("double"),
                        new DefinedParamType("int").setLiteralDefaultValue("THRESH_BINARY")
                ).addDescription("Applies a fixed-level threshold to each array element. " +
                        "The function applies fixed-level thresholding to a single-channel array. The function is typically " +
                        "used to get a bi-level (binary) image out of a grayscale image ( cv::compare could be also used for " +
                        "this purpose) or for removing a noise, that is, filtering out pixels with too small or too large " +
                        "values. There are several types of thresholding supported by the function. They are determined by " +
                        "type parameter. " +
                        "Also, the special values cv::THRESH_OTSU or cv::THRESH_TRIANGLE may be combined with one of the " +
                        "above values. In these cases, the function determines the optimal threshold value using the Otsu's " +
                        "or Triangle algorithm and uses it instead of the specified thresh . The function returns the " +
                        "computed threshold value. Currently, the Otsu's and Triangle methods are implemented only for 8-bit " +
                        "images."),
                new DefinedMethod("adaptiveThreshold", false,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("double"),
                        new DefinedParamType("int").setLiteralDefaultValue("ADAPTIVE_THRESH_MEAN_C"),
                        new DefinedParamType("int").setLiteralDefaultValue("THRESH_BINARY")
                ).addDescription("Applies an adaptive threshold to an array. The function transforms a grayscale image to a binary image"),
                new DefinedMethod("erode", false,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat")
                ),
                new DefinedMethod("cvtColor", false,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("int").setLiteralDefaultValue("COLOR_BGR2BGRA")
                ).addDescription("The function converts an input image from one color space to another. In case of a transformation " +
                        "to-from RGB color space, the order of the channels should be specified explicitly (RGB or BGR). Note " +
                        "that the default color format in OpenCV is often referred to as RGB but it is actually BGR (the " +
                        "bytes are reversed). So the first byte in a standard (24-bit) color image will be an 8-bit Blue " +
                        "component, the second byte will be Green, and the third byte will be Red. The fourth, fifth, and " +
                        "sixth bytes would then be the second pixel (Blue, then Green, then Red), and so on."),
                new DefinedMethod("applyColorMap", true,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("int").setLiteralDefaultValue("COLORMAP_AUTUMN")
                ).addDescription("Applies a GNU Octave/MATLAB equivalent colormap on a given image."),
                new DefinedMethod("resize", false,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Size")
                ).addDescription("The function resize resizes the image src down to or up to the specified size. Note that the " +
                        "initial dst type or size are not taken into account. Instead, the size and type are derived from " +
                        "the `src`,`dsize`,`fx`, and `fy`. To shrink an image, it will generally look best with CV_INTER_AREA interpolation, whereas to " +
                        "enlarge an image, it will generally look best with CV_INTER_CUBIC (slow) or CV_INTER_LINEAR " +
                        "(faster but still looks OK)"),
                new DefinedMethod("HoughLines", false,
                        new DefinedParamType("Mat", DefinedParamType.DefinedParamState.INPUT_AND_OUTPUT),
                        new DefinedParamType("Mat", DefinedParamType.DefinedParamState.OUTPUT)
                ),
                new DefinedMethod("rectangle", false,
                        new DefinedParamType("Mat", DefinedParamType.DefinedParamState.INPUT_AND_OUTPUT),
                        new DefinedParamType("Point"))
        ).setOutputDefaults("dst").setIgnoreDefaults("dtype");
        new OpenCVMethodVisitor(collection).visit(imgprocDeclaration, compilationUnits);
        collection.generateCompilationUnits(collector, compilationUnits, operations);
        return compilationUnits;
    }

    public static Map<String, CompilationUnit> parseOpenCVCore(CompilationUnit coreDeclaration, DefaultValueCollector collector, OperationList operations) {
        Map<String, CompilationUnit> compilationUnits = new HashMap<>();
        final String baseClassName = "opencv_core";

        OpenCVEnumVisitor enumVisitor = new OpenCVEnumVisitor(baseClassName, collector);
        enumVisitor.visit(coreDeclaration, compilationUnits);
        compilationUnits.putAll(enumVisitor.generateCompilationUnits());

        DefinedMethodCollection collection = new DefinedMethodCollection(baseClassName,
                new DefinedMethod("add", false, "Mat", "Mat", "Mat"),
                new DefinedMethod("subtract", false, "Mat", "Mat", "Mat").addDescription("Calculates the per-pixel difference between two images"),
                new DefinedMethod("multiply", false, "Mat", "Mat", "Mat"),
                new DefinedMethod("divide", false, "Mat", "Mat", "Mat"),
                new DefinedMethod("scaleAdd", false, "Mat", "double", "Mat", "Mat"),
                new DefinedMethod("normalize", false, "Mat", "Mat"),
                new DefinedMethod("batchDistance", false, "Mat", "Mat"),
                new DefinedMethod("addWeighted", false, "Mat"),
                new DefinedMethod("flip", false,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("int").setLiteralDefaultValue("Y_AXIS")),
                new DefinedMethod("bitwise_and", false, "Mat", "Mat"),
                new DefinedMethod("bitwise_or", false, "Mat", "Mat"),
                new DefinedMethod("bitwise_xor", false, "Mat", "Mat"),
                new DefinedMethod("bitwise_not", false, "Mat", "Mat"),
                new DefinedMethod("absdiff", false, "Mat", "Mat"),
                // TODO: Fix (Causes Segfault)
                //new DefinedMethod("inRange", false),
                new DefinedMethod("compare", true,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("int").setLiteralDefaultValue("CMP_EQ")
                ),
                new DefinedMethod("max", false, "Mat", "Mat"),
                new DefinedMethod("min", false, "Mat", "Mat")
//                new DefinedMethod("sqrt", false, "Mat", "Mat"),
//                new DefinedMethod("pow", false,
//                        new DefinedParamType("Mat"),
//                        new DefinedParamType("double")
//                                .setDefaultValue(new PrimitiveDefaultValue(new PrimitiveType(PrimitiveType.Primitive.Double), "1"))
//                )
        ).setOutputDefaults("dst").setIgnoreDefaults("dtype");
        new OpenCVMethodVisitor(collection).visit(coreDeclaration, compilationUnits);

        collection.generateCompilationUnits(collector, compilationUnits, operations);


        return compilationUnits;
    }

    public static void main(String... args) {
        FileParser.generateAllSourceCode();
    }
}
