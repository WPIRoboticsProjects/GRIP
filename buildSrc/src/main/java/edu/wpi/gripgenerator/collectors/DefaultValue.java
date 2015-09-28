package edu.wpi.gripgenerator.collectors;


import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.Type;

import java.util.Optional;
import java.util.Set;

public abstract class DefaultValue {
    protected final String packageName;
    protected final String name;
    public DefaultValue(String packageName, String name){
        this.packageName = packageName;
        this.name = name;
    }

    public String getName(){
        return name;
    }

    protected abstract Set<String> getDefaultValues();

    public ImportDeclaration getImportDeclaration(){
        return new ImportDeclaration(new NameExpr(packageName + "." + this.getName()), false, false);
    }

    public abstract Expression getDefaultValue(String defaultValue);

    public abstract Expression getDomainValue();

    public abstract String getViewType();

    public abstract Optional<Type> getType();
}
