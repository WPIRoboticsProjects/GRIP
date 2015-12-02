package edu.wpi.gripgenerator.defaults;


import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.Type;

import java.util.Optional;
import java.util.Set;

public abstract class DefaultValue {
    protected final String packageName;
    protected final String name;

    public DefaultValue(String packageName, String name) {
        this.packageName = packageName;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Default values that this default value can be mapped to
     *
     * @return
     */
    protected abstract Set<String> getDefaultValues();

    /**
     * The import declaration that is required for this default value to work.
     *
     * @return The import declaration or null if none is needed
     */
    public ImportDeclaration getImportDeclaration() {
        return new ImportDeclaration(new NameExpr(packageName + "." + this.getName()), false, false);
    }

    public String getSocketBuilderInitalValueMethodNameToUse() {
        return "initialValueSupplier";
    }

    /**
     * Gets the expression for the given default value
     *
     * @param defaultValue The helper default value to define the exact expression to return.
     *                     For example, with enums this will be the enumeration declaration name that is the default.
     * @return The expression accessing the default value.
     */
    public abstract Expression getDefaultValue(String defaultValue);

    /**
     * Returns the domain values for this default value.
     *
     * @return The expression defining the given domain.
     */
    public Optional<Expression> getDomainValue() {
        return Optional.empty();
    }

    /**
     * @return
     */
    public abstract String getViewType();

    /**
     * Gets the type of this default value
     *
     * @return
     */
    public abstract Optional<Type> getType();
}
