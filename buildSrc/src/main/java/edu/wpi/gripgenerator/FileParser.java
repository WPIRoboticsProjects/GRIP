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
        java.util.Scanner s = new java.util.Scanner(stream, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
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
        collector.add(new PrimitiveDefaultValue(new PrimitiveType(PrimitiveType.Primitive.Double)) {
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

        collector.add(new EnumDefaultValue("edu.wpi.grip.core.operations.opencv.enumeration", "FlipCode", "X_AXIS", "Y_AXIS", "BOTH_AXES"));

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
                new DefinedMethod("Sobel", false, "Mat", "Mat"
                ).addDescription("Find edges by calculating the requested derivative order for the given image."),
                new DefinedMethod("medianBlur", false, "Mat", "Mat"
                ).addDescription("Apply a Median blur to an image."),
                new DefinedMethod("GaussianBlur", false,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Size").setDefaultValue(new ObjectDefaultValue("Size", "1", "1"))
                ).addDescription("Apply a Gaussian blur to an image."),
                new DefinedMethod("Laplacian", "Mat", "Mat"
                ).addDescription("Find edges by calculating the Laplacian for the given image."),
                new DefinedMethod("dilate", false,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat").setDefaultValue(new ObjectDefaultValue("Mat"))
                ).addDescription("Expands areas of higher values in an image."),
                new DefinedMethod("Canny", false,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat", DefinedParamType.DefinedParamDirection.OUTPUT)
                ).addDescription("Apply a \\\"canny edge detection\\\" algorithm to an image."),
                new DefinedMethod("threshold", false,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("double"),
                        new DefinedParamType("double"),
                        new DefinedParamType("int").setLiteralDefaultValue("THRESH_BINARY")
                ).addDescription("Apply a fixed-level threshold to each array element in an image."),
                new DefinedMethod("adaptiveThreshold", false,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("double"),
                        new DefinedParamType("int").setLiteralDefaultValue("ADAPTIVE_THRESH_MEAN_C"),
                        new DefinedParamType("int").setLiteralDefaultValue("THRESH_BINARY")
                ).addDescription("Transforms a grayscale image to a binary image)."),
                new DefinedMethod("erode", false,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat").setDefaultValue(new ObjectDefaultValue("Mat"))
                ).addDescription("Expands areas of lower values in an image."),
                new DefinedMethod("cvtColor", false,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("int").setLiteralDefaultValue("COLOR_BGR2BGRA")
                ).addDescription("Convert an image from one color space to another."),
                new DefinedMethod("applyColorMap", true,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("int").setLiteralDefaultValue("COLORMAP_AUTUMN")
                ).addDescription("Apply a MATLAB equivalent colormap to an image."),
                new DefinedMethod("resize", false,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Size").setDefaultValue(new ObjectDefaultValue("Size"))
                ).addDescription("Resize the image to the specified size."),
                new DefinedMethod("rectangle", false,
                        new DefinedParamType("Mat", DefinedParamType.DefinedParamDirection.INPUT_AND_OUTPUT),
                        new DefinedParamType("Point")
                ).addDescription("Draw a rectangle (outline or filled) on an image.")
        ).setDirectionDefaults(DefinedParamType.DefinedParamDirection.OUTPUT, "dst")
                .setIgnoreDefaults("dtype", "ddepth");
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
                new DefinedMethod("add", true, "Mat", "Mat", "Mat")
                        .addDescription("Calculate the per-pixel sum of two images."),
                new DefinedMethod("subtract", true, "Mat", "Mat", "Mat")
                        .addDescription("Calculate the per-pixel difference between two images."),
                new DefinedMethod("multiply", false, "Mat", "Mat", "Mat")
                        .addDescription("Calculate the per-pixel scaled product of two images."),
                new DefinedMethod("divide", false, "Mat", "Mat", "Mat")
                        .addDescription("Perform per-pixel division of two images."),
                new DefinedMethod("scaleAdd", false, "Mat", "double", "Mat", "Mat")
                        .addDescription("Calculate the sum of two images where one image is multiplied by a scalar."),
//                new DefinedMethod("normalize", false, "Mat", "Mat"),
                new DefinedMethod("addWeighted", false, "Mat")
                        .addDescription("Calculate the weighted sum of two images."),
                new DefinedMethod("flip", false,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("int").setLiteralDefaultValue("Y_AXIS"))
                        .addDescription("Flip image around vertical, horizontal, or both axes."),
                new DefinedMethod("bitwise_and", true, "Mat", "Mat", "Mat")
                        .addDescription("Calculate the per-element bitwise conjunction of two images."),
                new DefinedMethod("bitwise_or", true, "Mat", "Mat", "Mat")
                        .addDescription("Calculate the per-element bit-wise disjunction of two images."),
                new DefinedMethod("bitwise_xor", true, "Mat", "Mat", "Mat")
                        .addDescription("Calculate the per-element bit-wise \\\"exclusive or\\\" on two images."),
                new DefinedMethod("bitwise_not", true, "Mat", "Mat")
                        .addDescription("Calculate per-element bit-wise inversion of an image."),
                new DefinedMethod("absdiff", false, "Mat", "Mat")
                        .addDescription("Calculate the per-element absolute difference of two images."),
                new DefinedMethod("compare", true,
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("Mat"),
                        new DefinedParamType("int").setLiteralDefaultValue("CMP_EQ")
                ).addDescription("Compare each pixel in two images using a given rule."),
                new DefinedMethod("max", false, "Mat", "Mat")
                        .addDescription("Calculate per-element maximum of two images."),
                new DefinedMethod("min", false, "Mat", "Mat")
                        .addDescription("Calculate the per-element minimum of two images."),
                new DefinedMethod("extractChannel", false, "Mat", "Mat")
                        .addDescription("Extract a single channel from a image."),
                new DefinedMethod("transpose", false, "Mat", "Mat")
                        .addDescription("Calculate the transpose of an image.")
//                new DefinedMethod("sqrt", false, "Mat", "Mat"),
//                new DefinedMethod("pow", false,
//                        new DefinedParamType("Mat"),
//                        new DefinedParamType("double")
//                                .setDefaultValue(new PrimitiveDefaultValue(new PrimitiveType(PrimitiveType.Primitive.Double), "1"))
//                )
        ).setDirectionDefaults(DefinedParamType.DefinedParamDirection.OUTPUT, "dst")
                .setIgnoreDefaults("dtype", "ddepth");
        new OpenCVMethodVisitor(collection).visit(coreDeclaration, compilationUnits);

        collection.generateCompilationUnits(collector, compilationUnits, operations);


        return compilationUnits;
    }

    public static void main(String... args) {
        FileParser.generateAllSourceCode();
    }
}
