package edu.wpi.gripgenerator.templates;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import edu.wpi.gripgenerator.settings.DefinedParamType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.javaparser.ASTHelper.createReferenceType;

/**
 * Helps create SocketHint declarations.
 * For example:
 * <code>
 *     SocketHintDeclaration testDeclaration = new SocketHintDeclaration("Mat", Arrays.asList("src1", "src2"));
 *     testDeclaration.getDeclaration().toString()
 * </code>
 * will generate
 * <code>
 *     private final SocketHint<Mat> src1Hint = new SocketHint<Mat>("src1", Mat.class), src2Hint = new SocketHint<Mat>("src2", Mat.class);
 * </code>
 */
public class SocketHintDeclaration {
    public static final String SOCKET_HINT_CLASS_NAME = "SocketHint";
    public static final ImportDeclaration SOCKET_IMPORT = new ImportDeclaration(
            new NameExpr("edu.wpi.grip.core." + SOCKET_HINT_CLASS_NAME), false, false);
    private static final String HINT_POSTFIX = "Hint";
    //End Statics

    private final Type genericType;
    private final List<String> hintNames;
    private final boolean isOutput;

    public SocketHintDeclaration(String genericTypeName, List<String> hintNames, boolean isOutput){
        this(createReferenceType(genericTypeName, 0), hintNames, isOutput);
    }

    public SocketHintDeclaration(Type genericType, List<String> hintNames, boolean isOutput){
        this.genericType = genericType;
        this.hintNames = hintNames;
        this.isOutput = isOutput;
    }


    public SocketHintDeclaration(Type genericType, List<DefinedParamType> hintNameTypes){
        /* Convert this to the 'boxed' type if this is a PrimitiveType */
        this.genericType = genericType instanceof PrimitiveType ? ((PrimitiveType) genericType).toBoxedType() : genericType;
        this.hintNames = new ArrayList();
        this.isOutput = hintNameTypes.get(0).isOutput();
        for(DefinedParamType type : hintNameTypes){
            assert this.isOutput == type.isOutput(): "Mixed input/output defined param types were passed";
            this.hintNames.add(type.getName());
        }
    }

    public boolean isOutput(){
        return this.isOutput;
    }

    /**
     * Creates a socket hint declaration from the constructor.
     * @param additionalParams Any additional params that should be passed to the constructor of the socket hint
     * @return The field declaration
     */
    public FieldDeclaration getDeclaration(List<Expression> additionalParams){;
        final int modifiers = ModifierSet.addModifier(ModifierSet.FINAL, ModifierSet.PRIVATE);

        final ClassOrInterfaceType socketHintType = new ClassOrInterfaceType(SOCKET_HINT_CLASS_NAME);
        socketHintType.setTypeArgs(Collections.singletonList(genericType));

        final List<VariableDeclarator> variableDeclarations = new ArrayList<>();
        for(String hintName : hintNames){
            // The variableId
            final String fullHintName = hintName+HINT_POSTFIX;
            // The name hint of the socket hint
            final StringLiteralExpr stringLiteralExpr = new StringLiteralExpr(hintName);
            final ClassExpr classExpr = new ClassExpr(genericType);

            //Add the additional params to the list for this param set.
            final List<Expression> paramSet = new ArrayList<>(additionalParams);
            paramSet.addAll(0, Arrays.asList(stringLiteralExpr, classExpr));
            variableDeclarations.add(
                    new VariableDeclarator(
                            new VariableDeclaratorId(fullHintName),
                            // Create new instantiation of type socket hint type
                            new ObjectCreationExpr(null, socketHintType, paramSet)));
        }
        return new FieldDeclaration(modifiers, socketHintType, variableDeclarations);
    }

    public FieldDeclaration getDeclaration(){
        return getDeclaration(Collections.emptyList());
    }
}
