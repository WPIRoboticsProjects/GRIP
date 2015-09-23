package edu.wpi.gripgenerator.templates;

import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.WildcardType;
import edu.wpi.gripgenerator.settings.DefinedParamType;

import java.util.*;

public class SocketHintDeclarationCollection {
    //Kept around to define the order of the params.
    private final List<DefinedParamType> paramTypes;
    private final Map<Type, List<DefinedParamType>> inputHintsMap = new HashMap<>();
    private final Map<Type, List<DefinedParamType>> outputHintsMap = new HashMap<>();
    //Defines the order of the input params
    private final List<DefinedParamType> inputParamTypes = new ArrayList<>();
    //Defines the order of the output params
    private final List<DefinedParamType> outputParamTypes = new ArrayList<>();


    /**
     *
     * @param paramTypes
     */
    public SocketHintDeclarationCollection(List<DefinedParamType> paramTypes){
        this.paramTypes = paramTypes;

        // Figure out which hint map to put this defined param type into
        for(DefinedParamType type : paramTypes){
            Map<Type, List<DefinedParamType>> assignmentMap;
            List<DefinedParamType> assignmentList;
            assignmentMap = type.isOutput() ? outputHintsMap : inputHintsMap;
            assignmentList = type.isOutput() ? outputParamTypes : inputParamTypes;
            assignmentMap.putIfAbsent(type.getType(), new ArrayList<>()); // Will return null if new
            assignmentMap.get(type.getType()).add(type);
            assignmentList.add(type);
        }
    }

    public List<Expression> getSocketAssignments(String inputParamId, String outputParamId){
        List<Expression> assignments = new ArrayList<>();
        int inputIndex = 0;
        int outputIndex = 0;
        for(DefinedParamType paramType: paramTypes){
            int index;
            String paramId;
            if (inputParamTypes.contains(paramType)){
                paramId = inputParamId;
                index = inputIndex;
                inputIndex ++;
            } else if (outputParamTypes.contains(paramType)){
                paramId = outputParamId;
                index = outputIndex;
                outputIndex ++;
            } else {
                assert false : "The paramType was not in either the input or output list";
                return null;
            }
            assignments.add(
                    new VariableDeclarationExpr(
                            ModifierSet.FINAL,
                            paramType.getType(),
                            Arrays.asList(
                                    new VariableDeclarator(
                                            new VariableDeclaratorId(paramType.getName()),
                                            new MethodCallExpr(
                                                    new MethodCallExpr(
                                                            new NameExpr(paramType.getName() + "Hint"),
                                                            "getType"
                                                    ),
                                                    "cast",
                                                    Arrays.asList(
                                                            new MethodCallExpr(
                                                                    new ArrayAccessExpr(
                                                                            new NameExpr(paramId),
                                                                            new IntegerLiteralExpr(String.valueOf(index))
                                                                    ),
                                                                    "getValue"
                                                            )
                                                    )
                                            )

                                    )
                            )
                    )
            );
        }
        return assignments;
    }

    public ObjectCreationExpr getSocketListParam(DefinedParamType definedParamType, ClassOrInterfaceType socketType){
        return new ObjectCreationExpr(null, socketType, Arrays.asList(
                new NameExpr("eventBus"),
                new NameExpr(definedParamType.getName() + "Hint")
        ));
    }

    private BlockStmt getInputOrOutputSocketBody(List<DefinedParamType> paramTypes){
        ClassOrInterfaceType socketType = new ClassOrInterfaceType("Socket");
        List<Expression> passedExpressions = new ArrayList<>();
        for (DefinedParamType inputParam : paramTypes){
            passedExpressions.add(getSocketListParam(inputParam, socketType));
        }
        BlockStmt returnStatement = new BlockStmt(
                Arrays.asList(new ReturnStmt(
                                new ArrayCreationExpr(socketType, 1, new ArrayInitializerExpr(passedExpressions)))
                )
        );
        return returnStatement;
    }

    public BlockStmt getInputSocketBody(){
        return getInputOrOutputSocketBody(inputParamTypes);
    }

    public BlockStmt getOutputSocketBody(){
        return getInputOrOutputSocketBody(outputParamTypes);
    }

    public List<SocketHintDeclaration> getAllSocketHints(){
        List<SocketHintDeclaration> socketHintDeclarations = new ArrayList<>();
        for(Type type : inputHintsMap.keySet()){
            socketHintDeclarations.add(new SocketHintDeclaration(type, inputHintsMap.get(type)));
        }
        for(Type type : outputHintsMap.keySet()){
            socketHintDeclarations.add(new SocketHintDeclaration(type, outputHintsMap.get(type)));
        }
        return socketHintDeclarations;
    }

    public static Type getSocketReturnParam(){
        ClassOrInterfaceType socketType = new ClassOrInterfaceType(null, "Socket");
        socketType.setTypeArgs(Arrays.asList(new WildcardType()));
        return new ReferenceType(socketType, 1);
    }

}
