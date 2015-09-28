package edu.wpi.gripgenerator.templates;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.PrimitiveType;
import edu.wpi.gripgenerator.collectors.DefaultValue;

import java.util.Arrays;
import java.util.List;

public class SocketHintAdditionalParams {
    private PrimitiveType type;
    private Expression viewValue;
    private Expression domainValue;
    private Expression defaultValue;
    public SocketHintAdditionalParams(PrimitiveType type, String defaultValue){

        this.type = type;
        switch (type.getType()){
            case Boolean:
                this.viewValue = getViewEnumElement("CHECKBOX");
                this.domainValue = createDomainValueExpression(
                        new BooleanLiteralExpr(true),
                        new BooleanLiteralExpr(false)
                );
                this.defaultValue = new BooleanLiteralExpr(Boolean.valueOf(defaultValue));
                break;
            case Int:
                this.viewValue = getViewEnumElement("SPINNER");
                this.domainValue = createDomainValueExpression(
                        new IntegerLiteralExpr("Integer.MIN_VALUE"),
                        new IntegerLiteralExpr("Integer.MAX_VALUE")
                );
                this.defaultValue = new IntegerLiteralExpr(
                        // Ensure that the default value is actually an Integer
                        Integer.valueOf(defaultValue).toString()
                );
                break;
            case Float:
                this.viewValue = getViewEnumElement("SPINNER");
                this.domainValue = createDomainValueExpression(
                        new DoubleLiteralExpr("-Float.MAX_VALUE"),
                        new DoubleLiteralExpr("Float.MAX_VALUE")
                );
                this.defaultValue = new DoubleLiteralExpr(
                        // Ensure that the default value is actually a Float
                        Float.valueOf(defaultValue).toString()
                );
                break;
            case Double:
                this.viewValue = getViewEnumElement("SPINNER");
                this.domainValue = createDomainValueExpression(
                        new DoubleLiteralExpr("-Double.MAX_VALUE"),
                        new DoubleLiteralExpr("Double.MAX_VALUE")
                );
                this.defaultValue = new DoubleLiteralExpr(
                        // Ensure that the default value is actually a Double
                        Double.valueOf(defaultValue).toString()
                );
                break;
            case Char:
                assert defaultValue.length() == 1 : "Invalid default value: " + defaultValue + " for type char";
                this.viewValue = getViewEnumElement("SPINNER");
                this.domainValue = createDomainValueExpression(
                        new CharLiteralExpr(Character.toString(Character.MIN_VALUE)),
                        new CharLiteralExpr(Character.toString(Character.MAX_VALUE))
                );
                this.defaultValue = new CharLiteralExpr(defaultValue);
            default:
                throw new UnsupportedOperationException("Type " + type.getType() + " is not supported.");
        }
    }

    public SocketHintAdditionalParams(DefaultValue defaultValue, String defaultValueString){
        this.viewValue = getViewEnumElement(defaultValue.getViewType());
        this.domainValue = defaultValue.getDomainValue();
        this.defaultValue = defaultValue.getDefaultValue(defaultValueString);
    }

    private FieldAccessExpr getViewEnumElement(String value){
        return new FieldAccessExpr(
                    new NameExpr("SocketHint.View"),
                    value
                );
    }

    private Expression createDomainValueExpression(Expression ...expressions){
        return new ArrayCreationExpr(type.toBoxedType(), 1,
                new ArrayInitializerExpr(
                        Arrays.asList(expressions)
                )
        );
    }

    public List<Expression> getHintAdditionalParams(){
        return Arrays.asList(
                viewValue,
                domainValue
        );
    }

    public List<Expression> getAdditionalParams(){
        return Arrays.asList(defaultValue);
    }
}
