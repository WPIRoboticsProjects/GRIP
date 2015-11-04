package edu.wpi.gripgenerator.templates;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import edu.wpi.gripgenerator.defaults.DefaultValue;
import edu.wpi.gripgenerator.defaults.DefaultValueCollector;
import edu.wpi.gripgenerator.settings.DefinedParamType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.javaparser.ASTHelper.createReferenceType;

/**
 * Helps create SocketHint declarations.
 * For example:
 * <code>
 * SocketHintDeclaration testDeclaration = new SocketHintDeclaration("Mat", Arrays.asList("src1", "src2"));
 * testDeclaration.getDeclaration().toString()
 * </code>
 * will generate
 * <code>
 * private final SocketHint<Mat> src1Hint = new SocketHint<Mat>("src1", Mat.class), src2Hint = new SocketHint<Mat>("src2", Mat.class);
 * </code>
 */
public class SocketHintDeclaration {
    public static final String SOCKET_HINT_CLASS_NAME = "SocketHint";
    public static final ImportDeclaration SOCKET_IMPORT = new ImportDeclaration(
            new NameExpr("edu.wpi.grip.core." + SOCKET_HINT_CLASS_NAME), false, false);
    public static final String HINT_POSTFIX = "Hint";
    public static final String INPUT_POSTFIX = "Input";
    public static final String OUTPUT_POSTFIX = "Output";
    //End Statics

    private final Type genericType;
    private final List<DefinedParamType> paramTypes;
    private final boolean isOutput;
    private final DefaultValueCollector collector;

    /**
     * USED ONLY IN TESTING!
     *
     * @param genericTypeName
     * @param hintNames
     * @param isOutput
     */
    public SocketHintDeclaration(String genericTypeName, List<String> hintNames, boolean isOutput) {
        this(null, createReferenceType(genericTypeName, 0), hintNames, isOutput);
    }

    public SocketHintDeclaration(DefaultValueCollector collector, Type genericType, List<String> hintNames, boolean isOutput) {
        this.genericType = genericType;
        this.paramTypes = hintNames.stream().map(n -> new DefinedParamType(
                        genericType.toStringWithoutComments(),
                        new Parameter(genericType, new VariableDeclaratorId(n)))
        ).collect(Collectors.toList());
        this.isOutput = isOutput;
        this.collector = collector;
    }


    public SocketHintDeclaration(DefaultValueCollector collector, Type genericType, List<DefinedParamType> paramTypes, DefinedParamType.DefinedParamDirection state) {
        /* Convert this to the 'boxed' type if this is a PrimitiveType */
        this.genericType =
                genericType instanceof PrimitiveType ?
                        ((PrimitiveType) genericType).getType().equals(PrimitiveType.Primitive.Boolean) ?
                                ((PrimitiveType) genericType).getType().toBoxedType() :
                                ASTHelper.createReferenceType("Number", 0)
                        : genericType;
        this.paramTypes = paramTypes;
        this.isOutput = state.isOutput();
        this.collector = collector;
    }

    public boolean isOutput() {
        return this.isOutput;
    }

    private FieldAccessExpr getViewEnumElement(String value) {
        return new FieldAccessExpr(
                new NameExpr("SocketHint.View"),
                value
        );
    }

    private List<Expression> getSocketHintAdditionalParams(DefinedParamType paramType) {
        if (paramType.getDefaultValue().isPresent()) {
            DefaultValue defaultValue = paramType.getDefaultValue().get();
            return Arrays.asList(
                    paramType.getDefaultValue().isPresent() ?
                            paramType.getDefaultValue().get().getDefaultValue(paramType.getLiteralDefaultValue()) :
                            new NullLiteralExpr(),
                    getViewEnumElement(defaultValue.getViewType()),
                    defaultValue.getDomainValue()
            );

        }
        return Collections.emptyList();
    }

    /**
     * Creates a socket hint declaration from the constructor.
     *
     * @return The field declaration
     */
    public FieldDeclaration getDeclaration() {
        final int modifiers = ModifierSet.addModifier(ModifierSet.FINAL, ModifierSet.PRIVATE);

        final ClassOrInterfaceType socketHintType = new ClassOrInterfaceType(SOCKET_HINT_CLASS_NAME);
        socketHintType.setTypeArgs(Collections.singletonList(genericType));

        final List<VariableDeclarator> variableDeclarations = new ArrayList<>();
        for (DefinedParamType paramType : paramTypes) {
            // Don't generate hint for ignored param
            if (paramType.isIgnored()) continue;

            String hintName = paramType.getName();
            // The variableId
            final String fullHintName = hintName
                    + (isOutput() ? OUTPUT_POSTFIX : INPUT_POSTFIX)
                    + HINT_POSTFIX;
            // The name hint of the socket hint
            final StringLiteralExpr stringLiteralExpr = new StringLiteralExpr(hintName);
            final ClassExpr classExpr = new ClassExpr(genericType);

            //Add the additional params to the list for this param set.
            final List<Expression> paramSet = new ArrayList<>(getSocketHintAdditionalParams(paramType));
            paramSet.addAll(0, Arrays.asList(stringLiteralExpr, classExpr));
            variableDeclarations.add(
                    new VariableDeclarator(
                            new VariableDeclaratorId(fullHintName),
                            // Create new instantiation of type socket hint type
                            new ObjectCreationExpr(null, socketHintType, paramSet)));
        }
        if (variableDeclarations.isEmpty()) return null;

        return new FieldDeclaration(modifiers, socketHintType, variableDeclarations);
    }
}
