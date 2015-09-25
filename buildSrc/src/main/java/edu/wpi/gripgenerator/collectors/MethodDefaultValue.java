package edu.wpi.gripgenerator.collectors;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NullLiteralExpr;

import java.util.Set;

public class MethodDefaultValue extends DefaultValue {

    public MethodDefaultValue(String name){
        super(name);
    }

    @Override
    protected Set<String> getDefaultValues() {
        return null;
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
