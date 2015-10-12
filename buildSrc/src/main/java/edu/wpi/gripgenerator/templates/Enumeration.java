package edu.wpi.gripgenerator.templates;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Enumeration {
    private final String importExpression;
    private final String baseClassName;
    private final Optional<String> parentClassName;
    private final String name;
    private final List<VariableDeclarator> declaratorList;

    public Enumeration(String name, PackageDeclaration packageDeclaration, String baseClassName, String parentClassName, List<VariableDeclarator> declaratorList) {
        this.name = name;
        this.importExpression = packageDeclaration.getName().toStringWithoutComments() + ".";
        this.baseClassName = baseClassName;
        this.parentClassName = Optional.ofNullable(parentClassName);
        this.declaratorList = declaratorList;
    }

    /**
     * Gets the class name of the enumeration.
     *
     * @return This should be used as the file name and will also be the name of the enumeration
     */
    public String getEnumerationClassName() {
        return name;
    }

    public PackageDeclaration getPackageDeclaration() {
        return new PackageDeclaration(new NameExpr("edu.wpi.grip.generated." + baseClassName + ".enumeration"));
    }

    /**
     * @return The imports for this enumeration
     */
    private List<ImportDeclaration> generateImports() {
        return Collections.singletonList(
                new ImportDeclaration(ASTHelper.createNameExpr(importExpression + baseClassName), false, false)
        );
    }

    /**
     * Generates the contents of the constructor.
     *
     * @param valueString The string representing the field that this will be assigned to
     * @return The constructor block
     */
    private BlockStmt getDefaultConstructorBlockStatement(String valueString) {
        BlockStmt block = new BlockStmt();
        AssignExpr assignment = new AssignExpr(new FieldAccessExpr(new ThisExpr(), valueString), new NameExpr(valueString), AssignExpr.Operator.assign);
        ASTHelper.addStmt(block, assignment);
        return block;
    }

    /**
     * Uses the VariableDeclarator from the original source code to generate an equivalent
     * constant in the enumeration
     *
     * @param var The variable from the source declaration.
     * @return The generated enum constant
     */
    private EnumConstantDeclaration generateEnumConstant(VariableDeclarator var) {
        // Create the constant
        final NameExpr baseClass = new NameExpr(baseClassName);
        final Expression parentAccessor;
        // If there is a parent class for this enumeration declaration
        if (parentClassName.isPresent()) {
            // Then we need to make the access statement longer
            parentAccessor = new FieldAccessExpr(
                    baseClass,
                    parentClassName.get()
            );
        } else {
            parentAccessor = baseClass;
        }

        // Generate the constant
        EnumConstantDeclaration enumConstant = new EnumConstantDeclaration(
                null,
                var.getId().getName(),
                Collections.singletonList(
                        new FieldAccessExpr(parentAccessor, var.getId().getName())
                ),
                null
        );

        // Add the javadoc comment but only if the original had a javadoc comment.
        if (var.hasComment() && var.getComment() instanceof JavadocComment) {
            enumConstant.setJavaDoc(new JavadocComment(var.getComment().getContent()));
        }
        return enumConstant;
    }

    /**
     * Generates all of the enumeration constants.
     *
     * @return The list of all of the enum constants
     */
    private List<EnumConstantDeclaration> generateEnumConstantDeclarations() {
        return declaratorList
                .stream()
                .map(v -> generateEnumConstant(v))
                .collect(Collectors.toList());
    }

    /**
     * Generates the enumeration class body.
     *
     * @return The generated Enumeration
     */
    private EnumDeclaration generateEnum() {
        final String valueString = "value";
        // Create new Enum
        EnumDeclaration newEnum = new EnumDeclaration(
                ModifierSet.PUBLIC,
                null,
                getEnumerationClassName(),
                null,
                generateEnumConstantDeclarations(),
                null
        );
        // Add a field value for the public final value variable
        FieldDeclaration valueField = new FieldDeclaration(
                ModifierSet.addModifier(ModifierSet.FINAL, ModifierSet.PUBLIC),
                ASTHelper.INT_TYPE,
                Collections.singletonList(
                        new VariableDeclarator(new VariableDeclaratorId(valueString))
                ));
        ASTHelper.addMember(newEnum, valueField);

        // Add the constructor to take the opencv value
        ConstructorDeclaration enumConstructor = new ConstructorDeclaration(
                0,
                null,
                null,
                name,
                Collections.singletonList(
                        ASTHelper.createParameter(ASTHelper.INT_TYPE, valueString)
                ),
                null,
                getDefaultConstructorBlockStatement(valueString)
        );
        ASTHelper.addMember(newEnum, enumConstructor);
        return newEnum;
    }

    /**
     * Kicks off the generation of the enumeration
     * @return The fully compiled CompilationUnit
     */
    public CompilationUnit generateUnit() {
        // Generate new compilation unit
        return new CompilationUnit(
                getPackageDeclaration(),
                generateImports(),
                Collections.singletonList(generateEnum())
        );
    }
}
