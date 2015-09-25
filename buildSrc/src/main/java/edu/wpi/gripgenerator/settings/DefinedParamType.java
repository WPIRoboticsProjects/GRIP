package edu.wpi.gripgenerator.settings;


import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import edu.wpi.gripgenerator.templates.SocketHintAdditionalParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.javaparser.ASTHelper.createReferenceType;

public class DefinedParamType {
    public enum DefinedParamState{
        INPUT,
        OUTPUT
    }

    private final String type;
    private DefinedParamState state;
    private Optional<Parameter> parameter = Optional.empty();

    /**
     * Defines a Param type that a method must match.
     * Defaults the value to being an input.
     * @param type
     */
    public DefinedParamType(String type){
        this(type, DefinedParamState.INPUT);
    }

    public DefinedParamType(String type, Parameter param){
        this(type);
        parameter = Optional.of(param);
    }

    public DefinedParamType(String type, DefinedParamState state){
        this.type = type;
        this.state = state;
    }

    public static List<DefinedParamType> fromStrings(List<String> params){
        List<DefinedParamType> paramStates = new ArrayList<>();
        for(String param : params){
            paramStates.add(new DefinedParamType(param));
        }
        return paramStates;
    }

    void setParamAs(Parameter param){
        this.parameter = Optional.of(param);
    }

    public boolean isMatch(Parameter param){
        return type.equals(param.getType().toString());
    }

    public Type getType(){
        if(parameter.isPresent()) return parameter.get().getType();
        return createReferenceType(type, 0);
    }

    private List<NormalAnnotationExpr> getAnnotationsMatchingIfPresent(){
        if (parameter.isPresent() && parameter.get().getAnnotations() != null) {
            Parameter param = parameter.get();
            return param.getAnnotations().stream()
                    .filter(a -> (a.getName().getName().equals("ByVal") || a.getName().getName().equals("ByRef")))
                    .filter(a -> a instanceof NormalAnnotationExpr)
                    .map(a-> (NormalAnnotationExpr)a)
                    .collect(Collectors.toList());
        }
        return null;
    }

    public List<Expression> getSocketHintAdditionalParams(){
        Pattern constructorCallExpression = Pattern.compile(".*cv?:?:([A-Z][a-zA-Z_]*)\\(.*\\)");
        Pattern methodCallExpression = Pattern.compile(".*cv?:?:([a-z][a-zA-Z_]*)\\(.*\\)");
        if(parameter.isPresent()){

            Parameter param = parameter.get();
            System.out.println("Param: " + param);
            System.out.println("Param w/out comments: " + param.toStringWithoutComments());
            System.out.println("Comment: " + param.getComment());
            System.out.println("Orphan Comments: " + param.getOrphanComments());
            Pattern defaultPrimitive = Pattern.compile(".*=([\\-a-zA-Z0-9_\\.]*)");

            if(param.getComment()!= null && param.getType() instanceof PrimitiveType){
                System.out.println("Checking match");
                Matcher matchesPrimitive = defaultPrimitive.matcher(param.getComment().getContent());
                if(matchesPrimitive.find()) System.out.println(matchesPrimitive.group());
                if(matchesPrimitive.matches()) {
                    System.out.println("Matching: " + matchesPrimitive.group(1));
                    return new SocketHintAdditionalParams((PrimitiveType) param.getType(), matchesPrimitive.group(1)).getAdditionalParams();
                }
            }
        }
        List<NormalAnnotationExpr> matchingAnnotations = getAnnotationsMatchingIfPresent();
        if (matchingAnnotations != null){
            for (NormalAnnotationExpr matching : matchingAnnotations) {
                Optional<MemberValuePair> constructorPair = matching.getPairs().stream().filter(p -> constructorCallExpression.matcher(p.getValue().toString()).find()).findFirst();
                Optional<MemberValuePair> methodPair = matching.getPairs().stream().filter(p -> methodCallExpression.matcher(p.getValue().toString()).find()).findFirst();
                System.out.println("Constructor: " + constructorPair);
                System.out.println("Method: " + methodPair);
            }
        }
        if(parameter.isPresent()){
            System.out.println();
        }
        return Collections.emptyList();
    }

    public List<Expression> getSocketAdditionalParams(){
        return null;
    }

    public boolean isOutput(){
        return state.equals(DefinedParamState.OUTPUT);
    }

    public String getName(){
        return parameter.get().getId().getName();
    }

    void setOutput(){
        state = DefinedParamState.OUTPUT;
    }

}
