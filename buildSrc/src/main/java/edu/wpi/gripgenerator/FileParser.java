package edu.wpi.gripgenerator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import edu.wpi.gripgenerator.settings.DefinedMethod;
import edu.wpi.gripgenerator.settings.DefinedMethodCollection;
import edu.wpi.gripgenerator.templates.SocketHintDeclaration;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileParser {

    public static CompilationUnit readFile(URL url){
        try {
            return JavaParser.parse(url.openStream());
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

        if (compilationUnit != null) {
            for(TypeDeclaration type : compilationUnit.getTypes()){
                if(type.getName().equals("opencv_core")){
                    return parseOpenCVCore(compilationUnit);
                }
            }
        } else {
            System.err.print("Invalid File input");
        }
        return null;
    }

    public static Map<String, CompilationUnit> parseOpenCVCore(CompilationUnit coreDeclaration){
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

        new OpenCVEnumVisitor().visit(coreDeclaration, compilationUnits);

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

        collection.generateCompilationUnits(compilationUnits);

        SocketHintDeclaration testDeclaration = new SocketHintDeclaration("Mat", Arrays.asList("src1", "src2"), true);
        System.out.println(testDeclaration.getDeclaration());

        return compilationUnits;
    }

    public static void main(String ... args) {
        FileParser.testRead();
    }
}
