package edu.wpi.gripgenerator;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OpenCVEnumVisitor extends VoidVisitorAdapter<Map<String, CompilationUnit>> {
    private static final String enumNamePostFix = "Enum";
    private static final String PACKAGE_EXPRESSION = "edu.wpi.grip.generated.opencv_core.enumeration";
    private static final String IMPORT_EXPRESSION = "org.bytedeco.javacpp.opencv_core";
    /**
     * @see <a href=http://fiddle.re/e0ek86>Regex Example</a>
     * */
    private static final Pattern ENUM_REGEX = Pattern.compile(".*enum cv::([a-zA-Z_]*)?:?:?\\s?([a-zA-Z_]*)");
    private static final String BASE_CLASS_NAME = "opencv_core";

    /**
     * Generates the contents of the constructor.
     * @param valueString The string representing the field that this will be assigned to
     * @return
     */
    private BlockStmt getDefaultConstructorBlockStatement(String valueString){
        BlockStmt block = new BlockStmt();
        AssignExpr assignment = new AssignExpr(new FieldAccessExpr(new ThisExpr(), valueString), new NameExpr(valueString), AssignExpr.Operator.assign);
        ASTHelper.addStmt(block, assignment);
        return block;
    }

    private EnumConstantDeclaration generateEnumConstant(VariableDeclarator var, Expression parentAccessor){
        // Create the constant
        EnumConstantDeclaration enumConstant = new EnumConstantDeclaration(var.getId().getName());
        List<Expression> expressionList = new ArrayList<>();
        enumConstant.setArgs(expressionList);

        FieldAccessExpr field = new FieldAccessExpr(parentAccessor, var.getId().getName());
        expressionList.add(field);

        // Add the javadoc comment
        if(var.hasComment()) enumConstant.setJavaDoc(new JavadocComment(var.getComment().getContent()));
        return enumConstant;
    }

    private void addEnumConstants(EnumDeclaration enumDec, FieldDeclaration declaration, Expression parentAccessor){
        // Generate the enum constants
        List<EnumConstantDeclaration> enumConstants = enumDec.getEntries() == null ? new ArrayList<>() : enumDec.getEntries();
        for (VariableDeclarator var : declaration.getVariables()) {
            enumConstants.add(generateEnumConstant(var, parentAccessor));
        }
        enumDec.setEntries(enumConstants);

    }

    private CompilationUnit generateFromDeclaration(final FieldDeclaration declaration, String name, Expression parentAccessor){
        final String valueString = "value";

        // Generate new compilation unit
        CompilationUnit newEnumCu = new CompilationUnit();
        newEnumCu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr(PACKAGE_EXPRESSION)));
        newEnumCu.setImports(Collections.singletonList(new ImportDeclaration(ASTHelper.createNameExpr(IMPORT_EXPRESSION), false, false)));

        // Create new Enum
        EnumDeclaration newEnum = new EnumDeclaration(ModifierSet.PUBLIC, name);

        VariableDeclarator valueDeclaration = new VariableDeclarator(new VariableDeclaratorId(valueString));
        FieldDeclaration valueField = new FieldDeclaration(ModifierSet.addModifier(ModifierSet.FINAL, ModifierSet.PUBLIC), null, ASTHelper.INT_TYPE, Collections.singletonList(valueDeclaration));
        ASTHelper.addMember(newEnum, valueField);

        // Add a constructor
        Parameter param = ASTHelper.createParameter(ASTHelper.INT_TYPE, valueString);
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(param);


        ConstructorDeclaration enumConstructor = new ConstructorDeclaration(0, null, null, name, parameters, null, getDefaultConstructorBlockStatement(valueString));
        ASTHelper.addMember(newEnum, enumConstructor);

        addEnumConstants(newEnum, declaration, parentAccessor);

        ASTHelper.addTypeDeclaration(newEnumCu, newEnum);
        return newEnumCu;
    }

    public void visit(final FieldDeclaration declaration, final Map<String, CompilationUnit> arg){
        super.visit(declaration, arg);

        Comment declarationComment = declaration.getComment();
        if(declarationComment != null) {
            Matcher matcher = ENUM_REGEX.matcher(declarationComment.toString());
            if (matcher.find()) {
                //This is the base class we are trying to find.
                NameExpr baseClazz = new NameExpr(BASE_CLASS_NAME);
                if(declaration.getParentNode() instanceof ClassOrInterfaceDeclaration){
                    Expression subClass;
                    ClassOrInterfaceDeclaration clazz = (ClassOrInterfaceDeclaration) declaration.getParentNode();
                    if(!clazz.getName().equals(baseClazz.getName())){
                        subClass = new FieldAccessExpr(baseClazz, clazz.getName());
                    } else {
                        subClass = baseClazz;
                    }

                    String name = matcher.group(1) + matcher.group(2) + enumNamePostFix;
                    if(!arg.containsKey(name)) {
                        // This is where the enum is generated
                        arg.put(name, generateFromDeclaration(declaration, name, subClass));
                    } else {
                        CompilationUnit existingEnum = arg.get(name);
                        addEnumConstants((EnumDeclaration) existingEnum.getTypes().get(0), declaration, subClass);
                    }
                } else {
                    throw new Error("Parent of Enum declaration was not a ClassOrInterfaceDeclaration");
                }
            }
        }
    }
}
