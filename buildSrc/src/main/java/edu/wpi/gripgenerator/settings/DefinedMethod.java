package edu.wpi.gripgenerator.settings;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import edu.wpi.gripgenerator.defaults.DefaultValueCollector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Allows you to define a Method that will be collected as the library is parsed.
 * This object is then used to store the method declaration that matches this method.
 * If a better match is found later in the parsing then it is used.
 */
public final class DefinedMethod {
    private final String methodName;
    private final List<DefinedParamType> paramTypes;
    private final boolean isCompleteList;
    private DefinedMethodCollection collectionOf;
    private Optional<String> description = Optional.empty();
    private Optional<MethodDeclaration> bestMatchMethod = Optional.empty();
    private boolean finalized = false;


    /**
     * Constructs the defined method. It is assumed that this is not the complete list of parameters.
     *
     * @param methodName the name of the method
     * @param paramTypes the type of param to match. Case sensitive.
     */
    public DefinedMethod(String methodName, String... paramTypes) {
        this(methodName, false, paramTypes);
    }

    /**
     * Constructs the defined method. It is assumed that this is not the complete list of parameters.
     *
     * @param methodName The name of the method.
     */
    public DefinedMethod(String methodName) {
        this(methodName, false, new ArrayList());
    }

    /**
     * Create a defined method
     *
     * @param methodName     The name of the method
     * @param isCompleteList true if this should match a method without any params.
     */
    public DefinedMethod(String methodName, boolean isCompleteList) {
        this(methodName, isCompleteList, new ArrayList());
    }

    /**
     * @param methodName     The name of the method
     * @param isCompleteList true if this should match a method with only the params provided and no more.
     *                       Otherwise will try to find one with these params and additional params.
     * @param paramTypes     The param types to match this method with.
     */
    public DefinedMethod(String methodName, boolean isCompleteList, String... paramTypes) {
        this(methodName, isCompleteList, Arrays.asList(paramTypes));
    }

    public DefinedMethod(String methodName, boolean isCompleteList, List<String> paramTypes) {
        this.methodName = methodName;
        this.paramTypes = paramTypes.stream().map(DefinedParamType::new).collect(Collectors.toList());
        this.isCompleteList = isCompleteList;
    }

    public DefinedMethod(String methodName, boolean isCompleteList, DefinedParamType... paramTypes) {
        this.methodName = methodName;
        this.paramTypes = new ArrayList(Arrays.asList(paramTypes));
        this.isCompleteList = isCompleteList;
    }

    /**
     * Set this DefinedMethod as part of a given collection
     *
     * @param collection The collection this method is a part of
     */
    void setCollectionOf(DefinedMethodCollection collection) {
        this.collectionOf = collection;
    }

    public String getMethodName() {
        return methodName;
    }

    /**
     * Adds a description to this method.
     *
     * @param description The description to use for this method.
     * @return This. Allows for method chaining off of constructor.
     */
    public DefinedMethod addDescription(String description) {
        this.description = Optional.of(description);
        return this;
    }

    public String getDescription() {
        if (this.description.isPresent()) {
            return this.description.get();
        } else {
            return "";
        }
    }

    public String getParentObjectName() {
        return collectionOf.getClassName();
    }

    /**
     * Is this defined method a match to the params of another MethodDeclaration's params
     *
     * @param params
     * @return true if a match
     */
    public boolean isMatch(List<Parameter> params) {
        // Check if the last param was a wildcard
        if (this.isCompleteList && paramTypes.size() != params.size()) return false;
        else if (paramTypes.size() > params.size()) return false;

        for (int i = 0; i < paramTypes.size(); i++) {
            if (!paramTypes.get(i).isMatch(params.get(i))) return false;
        }
        return true;
    }

    /**
     * Assigns this MethodDeclaration if it is a match to the params defined for this method.
     *
     * @param declaration The declaration to try to assign.
     * @return False if assignment has failed.
     */
    public boolean assignIfBestMatch(MethodDeclaration declaration) {
        assert !finalized : "Assigning on a method that has already been finalized";
        if (!isMatch(declaration.getParameters())) {
            //TODO: Perhaps this should throw an error?
            return false;
        }
        if (this.bestMatchMethod.isPresent()) {
            if (this.bestMatchMethod.get().getParameters().size() > declaration.getParameters().size()) {
                // The method we have is already more complex
                return false;
            }
        }
        this.bestMatchMethod = Optional.of(declaration);
        return true;
    }

    /**
     * Finalizes the Method
     * This method takes the MethodDeclaration that has been assigned to it and
     * constructs additional {@link DefinedParamType DefinedParamTypes} as needed
     *
     * @param collector The collector used to assign default values.
     * @return true if finalization succeeds or has already been completed.
     */
    protected boolean finalizeParamTypes(DefaultValueCollector collector) {
        if (finalized) return true;
        this.finalized = true;
        if (bestMatchMethod.isPresent()) {
            final MethodDeclaration declaration = bestMatchMethod.get();

            // Iterate over the chosen methods parameters
            for (int i = 0; i < declaration.getParameters().size(); i++) {
                Parameter param = declaration.getParameters().get(i);
                DefinedParamType definedParamType;
                assert i < paramTypes.size() : "The size of the params list was less than than the index. Invalid state";

                if (i == paramTypes.size()) { // Create the defined param for this param because it wasn't defined
                    definedParamType = new DefinedParamType(param.getType().toString());
                    paramTypes.add(definedParamType);
                } else { // Otherwise grab the predefined one.
                    definedParamType = paramTypes.get(i);
                }
                /*
                 * Sets the defined params direction given the mapping provided to the collector in the File Parser.
                 */
                collectionOf
                        .getDefaultDirection(param.getId().getName())
                        .ifPresent(definedParamType::trySetDirection);

                definedParamType.setParamAs(param);

                String defaultValue = definedParamType.getLiteralDefaultValue();
                if (defaultValue != null && collector.hasDefaultValueFor(defaultValue)) {
                    definedParamType.setDefaultValue(collector.getDefaultValueFor(defaultValue));
                }

                if (collectionOf.shouldIgnore(param.getId().getName())){
                    definedParamType.setIgnored();
                }
            }
            return this.finalized;
        } else {
            throw new IllegalStateException("No method was found that matched this method definition " + toSimpleString());
        }
    }

    /**
     * Gets the imports required for this Defined Method to compile
     *
     * @return The list of required imports.
     */
    public List<ImportDeclaration> getImports() {
        return paramTypes.stream().map(p -> p.getImport()).filter(i -> i != null).collect(Collectors.toList());
    }

    /**
     * Gets the finalized list of param types.
     *
     * @param collector The collector to use
     * @return The defined param types.
     */
    public List<DefinedParamType> getFinalizedParamTypes(DefaultValueCollector collector) {
        if (!this.finalizeParamTypes(collector)) {
            return null;
        }
        return this.paramTypes;
    }

    public String toSimpleString() {
        return "DefinedMethod{" +
                "methodName='" + methodName + '\'' +
                ", paramTypes=" + paramTypes +
                ", isCompleteList=" + isCompleteList +
                ", collectionOf=" + collectionOf +
                ", description=" + description +
                '}';
    }

    public String methodToString() {
        return this.bestMatchMethod.get().toString();
    }
}
