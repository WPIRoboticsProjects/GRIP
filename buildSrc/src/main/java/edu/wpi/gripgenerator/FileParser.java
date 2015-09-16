package edu.wpi.gripgenerator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;

import java.io.IOException;
import java.net.URL;
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
        return compilationUnits;
    }

    public static void main(String ... args) {
        FileParser.testRead();
    }
}
