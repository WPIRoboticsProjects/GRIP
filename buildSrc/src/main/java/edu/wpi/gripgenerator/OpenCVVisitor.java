package edu.wpi.gripgenerator;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OpenCVVisitor extends VoidVisitorAdapter<List<CompilationUnit>> {

    enum TestEnum {
        TEST_VALUE(1),
        TEST_VALUE2(2);
        public final int i;

        TestEnum(int i){
            this.i = i;
        }
    }

    private BlockStmt getDefaultConstructorBlockStatement(){
        BlockStmt block = new BlockStmt();
        AssignExpr assignment = new AssignExpr(new FieldAccessExpr(new ThisExpr(), "value"), new NameExpr("value"), AssignExpr.Operator.assign);
        ASTHelper.addStmt(block, assignment);
        return block;
    }

    private CompilationUnit generateFromDeclaration(final FieldDeclaration declaration, String name, Expression parentAccessor){
        // Generate new compilation unit
        CompilationUnit newEnumCu = new CompilationUnit();
        newEnumCu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr("edu.wpi.grip.core.opencv_core.enum")));

        // Create new Enum
        EnumDeclaration newEnum = new EnumDeclaration(ModifierSet.PUBLIC, name);
        Parameter param = ASTHelper.createParameter(ASTHelper.createReferenceType("int", 0), "value");
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(param);

        // Add a constructor
        ConstructorDeclaration enumConstructor = new ConstructorDeclaration(0, null, null, name, parameters, null, getDefaultConstructorBlockStatement());
        ASTHelper.addMember(newEnum, enumConstructor);

        System.out.println(name);
        // Generate the enum constants
        List<EnumConstantDeclaration> enumConstants = new ArrayList<>();
        for (VariableDeclarator var : declaration.getVariables()) {
            // Create the constant
            EnumConstantDeclaration enumConstant = new EnumConstantDeclaration(var.getId().getName());
            List<Expression> expressionList = new ArrayList<>();
            enumConstant.setArgs(expressionList);

            FieldAccessExpr field = new FieldAccessExpr(parentAccessor, var.getId().getName());
            expressionList.add(field);


            // Add the javadoc comment
            if(var.hasComment()) enumConstant.setJavaDoc(new JavadocComment(var.getComment().getContent()));
            enumConstants.add(enumConstant);
        }
        newEnum.setEntries(enumConstants);
        System.out.println(newEnum);

        return newEnumCu;
    }

    public void visit(final FieldDeclaration declaration, final List<CompilationUnit> arg){
        super.visit(declaration, arg);

        Pattern enumRegex = Pattern.compile(".*enum cv::([a-zA-Z_]*)");
        Comment declarationComment = declaration.getComment();
        if(declarationComment != null) {
            Matcher matcher = enumRegex.matcher(declarationComment.toString());
            if (matcher.find()) {
                NameExpr baseClazz = new NameExpr("opencv_core");
                Expression subClass = null;
                if(declaration.getParentNode() instanceof ClassOrInterfaceDeclaration){
                    ClassOrInterfaceDeclaration clazz = (ClassOrInterfaceDeclaration) declaration.getParentNode();
                    if(!clazz.getName().equals("opencv_core")){
                        subClass = new FieldAccessExpr(baseClazz, clazz.getName());
                    } else {
                        subClass = baseClazz;
                    }
                    arg.add(generateFromDeclaration(declaration, matcher.group(1), subClass));
                } else {
                    System.err.println("Parent was not a class? What??");
                }
            }
        }
    }
}
