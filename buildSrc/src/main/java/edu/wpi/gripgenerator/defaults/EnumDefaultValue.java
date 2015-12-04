package edu.wpi.gripgenerator.defaults;


import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.Type;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Constructs a Default Value for an enumeration.
 */
public class EnumDefaultValue extends DefaultValue {
    private final Set<String> enumConstantNames;

    public EnumDefaultValue(String packageName, String name, String... enumConstantNames) {
        this(packageName, name, new HashSet(Arrays.asList(enumConstantNames)));
    }

    public EnumDefaultValue(String packageName, String name, Set<String> enumConstantNames) {
        super(packageName, name);
        this.enumConstantNames = enumConstantNames;
    }

    @Override
    protected Set<String> getDefaultValues() {
        return enumConstantNames;
    }

    @Override
    public Expression getDefaultValue(String defaultValue) {
        return new FieldAccessExpr(
                new NameExpr(name),
                defaultValue
        );
    }

    @Override
    public String getSocketBuilderInitalValueMethodNameToUse() {
        return "initialValue";
    }

    @Override
    public Optional<Expression> getDomainValue() {
        return Optional.of(new MethodCallExpr(
                new NameExpr(name),
                "values"
        ));
    }

    @Override
    public String getViewType() {
        return "SELECT";
    }

    @Override
    public Optional<Type> getType() {
        return Optional.of(ASTHelper.createReferenceType(this.name, 0));
    }


}
