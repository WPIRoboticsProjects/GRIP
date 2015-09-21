package edu.wpi.gripgenerator.templates;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.*;
import edu.wpi.gripgenerator.settings.DefinedMethod;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.github.javaparser.ASTHelper.createReferenceType;

public class Operation {
    private static final ImportDeclaration OPERATION_IMPORT = new ImportDeclaration(new NameExpr("edu.wpi.grip.core.Operation"), false, false);
    private static final ImportDeclaration SOCKET_IMPORT = new ImportDeclaration(new NameExpr("edu.wpi.grip.core.Socket"), false, false);
    private static final ImportDeclaration EVENT_BUS_IMPORT = new ImportDeclaration(new NameExpr("com.google.common.eventbus.EventBus"), false, false);
    private static final ImportDeclaration CV_CORE_IMPORT = new ImportDeclaration(new NameExpr("org.bytedeco.javacpp.opencv_core"), true, true);
    private static final ClassOrInterfaceType iOperation = new ClassOrInterfaceType("Operation");
    private static final AnnotationExpr OVERRIDE_ANNOTATION = new MarkerAnnotationExpr(new NameExpr("Override"));
    private final DefinedMethod definedMethod;
    private final PackageDeclaration packageDec;
    private final JavadocComment javadocComment;

    public Operation(DefinedMethod definedMethod, String className){
        this.definedMethod = definedMethod;
        this.packageDec = new PackageDeclaration(new NameExpr("edu.wpi.grip.generated." + className));
        this.javadocComment = new JavadocComment("Operaion to call the " + className + " method " + definedMethod.getMethodName());
    }

    private MethodDeclaration getNameMethod(){
        MethodDeclaration getName = new MethodDeclaration(
                ModifierSet.PUBLIC,
                Arrays.asList(OVERRIDE_ANNOTATION),
                null,
                createReferenceType("String", 0),
                "getName",
                null, 0,
                null,
                null
        );
        BlockStmt methodBody = new BlockStmt(
                Collections.singletonList(new ReturnStmt(
                        new StringLiteralExpr(definedMethod.getMethodName())))
        );
        getName.setBody(methodBody);
        return getName;
    }

    private MethodDeclaration getDescriptionMethod(){
        MethodDeclaration getDescription = new MethodDeclaration(
                ModifierSet.PUBLIC,
                Arrays.asList(OVERRIDE_ANNOTATION),
                null,
                createReferenceType("String", 0),
                "getDescription",
                null, 0,
                null,
                null
        );
        BlockStmt methodBody = new BlockStmt(
                Collections.singletonList(new ReturnStmt(
                        new StringLiteralExpr("No Description Yet")))
        );
        getDescription.setBody(methodBody);
        return getDescription;
    }

    private MethodDeclaration getCreateInputSocketsMethod(){
        MethodDeclaration createInputSockets = new MethodDeclaration(
                ModifierSet.PUBLIC,
                Arrays.asList(OVERRIDE_ANNOTATION),
                null,
                SocketHintDeclarationCollection.getSocketReturnParam(),
                "createInputSockets",
                Arrays.asList(
                        new Parameter(createReferenceType("EventBus", 0), new VariableDeclaratorId("eventBus"))
                ),
                0,
                null,
                definedMethod.getSocketHintsCollection().getInputSocketBody()
        );
        return createInputSockets;
    }

    private MethodDeclaration getCreateOutputSocketsMethod(){
        MethodDeclaration createOutputSockets = new MethodDeclaration(
                ModifierSet.PUBLIC,
                Arrays.asList(OVERRIDE_ANNOTATION),
                null,
                SocketHintDeclarationCollection.getSocketReturnParam(),
                "createOutputSockets",
                Arrays.asList(
                        new Parameter(createReferenceType("EventBus", 0), new VariableDeclaratorId("eventBus"))
                ),
                0,
                null,
                null
        );
        BlockStmt methodBody = new BlockStmt(
                Collections.singletonList(new ReturnStmt(new NullLiteralExpr()))
        );
        createOutputSockets.setBody(methodBody);
        return createOutputSockets;
    }

    private MethodDeclaration getPerformMethod(){
        MethodDeclaration perform = new MethodDeclaration(
                ModifierSet.PUBLIC,
                Arrays.asList(OVERRIDE_ANNOTATION),
                null,
                new VoidType(),
                "perform",
                Arrays.asList(
                        new Parameter(SocketHintDeclarationCollection.getSocketReturnParam(), new VariableDeclaratorId("inputs")),
                        new Parameter(SocketHintDeclarationCollection.getSocketReturnParam(), new VariableDeclaratorId("outputs"))
                ),
                0,
                null,
                new BlockStmt(
                        Arrays.asList(new AssertStmt(new BooleanLiteralExpr(false), new StringLiteralExpr("This function has not been implemented yet")))
                )
        );
        return perform;
    }

    public ClassOrInterfaceDeclaration getClassDeclaration(){
        ClassOrInterfaceDeclaration operation = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, definedMethod.getMethodName());
        operation.setImplements(Arrays.asList(iOperation));
        operation.setJavaDoc(javadocComment);
        operation.setComment(new BlockComment("===== THIS CODE HAS BEEN DYNAMICALLY GENERATED! DO NOT MODIFY! ===="));
        operation.setMembers(definedMethod.getSocketHintsCollection().getAllSocketHints().stream().map(d -> d.getDeclaration()).collect(Collectors.toList()));
        ASTHelper.addMember(operation, getNameMethod());
        ASTHelper.addMember(operation, getDescriptionMethod());
        ASTHelper.addMember(operation, getCreateInputSocketsMethod());
        ASTHelper.addMember(operation, getCreateOutputSocketsMethod());
        ASTHelper.addMember(operation, getPerformMethod());
        return operation;
    }

    public CompilationUnit getDeclaration(){
        CompilationUnit unit = new CompilationUnit(
                packageDec,
                Arrays.asList(
                        SocketHintDeclaration.SOCKET_IMPORT,
                        OPERATION_IMPORT,
                        CV_CORE_IMPORT,
                        SOCKET_IMPORT,
                        EVENT_BUS_IMPORT
                ),
                Arrays.asList(getClassDeclaration())
                );
        return unit;
    }
}
