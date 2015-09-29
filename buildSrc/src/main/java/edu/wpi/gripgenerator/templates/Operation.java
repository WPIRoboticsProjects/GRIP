package edu.wpi.gripgenerator.templates;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.VoidType;
import com.google.common.collect.Sets;
import edu.wpi.gripgenerator.defaults.DefaultValueCollector;
import edu.wpi.gripgenerator.settings.DefinedMethod;
import edu.wpi.gripgenerator.settings.DefinedParamType;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.javaparser.ASTHelper.createReferenceType;

public class Operation {
    private static final ImportDeclaration OPERATION_IMPORT = new ImportDeclaration(new NameExpr("edu.wpi.grip.core.operations.opencv.CVOperation"), false, false);
    private static final ImportDeclaration SOCKET_IMPORT = new ImportDeclaration(new NameExpr("edu.wpi.grip.core.Socket"), false, false);
    private static final ImportDeclaration EVENT_BUS_IMPORT = new ImportDeclaration(new NameExpr("com.google.common.eventbus.EventBus"), false, false);
    private static final ImportDeclaration CV_CORE_IMPORT = new ImportDeclaration(new NameExpr("org.bytedeco.javacpp.opencv_core"), true, true);
    private static final ClassOrInterfaceType iOperation = new ClassOrInterfaceType("CVOperation");
    private static final AnnotationExpr OVERRIDE_ANNOTATION = new MarkerAnnotationExpr(new NameExpr("Override"));
    private static final AnnotationExpr SUPPRESS_ANNOTATION = new SingleMemberAnnotationExpr(new NameExpr("SuppressWarnings"), new StringLiteralExpr("unchecked"));
    private final DefinedMethod definedMethod;
    private final PackageDeclaration packageDec;
    private final SocketHintDeclarationCollection socketHintDeclarationCollection;
    private final JavadocComment javadocComment;
    private final List<DefinedParamType> operationParams;

    public Operation(DefaultValueCollector collector, DefinedMethod definedMethod, String className){
        this.definedMethod = definedMethod;
        this.packageDec = new PackageDeclaration(new NameExpr("edu.wpi.grip.generated." + className));
        this.operationParams = this.definedMethod.getFinalizedParamTypes(collector);
        this.socketHintDeclarationCollection = new SocketHintDeclarationCollection(collector, this.operationParams);
        this.javadocComment = new JavadocComment(" Operation to call {@link " + className + "#" + definedMethod.getMethodName() + "} ");
    }

    public String getOperationName(){
        return definedMethod.getMethodName();
    }

    private List<ImportDeclaration> getAdditionalImports(){
        List<ImportDeclaration> imports = new ArrayList(Collections.singletonList(new ImportDeclaration(new NameExpr("org.bytedeco.javacpp." + definedMethod.getParentObjectName()), false, false)));
        imports.addAll(this.definedMethod.getImports());
        return imports;
    }

    private MethodDeclaration getNameMethod(){
        MethodDeclaration getName = new MethodDeclaration(
                ModifierSet.PUBLIC,
                Collections.singletonList(OVERRIDE_ANNOTATION),
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
                Collections.singletonList(OVERRIDE_ANNOTATION),
                null,
                createReferenceType("String", 0),
                "getDescription",
                null, 0,
                null,
                null
        );
        BlockStmt methodBody = new BlockStmt(
                Collections.singletonList(new ReturnStmt(
                        new StringLiteralExpr(this.definedMethod.getDescription())))
        );
        getDescription.setBody(methodBody);
        return getDescription;
    }

    private MethodDeclaration getCreateInputSocketsMethod(){
        return new MethodDeclaration(
                ModifierSet.PUBLIC,
                Arrays.asList(OVERRIDE_ANNOTATION, SUPPRESS_ANNOTATION),
                null,
                SocketHintDeclarationCollection.getSocketReturnParam(),
                "createInputSockets",
                Collections.singletonList(
                        new Parameter(createReferenceType("EventBus", 0), new VariableDeclaratorId("eventBus"))
                ),
                0,
                null,
                socketHintDeclarationCollection.getInputSocketBody()
        );
    }

    private MethodDeclaration getCreateOutputSocketsMethod(){
        return new MethodDeclaration(
                ModifierSet.PUBLIC,
                Arrays.asList(OVERRIDE_ANNOTATION, SUPPRESS_ANNOTATION),
                null,
                SocketHintDeclarationCollection.getSocketReturnParam(),
                "createOutputSockets",
                Collections.singletonList(
                        new Parameter(createReferenceType("EventBus", 0), new VariableDeclaratorId("eventBus"))
                ),
                0,
                null,
                socketHintDeclarationCollection.getOutputSocketBody()
        );
    }


    private Expression getFunctionCallExpression(){
        return new MethodCallExpr(
                new NameExpr(definedMethod.getParentObjectName()),
                definedMethod.getMethodName(),
                operationParams.stream().map(DefinedParamType::getLiteralExpression).collect(Collectors.toList())
        );
    }

    private List<Statement> getPerformExpressionList(String inputParamId, String outputParamId){
        List<Expression> expressionList = socketHintDeclarationCollection.getSocketAssignments(inputParamId, outputParamId);
        expressionList.add(getFunctionCallExpression());
        return expressionList.stream().map(ExpressionStmt::new).collect(Collectors.toList());
    }

    private MethodDeclaration getPerformMethod(){
        String inputParamId = "inputs";
        String outputParamId = "outputs";
        return new MethodDeclaration(
                ModifierSet.PUBLIC,
                Arrays.asList(OVERRIDE_ANNOTATION),
                null,
                new VoidType(),
                "perform",
                Arrays.asList(
                        new Parameter(SocketHintDeclarationCollection.getSocketReturnParam(), new VariableDeclaratorId(inputParamId)),
                        new Parameter(SocketHintDeclarationCollection.getSocketReturnParam(), new VariableDeclaratorId(outputParamId))
                ),
                0,
                null,
                new BlockStmt(
                        getPerformExpressionList(inputParamId, outputParamId)
                )
        );
    }

    public ClassOrInterfaceDeclaration getClassDeclaration(){
        System.out.println("Generating: " + getOperationName());
        System.out.println(definedMethod.methodToString());
        ClassOrInterfaceDeclaration operation = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, getOperationName());
        operation.setImplements(Collections.singletonList(iOperation));
        operation.setJavaDoc(javadocComment);
        operation.setComment(new BlockComment(
                  " * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n" +
                " * ===== THIS CODE HAS BEEN DYNAMICALLY GENERATED! DO NOT MODIFY! ==== *\n" +
                " * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * "));
        operation.setMembers(socketHintDeclarationCollection.getAllSocketHints().stream().map(SocketHintDeclaration::getDeclaration).collect(Collectors.toList()));
        ASTHelper.addMember(operation, getNameMethod());
        ASTHelper.addMember(operation, getDescriptionMethod());
        ASTHelper.addMember(operation, getCreateInputSocketsMethod());
        ASTHelper.addMember(operation, getCreateOutputSocketsMethod());
        ASTHelper.addMember(operation, getPerformMethod());
        return operation;
    }

    public CompilationUnit getDeclaration(){
        Set<ImportDeclaration> importList = Sets.newHashSet(
                SocketHintDeclaration.SOCKET_IMPORT,
                OPERATION_IMPORT,
                CV_CORE_IMPORT,
                SOCKET_IMPORT,
                EVENT_BUS_IMPORT
        );
        importList.addAll(getAdditionalImports());
        return new CompilationUnit(
                packageDec,
                new ArrayList<>(importList),
                Collections.singletonList(getClassDeclaration())
        );
    }
}
