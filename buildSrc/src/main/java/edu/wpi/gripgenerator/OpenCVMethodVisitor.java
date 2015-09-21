package edu.wpi.gripgenerator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import edu.wpi.gripgenerator.settings.DefinedMethod;
import edu.wpi.gripgenerator.settings.DefinedMethodCollection;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OpenCVMethodVisitor extends VoidVisitorAdapter<Map<String, CompilationUnit>> {
    private final static String DESTINATION_PARAM_NAME = "dst";
    private final List<DefinedMethodCollection> collections;

    public OpenCVMethodVisitor(DefinedMethodCollection ...collections){
        this.collections = Arrays.asList(collections);
    }

    private DefinedMethodCollection getDefinedCollectionMatchingParentOf(MethodDeclaration declaration){
        for(DefinedMethodCollection collection : collections){
            if(collection.matchesParent(declaration)) return collection;
        }
        return null;
    }

    private DefinedMethod getDefinedMethodMatching(MethodDeclaration declaration){
        DefinedMethodCollection collection = getDefinedCollectionMatchingParentOf(declaration);
        if(collection != null){
            return collection.getMethodMatching(declaration);
        }
        return null;
    }


    public void visit(MethodDeclaration declaration, final Map<String, CompilationUnit> args){
        DefinedMethod method = getDefinedMethodMatching(declaration);
        if(method != null) {
            System.out.print("Found " + method.getMethodName());
            method.assignIfBestMatch(declaration);
        }
        for(Parameter param : declaration.getParameters()){
            if(param.getId().getName().equals(DESTINATION_PARAM_NAME)){
                System.out.println(declaration.getDeclarationAsString(true, true));
            }
        }
    }
}
