package edu.wpi.gripgenerator.templates;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.VoidType;
import com.google.common.base.Ascii;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Sets;
import edu.wpi.gripgenerator.defaults.DefaultValueCollector;
import edu.wpi.gripgenerator.settings.DefinedMethod;
import edu.wpi.gripgenerator.settings.DefinedParamType;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.javaparser.ASTHelper.createReferenceType;

public class Operation {
    private static final ImportDeclaration OPERATION_IMPORT = new ImportDeclaration(new NameExpr("edu.wpi.grip.core.operations.opencv.CVOperation"), false, false);
    private static final ImportDeclaration INPUT_SOCKET_IMPORT = new ImportDeclaration(new NameExpr("edu.wpi.grip.core.InputSocket"), false, false);
    private static final ImportDeclaration OUTPUT_SOCKET_IMPORT = new ImportDeclaration(new NameExpr("edu.wpi.grip.core.OutputSocket"), false, false);
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

    /**
     * Constructs an Operation
     *
     * @param collector     The collection of default values.
     * @param definedMethod The method that this operation is wrapping
     * @param className     The name of the class being generated
     */
    public Operation(DefaultValueCollector collector, DefinedMethod definedMethod, String className) {
        this.definedMethod = definedMethod;
        this.packageDec = new PackageDeclaration(new NameExpr("edu.wpi.grip.generated." + className));
        this.operationParams = this.definedMethod.getFinalizedParamTypes(collector);
        this.socketHintDeclarationCollection = new SocketHintDeclarationCollection(collector, this.operationParams);
        this.javadocComment = new JavadocComment(" Operation to call {@link " + className + "#" + definedMethod.getMethodName() + "} ");
    }

    /**
     * Gets the CamelCase formatted name version of the class name.
     * @return The name to be used as the Class and file name.
     */
    public String getOperationClassName() {
        final String name = definedMethod.getMethodName();
        final CaseFormat format;
        if (name.contains("_")) {
            format = CaseFormat.LOWER_UNDERSCORE;
        } else if(Ascii.isLowerCase(name.charAt(0))) {
            format = CaseFormat.LOWER_CAMEL;
        } else if(Ascii.isUpperCase(name.charAt(0))){
            format = CaseFormat.UPPER_CAMEL;
        } else {
            throw new UnsupportedOperationException("Can not convert class name for " + name);
        }
        return format.to(CaseFormat.UPPER_CAMEL, definedMethod.getMethodName());
    }

    private List<ImportDeclaration> getAdditionalImports() {
        List<ImportDeclaration> imports = new ArrayList(Collections.singletonList(new ImportDeclaration(new NameExpr("org.bytedeco.javacpp." + definedMethod.getParentObjectName()), false, false)));
        imports.addAll(this.definedMethod.getImports());
        return imports;
    }

    /**
     * Creates the Name method
     *
     * @return
     */
    private MethodDeclaration getNameMethod() {
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
                        new StringLiteralExpr("CV " + definedMethod.getMethodName())))
        );
        getName.setBody(methodBody);
        return getName;
    }

    /**
     * Creates the description method
     *
     * @return
     */
    private MethodDeclaration getDescriptionMethod() {
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

    private MethodDeclaration getCategoryMethod() {
        MethodDeclaration getCategory = new MethodDeclaration(
                ModifierSet.PUBLIC,
                Collections.singletonList(OVERRIDE_ANNOTATION),
                null,
                createReferenceType("Category", 0),
                "getCategory",
                null, 0,
                null,
                null
        );
        getCategory.setBody(new BlockStmt(Collections.singletonList(new ReturnStmt(new NameExpr("Category.OPENCV")))));
        return getCategory;
    }

    /**
     * Creates the method that returns the input socket of this operation.
     *
     * @return The method declaration.
     */
    private MethodDeclaration getCreateInputSocketsMethod() {
        return new MethodDeclaration(
                ModifierSet.PUBLIC,
                Arrays.asList(OVERRIDE_ANNOTATION, SUPPRESS_ANNOTATION),
                null,
                SocketHintDeclarationCollection.getSocketReturnParam("InputSocket"),
                "createInputSockets",
                Collections.singletonList(
                        new Parameter(createReferenceType("EventBus", 0), new VariableDeclaratorId("eventBus"))
                ),
                0,
                null,
                socketHintDeclarationCollection.getInputSocketBody()
        );
    }

    /**
     * Creates the method that returns the output sockets of this operation.
     *
     * @return The method declaration.
     */
    private MethodDeclaration getCreateOutputSocketsMethod() {
        return new MethodDeclaration(
                ModifierSet.PUBLIC,
                Arrays.asList(OVERRIDE_ANNOTATION, SUPPRESS_ANNOTATION),
                null,
                SocketHintDeclarationCollection.getSocketReturnParam("OutputSocket"),
                "createOutputSockets",
                Collections.singletonList(
                        new Parameter(createReferenceType("EventBus", 0), new VariableDeclaratorId("eventBus"))
                ),
                0,
                null,
                socketHintDeclarationCollection.getOutputSocketBody()
        );
    }


    private Expression getFunctionCallExpression() {
        return new MethodCallExpr(
                new NameExpr(definedMethod.getParentObjectName()),
                definedMethod.getMethodName(),
                operationParams.stream().map(DefinedParamType::getLiteralExpression).collect(Collectors.toList())
        );
    }

    /**
     * Generates the perform function
     *
     * @param inputParamId  The name of the variable for the input to the function
     * @param outputParamId The name of the variable for the output to the function
     * @return The list of statements that are performed inside of the perform function.
     */
    private List<Statement> getPerformExpressionList(String inputParamId, String outputParamId) {
        assert !inputParamId.equals(outputParamId) : "The input and output param can not be the same";
        List<Expression> expressionList = socketHintDeclarationCollection.getSocketAssignments(inputParamId, outputParamId);
        List<Statement> performStatement = expressionList.stream().map(ExpressionStmt::new).collect(Collectors.toList());
        final String exceptionVariable = "e";
        final String outputSocketForEachVariableId = "outputSocket";

        performStatement.addAll(
                Arrays.asList(
                                /* Make the operation function call */
                        new ExpressionStmt(
                                getFunctionCallExpression()
                        ),
                                /*
                                 * Afterwards iterate over all of the output sockets and call setValue using the value
                                 * stored.
                                 */
                        new ForeachStmt(
                                new VariableDeclarationExpr(
                                        0,
                                        ASTHelper.createReferenceType("OutputSocket", 0),
                                        Collections.singletonList(
                                                new VariableDeclarator(
                                                        new VariableDeclaratorId(outputSocketForEachVariableId)
                                                )
                                        )
                                ),
                                new NameExpr(outputParamId),
                                new BlockStmt(
                                        Collections.singletonList(
                                                new ExpressionStmt(
                                                        new MethodCallExpr(
                                                                new NameExpr(outputSocketForEachVariableId),
                                                                "setValueOptional",
                                                                Collections.singletonList(
                                                                        new MethodCallExpr(
                                                                                new NameExpr(outputSocketForEachVariableId),
                                                                                "getValue"
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );


        return performStatement;
    }

    private MethodDeclaration getPerformMethod() {
        String inputParamId = "inputs";
        String outputParamId = "outputs";
        return new MethodDeclaration(
                ModifierSet.PUBLIC,
                Arrays.asList(OVERRIDE_ANNOTATION),
                null,
                new VoidType(),
                "perform",
                Arrays.asList(
                        new Parameter(SocketHintDeclarationCollection.getSocketReturnParam("InputSocket"), new VariableDeclaratorId(inputParamId)),
                        new Parameter(SocketHintDeclarationCollection.getSocketReturnParam("OutputSocket"), new VariableDeclaratorId(outputParamId))
                ),
                0,
                null,
                new BlockStmt(
                        getPerformExpressionList(inputParamId, outputParamId)
                )
        );
    }

    /**
     * Generates the class definition for the Operation
     *
     * @return
     */
    private ClassOrInterfaceDeclaration getClassDeclaration() {
//        System.out.println("Generating: " + getOperationClassName());
//        System.out.println(definedMethod.methodToString());
        ClassOrInterfaceDeclaration operation = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, getOperationClassName());
        operation.setImplements(Collections.singletonList(iOperation));
        operation.setJavaDoc(javadocComment);
        operation.setComment(new BlockComment(
                " * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n" +
                        " * ===== THIS CODE HAS BEEN DYNAMICALLY GENERATED! DO NOT MODIFY! ==== *\n" +
                        " * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * "));
        operation.setMembers(socketHintDeclarationCollection
                .getAllSocketHints()
                .stream()
                .map(SocketHintDeclaration::getDeclaration)
                .filter(declaration -> declaration != null)
                .collect(Collectors.toList()));
        ASTHelper.addMember(operation, getNameMethod());
        ASTHelper.addMember(operation, getDescriptionMethod());
        ASTHelper.addMember(operation, getCategoryMethod());
        ASTHelper.addMember(operation, getCreateInputSocketsMethod());
        ASTHelper.addMember(operation, getCreateOutputSocketsMethod());
        ASTHelper.addMember(operation, getPerformMethod());
        return operation;
    }

    /**
     * Creates the operation declaration
     *
     * @return The operation declaration ready to be turned into a file.
     */
    public CompilationUnit getDeclaration() {
        Set<ImportDeclaration> importList = Sets.newHashSet(
                SocketHintDeclaration.SOCKET_IMPORT,
                OPERATION_IMPORT,
                CV_CORE_IMPORT,
                INPUT_SOCKET_IMPORT,
                OUTPUT_SOCKET_IMPORT,
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
