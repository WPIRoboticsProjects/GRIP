package edu.wpi.gripgenerator.defaults;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.util.*;
import java.util.stream.Collectors;

public class ObjectDefaultValue extends DefaultValue{
    private final ClassOrInterfaceType type;
    private final List<Expression> defaultValues;

    public ObjectDefaultValue(Type type){
        super("", type.toStringWithoutComments());
        this.type = new ClassOrInterfaceType(type.toStringWithoutComments());
        this.defaultValues = Collections.emptyList();
    }

    public ObjectDefaultValue(String type, String ...defaultValues){
        super("", type);
        this.type = new ClassOrInterfaceType(type);
        this.defaultValues = Arrays.asList(defaultValues).stream().map(NameExpr::new).collect(Collectors.toList());
    }

    @Override
    public ImportDeclaration getImportDeclaration(){
        return null;
    }

    @Override
    protected Set<String> getDefaultValues() {
        return null;
    }

    @Override
    public Expression getDefaultValue(String defaultValue) {
        return new ObjectCreationExpr(null, type, defaultValues);
    }

    @Override
    public Expression getDomainValue() {
        return new NullLiteralExpr();
    }

    @Override
    public String getViewType() {
        return "NONE";
    }

    @Override
    public Optional<Type> getType() {
        return Optional.of(type);
    }
}
