package edu.wpi.gripgenerator.settings;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import edu.wpi.gripgenerator.defaults.DefaultValueCollector;
import edu.wpi.gripgenerator.templates.Operation;
import edu.wpi.gripgenerator.templates.OperationList;

import java.util.*;
import java.util.stream.Collectors;

public class DefinedMethodCollection {
    private final String className;
    private final Map<String, DefinedMethod> definedMethodMap;
    private final Map<DefinedParamType.DefinedParamDirection, Set<String>> directionDefault = new HashMap<>();
    private final Set<String> ignoreDefaults = new HashSet<>();

    public DefinedMethodCollection(String className, DefinedMethod... methodList) {
        this(className, Arrays.asList(methodList));
    }

    public DefinedMethodCollection(String className, List<DefinedMethod> methodList) {
        this.className = className;
        methodList.forEach(m -> m.setCollectionOf(this));
        // Basically turn the list into a map with the method name as the key
        this.definedMethodMap = methodList.stream().collect(Collectors.toMap(DefinedMethod::getMethodName, t -> t));
    }

    public DefinedMethodCollection setDirectionDefaults(DefinedParamType.DefinedParamDirection direction, String... paramNames) {
        directionDefault.putIfAbsent(direction, new HashSet<>());
        directionDefault.get(direction).addAll(Arrays.asList(paramNames));
        return this;
    }

    public DefinedMethodCollection setIgnoreDefaults(String... ignoreDefaults) {
        this.ignoreDefaults.addAll(Arrays.asList(ignoreDefaults));
        return this;
    }

    public boolean matchesParent(MethodDeclaration declaration) {
        Node parent = declaration.getParentNode();
        if (parent instanceof ClassOrInterfaceDeclaration) {
            return ((ClassOrInterfaceDeclaration) parent).getName().equals(this.className);
        }
        return false;
    }

    public String getClassName() {
        return className;
    }

    /**
     * Gets the default direction for a given param's Variable Identifier
     * @param check The Variable identifier to check
     * @return The direction this has been mapped to
     */
    public Optional<DefinedParamType.DefinedParamDirection> getDefaultDirection(String check) {
        for(DefinedParamType.DefinedParamDirection direction : DefinedParamType.DefinedParamDirection.values()){
            if(directionDefault.getOrDefault(direction, Collections.EMPTY_SET).contains(check)){
                return Optional.of(direction);
            }
        }
        return Optional.empty();
    }

    public boolean shouldIgnore(String check) {
        return ignoreDefaults.contains(check);
    }


    /**
     * Tries to find a method matching the given MethodDeclaraion
     *
     * @param declaration The declaration to match against
     * @return The DefinedMethod that matches or <code>null</code> if none exists.
     */
    public DefinedMethod getMethodMatching(MethodDeclaration declaration) {
        DefinedMethod possibleMatch = definedMethodMap.get(declaration.getName());
        if (possibleMatch != null && possibleMatch.isMatch(declaration.getParameters())) {
            return possibleMatch;
        } else {
            return null;
        }
    }

    public void generateCompilationUnits(DefaultValueCollector collector, Map<String, CompilationUnit> compilationUnits, OperationList operations) {
        for (DefinedMethod method : definedMethodMap.values()) {
            Operation thisOperation = new Operation(collector, method, className);
            operations.addOperation(thisOperation);
            CompilationUnit cu = thisOperation.getDeclaration();
            //System.out.println(cu);
            compilationUnits.put(thisOperation.getOperationClassName(), cu);
        }
    }
}
