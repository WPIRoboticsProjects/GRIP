package edu.wpi.gripgenerator.defaults;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.type.Type;

import java.util.Optional;
import java.util.Set;

public class MethodDefaultValue extends DefaultValue {

    public MethodDefaultValue(String packageName, String name) {
        super(packageName, name);
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
    public String getViewType() {
        return "NONE";
    }

    @Override
    public Optional<Type> getType() {
        return Optional.empty();
    }
}
