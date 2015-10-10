package edu.wpi.gripgenerator.templates;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.*;
import edu.wpi.gripgenerator.defaults.DefaultValueCollector;
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
    private final DefaultValueCollector collector;


    /**
     * @param paramTypes
     */
    public SocketHintDeclarationCollection(DefaultValueCollector collector, List<DefinedParamType> paramTypes) {
        this.paramTypes = paramTypes;
        this.collector = collector;

        // Figure out which hint map to put this defined param type into
        for (DefinedParamType type : paramTypes) {
            Map<Type, List<DefinedParamType>> assignmentMap;
            List<DefinedParamType> assignmentList;
            assignmentMap = type.isOutput() ? outputHintsMap : inputHintsMap;
            assignmentList = type.isOutput() ? outputParamTypes : inputParamTypes;
            assignmentMap.putIfAbsent(type.getType(), new ArrayList<>()); // Will return null if new
            assignmentMap.get(type.getType()).add(type);
            assignmentList.add(type);
        }
    }

    public List<Expression> getSocketAssignments(String inputParamId, String outputParamId) {
        List<Expression> assignments = new ArrayList<>();
        int inputIndex = 0;
        int outputIndex = 0;
        for (DefinedParamType paramType : paramTypes) {
            int index;
            String paramId;
            if (inputParamTypes.contains(paramType)) {
                paramId = inputParamId;
                index = inputIndex;
                inputIndex++;
            } else if (outputParamTypes.contains(paramType)) {
                paramId = outputParamId;
                index = outputIndex;
                outputIndex++;
            } else {
                assert false : "The paramType was not in either the input or output list";
                return null;
            }
            final MethodCallExpr getValueExpression = new MethodCallExpr(
                    new ArrayAccessExpr(
                            new NameExpr(paramId),
                            new IntegerLiteralExpr(String.valueOf(index))
                    ),
                    "getValue"
            );
            final Expression assignExpression;
            if (paramType.getType() instanceof PrimitiveType && (!((PrimitiveType) paramType.getType()).getType().equals(PrimitiveType.Primitive.Boolean))) {
                final String numberConversionFunction;
                switch (((PrimitiveType) paramType.getType()).getType()) {
                    case Int:
                        numberConversionFunction = "intValue";
                        break;
                    case Short:
                    case Char:
                        numberConversionFunction = "shortValue";
                        break;
                    case Float:
                        numberConversionFunction = "floatValue";
                        break;
                    case Double:
                        numberConversionFunction = "doubleValue";
                        break;
                    case Byte:
                        numberConversionFunction = "byteValue";
                        break;
                    case Long:
                        numberConversionFunction = "longValue";
                        break;
                    default:
                        throw new IllegalStateException("Conversion for type " + paramType.getType() + " is not defined");
                }
                assignExpression = new MethodCallExpr(
                        new EnclosedExpr(
                                new CastExpr(
                                        ASTHelper.createReferenceType("Number", 0),
                                        getValueExpression
                                )
                        ),
                        numberConversionFunction
                );
            } else {
                assignExpression = new CastExpr(
                        paramType.getTypeBoxedIfPossible(),
                        getValueExpression
                );
            }

            assignments.add(
                    new VariableDeclarationExpr(
                            ModifierSet.FINAL,
                            paramType.getType(),
                            Collections.singletonList(
                                    new VariableDeclarator(
                                            new VariableDeclaratorId(paramType.getName()), assignExpression)
                            )
                    )
            );
        }
        return assignments;
    }

    public ObjectCreationExpr getSocketListParam(DefinedParamType definedParamType, ClassOrInterfaceType socketType) {
        //System.out.println("Generating for default " + (definedParamType.getDefaultValue().isPresent() ? definedParamType.getDefaultValue().get().getName().toString() : "null"));
        return new ObjectCreationExpr(null, socketType, Arrays.asList(
                new NameExpr("eventBus"),
                new NameExpr(definedParamType.getName() + SocketHintDeclaration.HINT_POSTFIX)
        ));
    }

    private BlockStmt getInputOrOutputSocketBody(List<DefinedParamType> paramTypes, ClassOrInterfaceType socketType) {
        List<Expression> passedExpressions = new ArrayList<>();
        for (DefinedParamType inputParam : paramTypes) {
            passedExpressions.add(getSocketListParam(inputParam, socketType));
        }
        BlockStmt returnStatement = new BlockStmt(
                Arrays.asList(new ReturnStmt(
                                new ArrayCreationExpr(socketType, 1, new ArrayInitializerExpr(passedExpressions)))
                )
        );
        return returnStatement;
    }

    public BlockStmt getInputSocketBody() {
        return getInputOrOutputSocketBody(inputParamTypes, new ClassOrInterfaceType("InputSocket"));
    }

    public BlockStmt getOutputSocketBody() {
        return getInputOrOutputSocketBody(outputParamTypes, new ClassOrInterfaceType("OutputSocket"));
    }

    public List<SocketHintDeclaration> getAllSocketHints() {
        List<SocketHintDeclaration> socketHintDeclarations = new ArrayList<>();
        for (Type type : inputHintsMap.keySet()) {
            socketHintDeclarations.add(new SocketHintDeclaration(collector, type, inputHintsMap.get(type)));
        }
        for (Type type : outputHintsMap.keySet()) {
            socketHintDeclarations.add(new SocketHintDeclaration(collector, type, outputHintsMap.get(type)));
        }
        return socketHintDeclarations;
    }

    public static Type getSocketReturnParam(String socketNameType) {
        ClassOrInterfaceType socketType = new ClassOrInterfaceType(null, socketNameType);
        socketType.setTypeArgs(Arrays.asList(new WildcardType()));
        return new ReferenceType(socketType, 1);
    }

}
