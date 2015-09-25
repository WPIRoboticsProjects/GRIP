package edu.wpi.gripgenerator.collectors;


import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NullLiteralExpr;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EnumDefaultValue extends DefaultValue {
    private final Set<String> enumConstantNames;

    public EnumDefaultValue(String name, String ...enumConstantNames){
        this(name, new HashSet(Arrays.asList(enumConstantNames)));
    }

    public EnumDefaultValue(String name, Set<String> enumConstantNames){
        super(name);
        this.enumConstantNames = enumConstantNames;
    }

    @Override
    protected Set<String> getDefaultValues() {
        return enumConstantNames;
    }

    @Override
    public Expression getDefaultValue(String defaultValue) {
        return new NullLiteralExpr();
    }

    @Override
    public Expression getDomainValue() {
        return new NullLiteralExpr();
    }


}
