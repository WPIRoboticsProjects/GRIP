package edu.wpi.gripgenerator.settings;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import edu.wpi.gripgenerator.templates.Operation;

import java.util.*;
import java.util.stream.Collectors;

public class DefinedMethodCollection {
    private final String className;
    private final Map<String, DefinedMethod> definedMethodMap;
    private final Set<String> outputDefaults = new HashSet<>();

    public DefinedMethodCollection(String className, DefinedMethod ...methodList){
        this(className, Arrays.asList(methodList));
    }

    public DefinedMethodCollection(String className, List<DefinedMethod> methodList){
        this.className = className;
        methodList.forEach(m -> m.setCollectionOf(this));
        // Basically turn the list into a map with the method name as the key
        this.definedMethodMap = methodList.stream().collect(Collectors.toMap(DefinedMethod::getMethodName, t -> t));
    }

    public DefinedMethodCollection setOutputDefaults(String ...paramNames){
        outputDefaults.addAll(Arrays.asList(paramNames));
        return this;
    }

    public boolean matchesParent(MethodDeclaration declaration){
        Node parent = declaration.getParentNode();
        if(parent instanceof ClassOrInterfaceDeclaration){
            return ((ClassOrInterfaceDeclaration) parent).getName().equals(this.className);
        }
        return false;
    }

    public boolean isOutputDefault(String check){
        return outputDefaults.contains(check);
    }


    /**
     * Tries to find a method matching the given MethodDeclaraion
     * @param declaration The declaration to match against
     * @return The DefinedMethod that matches or <code>null</code> if none exists.
     */
    public DefinedMethod getMethodMatching(MethodDeclaration declaration){
        DefinedMethod possibleMatch = definedMethodMap.get(declaration.getName());
        if (possibleMatch != null && possibleMatch.isMatch(declaration.getParameters())){
            return possibleMatch;
        } else {
            return null;
        }
    }

    public void generateCompilationUnits(Map<String, CompilationUnit> compilationUnits) {
        for(DefinedMethod method : definedMethodMap.values()){
            Operation thisOperation = new Operation(method, className);
            CompilationUnit cu = thisOperation.getDeclaration();
            System.out.println(cu);
            compilationUnits.put(method.getMethodName(), cu);
        }
    }
}
