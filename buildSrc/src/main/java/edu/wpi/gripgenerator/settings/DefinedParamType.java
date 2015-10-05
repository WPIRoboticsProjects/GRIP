package edu.wpi.gripgenerator.settings;


import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import edu.wpi.gripgenerator.defaults.DefaultValue;
import edu.wpi.gripgenerator.defaults.EnumDefaultValue;
import edu.wpi.gripgenerator.defaults.ObjectDefaultValue;
import edu.wpi.gripgenerator.defaults.PrimitiveDefaultValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.javaparser.ASTHelper.createNameExpr;
import static com.github.javaparser.ASTHelper.createReferenceType;
import static com.google.common.base.Preconditions.checkNotNull;

public class DefinedParamType {
    public enum DefinedParamState {
        INPUT,
        OUTPUT
    }

    private final String type;
    private DefinedParamState state;
    private Optional<Parameter> parameter = Optional.empty();
    private Optional<DefaultValue> defaultValue = Optional.empty();
    private String literalDefaultValue;

    private final Pattern constructorCallExpression = Pattern.compile(".*cv?:?:([A-Z][a-zA-Z_]*)\\(.*\\)");
    private final Pattern methodCallExpression = Pattern.compile(".*cv?:?:([a-z][a-zA-Z_]*)\\(.*\\)");

    /**
     * Defines a Param type that a method must match.
     * Defaults the value to being an input.
     *
     * @param type
     */
    public DefinedParamType(String type) {
        this(type, DefinedParamState.INPUT);
    }

    public DefinedParamType(String type, Parameter param) {
        this(type);
        parameter = Optional.of(param);
    }

    public DefinedParamType(String type, DefinedParamState state) {
        this.type = type;
        this.state = state;
    }

    public static List<DefinedParamType> fromStrings(List<String> params) {
        List<DefinedParamType> paramStates = new ArrayList<>();
        for (String param : params) {
            paramStates.add(new DefinedParamType(param));
        }
        return paramStates;
    }

    public DefinedParamType setDefaultValue(DefaultValue defaultValue) {
        checkNotNull(defaultValue);
        //System.out.println("Setting default value " + defaultValue.getName());
        this.defaultValue = Optional.of(defaultValue);
        return this;
    }

    public Optional<DefaultValue> getDefaultValue() {
        return defaultValue;
    }

    void setParamAs(Parameter param) {
        checkNotNull(param);
        this.parameter = Optional.of(param);
//        System.out.println("Param: " + param);
//        System.out.println("Param w/out comments: " + param.toStringWithoutComments());
//        System.out.println("Comment: " + param.getComment());
//        System.out.println("Orphan Comments: " + param.getOrphanComments());
        Pattern defaultPrimitive = Pattern.compile(".*=([\\-a-zA-Z0-9_\\.]*)");
        Pattern defaultEnumPrimitive = Pattern.compile(".*=(?:cv::)([\\-a-zA-Z0-9_\\.]*)");

        if (param.getComment() != null && param.getType() instanceof PrimitiveType) {
            //System.out.println("Checking match");
            Matcher matchesPrimitive = defaultPrimitive.matcher(param.getComment().getContent());
            Matcher matchesEnumPrimitive = defaultEnumPrimitive.matcher(param.getComment().getContent());
            //if (matchesPrimitive.find()) System.out.println("Primitive Matcher: " + matchesPrimitive.group());
            //if (matchesEnumPrimitive.find()) System.out.println("Enum Matcher: " + matchesEnumPrimitive.group(1));
            if (matchesPrimitive.matches()) {
                //System.out.println("Matching Primitive: " + matchesPrimitive.group(1));
                this.literalDefaultValue = matchesPrimitive.group(1);
                this.defaultValue = Optional.of(new PrimitiveDefaultValue((PrimitiveType) param.getType()));
            }
            if (matchesEnumPrimitive.matches()) {
                //System.out.println("Matching Enum: " + matchesEnumPrimitive.group(1));
                this.literalDefaultValue = matchesEnumPrimitive.group(1);
                //The default value should be assigned
            }
        } else if (param.getType() instanceof PrimitiveType) {
            this.literalDefaultValue = "0";
            this.defaultValue = Optional.of(this.defaultValue.orElse(new PrimitiveDefaultValue((PrimitiveType) param.getType())));
        } else {
            this.defaultValue = Optional.of(this.defaultValue.orElse(new ObjectDefaultValue(param.getType())));
        }
        //System.out.println(param.getType().getClass());
    }

    public boolean isMatch(Parameter param) {
        return type.equals(param.getType().toString());
    }

    private Type getRealType() {
        if (parameter.isPresent()) return parameter.get().getType();
        return createReferenceType(type, 0);
    }

    public Type getType() {
        if (defaultValue.isPresent()) {
            return defaultValue.get().getType().orElse(getRealType());
        }
        return getRealType();
    }

    public Type getTypeBoxedIfPossible() {
        return getType() instanceof PrimitiveType ? ((PrimitiveType) getType()).toBoxedType() : getType();
    }

    private List<NormalAnnotationExpr> getAnnotationsMatchingIfPresent() {
        if (parameter.isPresent() && parameter.get().getAnnotations() != null) {
            Parameter param = parameter.get();
            return param.getAnnotations().stream()
                    .filter(a -> (a.getName().getName().equals("ByVal") || a.getName().getName().equals("ByRef")))
                    .filter(a -> a instanceof NormalAnnotationExpr)
                    .map(a -> (NormalAnnotationExpr) a)
                    .collect(Collectors.toList());
        }
        return null;
    }

    public String getLiteralDefaultValue() {
        List<NormalAnnotationExpr> matchingAnnotations = getAnnotationsMatchingIfPresent();
        if (matchingAnnotations != null) {
            for (NormalAnnotationExpr matching : matchingAnnotations) {
                Optional<MemberValuePair> constructorPair = matching.getPairs().stream().filter(p -> constructorCallExpression.matcher(p.getValue().toString()).find()).findFirst();
                Optional<MemberValuePair> methodPair = matching.getPairs().stream().filter(p -> methodCallExpression.matcher(p.getValue().toString()).find()).findFirst();
//                System.out.println("Constructor: " + constructorPair);
//                System.out.println("Method: " + methodPair);
            }
        }
        return this.literalDefaultValue;
    }

    public ImportDeclaration getImport() {
        if (defaultValue.isPresent()) {
            return defaultValue.get().getImportDeclaration();
        } else {
            return null;
        }
    }

    /**
     * @return True if this para type represents an output.
     */
    public boolean isOutput() {
        return state.equals(DefinedParamState.OUTPUT);
    }

    public String getName() {
        return parameter.get().getId().getName();
    }

    /**
     * Gets the literal expression that should be passed to the opencv method.
     * This allows for any last type conversions or enumerations field accessing
     *
     * @return The Expression to pass to the opencv method.
     */
    public Expression getLiteralExpression() {
        if (defaultValue.isPresent() && defaultValue.get() instanceof EnumDefaultValue) {
            return new FieldAccessExpr(createNameExpr(getName()), "value");
        } else {
            return createNameExpr(getName());
        }
    }

    /**
     * Makes this param an output.
     */
    void setOutput() {
        state = DefinedParamState.OUTPUT;
    }

}
