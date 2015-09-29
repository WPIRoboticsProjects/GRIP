package edu.wpi.gripgenerator.defaults;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class PrimitiveDefaultValue extends DefaultValue{
    private PrimitiveType type;
    private String viewValue;
    private Expression domainValue;

    public PrimitiveDefaultValue(PrimitiveType type){
        super("", type.getType().name());
        this.type = type;
        switch (type.getType()){
            case Boolean:
                this.viewValue = "CHECKBOX";
                this.domainValue = createDomainValueExpression(
                        new BooleanLiteralExpr(true),
                        new BooleanLiteralExpr(false)
                );
                break;
            case Int:
                this.viewValue = "SPINNER";
                this.domainValue = createDomainValueExpression(
                        new IntegerLiteralExpr("Integer.MIN_VALUE"),
                        new IntegerLiteralExpr("Integer.MAX_VALUE")
                );
                break;
            case Float:
                this.viewValue = "SPINNER";
                this.domainValue = createDomainValueExpression(
                        new DoubleLiteralExpr("-Float.MAX_VALUE"),
                        new DoubleLiteralExpr("Float.MAX_VALUE")
                );
                break;
            case Double:
                this.viewValue = "SPINNER";
                this.domainValue = createDomainValueExpression(
                        new DoubleLiteralExpr("-Double.MAX_VALUE"),
                        new DoubleLiteralExpr("Double.MAX_VALUE")
                );
                break;
            case Char:
                this.viewValue = "SPINNER";
                this.domainValue = createDomainValueExpression(
                        new CharLiteralExpr(Character.toString(Character.MIN_VALUE)),
                        new CharLiteralExpr(Character.toString(Character.MAX_VALUE))
                );
            default:
                throw new UnsupportedOperationException("Type " + type.getType() + " is not supported.");
        }
    }



    private Expression createDomainValueExpression(Expression ...expressions){
        return new ArrayCreationExpr(type.toBoxedType(), 1,
                new ArrayInitializerExpr(
                        Arrays.asList(expressions)
                )
        );
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
        switch (type.getType()){
            case Boolean: return new BooleanLiteralExpr(Boolean.valueOf(defaultValue));
            case Int: return new IntegerLiteralExpr(Integer.valueOf(defaultValue).toString());
            case Float: return new DoubleLiteralExpr(Float.valueOf(defaultValue).toString());
            case Double: return new DoubleLiteralExpr(Double.valueOf(defaultValue).toString());
            case Char:
                assert defaultValue.length() == 1 : "Invalid default value: " + defaultValue + " for type char";
                return new CharLiteralExpr(defaultValue);
            default: throw new UnsupportedOperationException("Type " + type.getType() + " is not supported.");
        }
    }

    @Override
    public Expression getDomainValue() {
        return domainValue;
    }

    @Override
    public String getViewType() {
        return viewValue;
    }

    @Override
    public Optional<Type> getType() {
        return Optional.of(type);
    }
}
