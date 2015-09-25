package edu.wpi.gripgenerator.collectors;


import com.github.javaparser.ast.expr.Expression;

import java.util.Set;

public abstract class DefaultValue {
    protected final String name;
    public DefaultValue(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    protected abstract Set<String> getDefaultValues();

    public abstract Expression getDefaultValue(String defaultValue);

    public abstract Expression getDomainValue();
}
