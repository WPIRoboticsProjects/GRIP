package edu.wpi.gripgenerator.defaults;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.type.Type;

import java.util.Optional;
import java.util.Set;

public class NullDefaultValue extends DefaultValue {

    public NullDefaultValue() {
        super("", "null");
    }

    @Override
    public ImportDeclaration getImportDeclaration() {
        return null;
    }

    @Override
    protected Set<String> getDefaultValues() {
        return null;
    }

    @Override
    public Expression getDefaultValue(String defaultValue) {
        NullLiteralExpr expr = new NullLiteralExpr();
        expr.setComment(new BlockComment("Intentionally null"));
        return expr;
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
