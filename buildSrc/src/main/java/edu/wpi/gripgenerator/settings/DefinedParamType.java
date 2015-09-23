package edu.wpi.gripgenerator.settings;


import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
