package edu.wpi.gripgenerator.collectors;


import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.Type;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class EnumDefaultValue extends DefaultValue {
    private final Set<String> enumConstantNames;

    public EnumDefaultValue(String packageName, String name, String ...enumConstantNames){
        this(packageName, name, new HashSet(Arrays.asList(enumConstantNames)));
    }

    public EnumDefaultValue(String packageName, String name, Set<String> enumConstantNames){
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
    public Expression getDomainValue() {
        return new MethodCallExpr(
                new NameExpr(name),
                "values"
        );
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
