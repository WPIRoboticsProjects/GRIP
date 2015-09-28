package edu.wpi.gripgenerator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import edu.wpi.gripgenerator.collectors.DefaultValueCollector;
import edu.wpi.gripgenerator.settings.DefinedMethod;
import edu.wpi.gripgenerator.settings.DefinedMethodCollection;
import edu.wpi.gripgenerator.settings.DefinedParamType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileParser {
    /**
     * Regex splits the parameter into three distinct capture groups.
     * <ol>
     *     <li>The type and the param with optional varargs.</li>
     *     <li>The comment that is after the parameter.</li>
     *     <li>The various ways that the parameter can end.</li>
     * </ol>
     */
    protected static final String methodReorderPattern = "([A-Za-z1-9]+ (?:\\.\\.\\.)?[a-z][A-Za-z0-9]*)(/\\*=[^ ]*\\*/)((?:,)|(?: \\)))";

    /**
     * Reorders the {@link FileParser#methodReorderPattern} capture groups so the JavaParser can correctly
     * associate the params with their respective comments.
     */
    protected static final String methodNewOrder = "$2$1$3";

    /**
     * There is a bug in the JavaParser that will incorrectly associate comments after a parameter but
     * before a comma will incorrectly associate that comment with the next param in the method's params.
     * @see <a href="https://github.com/javaparser/javaparser/issues/199">Javaparser Issue:199</a>
     * @param stream The original input file.
     * @return The processed output stream.
     */
    private static InputStream preProcessStream(InputStream stream){
        //FIXME: This is a hack around. This should be removed once the above noted issue is resolved.
        java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
        String input = s.hasNext() ? s.next() : "";
        input = input.replaceAll(methodReorderPattern, methodNewOrder);
        return new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    }

    public static CompilationUnit readFile(URL url){

        try {
            return JavaParser.parse(preProcessStream(url.openStream()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, CompilationUnit> testRead(){
        URL INPUT_URL = FileParser.class.getResource("/org/bytedeco/javacpp/opencv_core.txt");
        CompilationUnit compilationUnit = readFile(INPUT_URL);
        Map<String, CompilationUnit> returnMap = new HashMap<>();
        DefaultValueCollector collector = new DefaultValueCollector();

        if (compilationUnit != null) {
            for(TypeDeclaration type : compilationUnit.getTypes()){
                if(type.getName().equals("opencv_core")){
                    returnMap.putAll( parseOpenCVCore(compilationUnit, collector) );
                }
            }
        } else {
            System.err.print("Invalid File input");
        }
        URL INPUT_URL2 = FileParser.class.getResource("/org/bytedeco/javacpp/opencv_imgproc.txt");
        compilationUnit = readFile(INPUT_URL2);
        if(compilationUnit != null){
            returnMap.putAll(parseOpenImgprc(compilationUnit, collector));
        }
        return returnMap;
    }

    public static Map<String, CompilationUnit> parseOpenImgprc(CompilationUnit imgprocDeclaration, DefaultValueCollector collector){
        Map<String, CompilationUnit> compilationUnits = new HashMap<>();
        DefinedMethodCollection collection = new DefinedMethodCollection("opencv_imgproc",
                new DefinedMethod("Sobel", false, "Mat", "Mat"),
                new DefinedMethod("accumulateSquare", false, "Mat", "Mat"),
                new DefinedMethod("medianBlur", false, "Mat", "Mat"),
                new DefinedMethod("GaussianBlur", false, "Mat", "Mat"),
                new DefinedMethod("Laplacian", "Mat", "Mat"),
                new DefinedMethod("dilate", false, "Mat", "Mat"),
                new DefinedMethod("Canny", false, new DefinedParamType("Mat"), new DefinedParamType("Mat", DefinedParamType.DefinedParamState.OUTPUT)),
                new DefinedMethod("cornerMinEigenVal", false, "Mat", "Mat"),
                new DefinedMethod("cornerHarris", false, "Mat", "Mat"),
                new DefinedMethod("cornerEigenValsAndVecs", false, "Mat", "Mat")
                ).setOutputDefaults("dst");
        new OpenCVMethodVisitor(collection).visit(imgprocDeclaration, compilationUnits);
        collection.generateCompilationUnits(collector, compilationUnits);
        return compilationUnits;
    }

    public static Map<String, CompilationUnit> parseOpenCVCore(CompilationUnit coreDeclaration, DefaultValueCollector collector){
        Map<String, CompilationUnit> compilationUnits = new HashMap<>();
        Pattern enumRegex = Pattern.compile(".*enum cv::([a-zA-Z_]*)");
        for(Comment comment : coreDeclaration.getAllContainedComments()){
            Matcher matcher = enumRegex.matcher(comment.getContent());
            if(matcher.find()){
                System.out.print(comment.getContent());
                System.out.print(" ");
                System.out.print(matcher.group(1));
                System.out.print(" ");
                System.out.print(comment.getBeginLine());
                System.out.print(" ");
                System.out.print(comment.getEndLine());
                System.out.println();
            }
        }

        new OpenCVEnumVisitor(collector).visit(coreDeclaration, compilationUnits);

        DefinedMethodCollection collection = new DefinedMethodCollection("opencv_core",
                new DefinedMethod("add", false, "Mat", "Mat", "Mat"),
                new DefinedMethod("subtract", false, "Mat", "Mat", "Mat").addDescription("Calculates the per-pixel difference between two images"),
                new DefinedMethod("multiply", false, "Mat", "Mat", "Mat"),
                new DefinedMethod("divide", false, "Mat", "Mat", "Mat"),
                new DefinedMethod("scaleAdd", false, "Mat", "double", "Mat", "Mat"),
                new DefinedMethod("normalize", false, "Mat", "Mat"),
                new DefinedMethod("batchDistance", false, "Mat", "Mat"),
                new DefinedMethod("addWeighted", false, "Mat"),
                new DefinedMethod("flip", false, "Mat", "Mat"),
                new DefinedMethod("bitwise_and", false, "Mat", "Mat"),
                new DefinedMethod("bitwise_or", false, "Mat", "Mat"),
                new DefinedMethod("bitwise_xor", false, "Mat", "Mat"),
                new DefinedMethod("bitwise_not", false, "Mat", "Mat"),
                new DefinedMethod("absdiff", false, "Mat", "Mat"),
                new DefinedMethod("inRange", false),
                new DefinedMethod("compare"),
                new DefinedMethod("max", false, "Mat", "Mat"),
                new DefinedMethod("min", false, "Mat", "Mat"),
                new DefinedMethod("sqrt", false, "Mat", "Mat"),
                new DefinedMethod("pow", false, "Mat")
        ).setOutputDefaults("dst");
        new OpenCVMethodVisitor(collection).visit(coreDeclaration, compilationUnits);

        collection.generateCompilationUnits(collector, compilationUnits);


        return compilationUnits;
    }

    public static void main(String ... args) {
        FileParser.testRead();
    }
}
