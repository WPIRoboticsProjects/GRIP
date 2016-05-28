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
import edu.wpi.gripgenerator.defaults.PrimitiveDefaultValue;
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
        return returnMap;
    }

    public static Map<String, CompilationUnit> parseOpenImgprc(CompilationUnit imgprocDeclaration, DefaultValueCollector collector, OperationList operations) {
        Map<String, CompilationUnit> compilationUnits = new HashMap<>();
        final String baseClassName = "opencv_imgproc";

        OpenCVEnumVisitor enumVisitor = new OpenCVEnumVisitor(baseClassName, collector);
        enumVisitor.visit(imgprocDeclaration, compilationUnits);
        compilationUnits.putAll(enumVisitor.generateCompilationUnits());
        return compilationUnits;
    }

    public static Map<String, CompilationUnit> parseOpenCVCore(CompilationUnit coreDeclaration, DefaultValueCollector collector, OperationList operations) {
        Map<String, CompilationUnit> compilationUnits = new HashMap<>();
        final String baseClassName = "opencv_core";

        OpenCVEnumVisitor enumVisitor = new OpenCVEnumVisitor(baseClassName, collector);
        enumVisitor.visit(coreDeclaration, compilationUnits);
        compilationUnits.putAll(enumVisitor.generateCompilationUnits());

        return compilationUnits;
    }

    public static void main(String... args) {
        FileParser.generateAllSourceCode();
    }
}
