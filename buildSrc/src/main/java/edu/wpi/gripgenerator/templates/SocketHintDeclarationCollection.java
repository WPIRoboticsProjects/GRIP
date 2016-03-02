package edu.wpi.gripgenerator.templates;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.comments.BlockComment;
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

    private final void addToLists(DefinedParamType type, Map<Type, List<DefinedParamType>> assignmentMap, List<DefinedParamType> assignmentList) {
        assignmentMap.putIfAbsent(type.getType(), new ArrayList<>()); // Will return null if new
        assignmentMap.get(type.getType()).add(type);
        if (!assignmentList.contains(type)) {
            assignmentList.add(type);
        }
    }

    private final void addToOutput(DefinedParamType param) {
        addToLists(param, outputHintsMap, outputParamTypes);
    }

    private final void addToInput(DefinedParamType param) {
        addToLists(param, inputHintsMap, inputParamTypes);
    }

    /**
     * @param paramTypes
     */
    public SocketHintDeclarationCollection(DefaultValueCollector collector, List<DefinedParamType> paramTypes) {
        this.paramTypes = paramTypes;
        this.collector = collector;

        // Figure out which hint map to put this defined param type into
        for (DefinedParamType type : paramTypes) {
            if (type.getDirection().isInput()) {
                addToInput(type);
            }
            if (type.getDirection().isOutput()) {
                addToOutput(type);
            }
        }
    }

    private ArrayAccessExpr arrayAccessExpr(String paramId, int index) {
        return new ArrayAccessExpr(
                new NameExpr(paramId),
                new IntegerLiteralExpr(String.valueOf(index))
        );
    }

    private MethodCallExpr getOrSetValueExpression(Expression scope, Expression setExpression) {
        return new MethodCallExpr(
                scope,
                (setExpression == null ? "getValue" : "setValue"),
                (setExpression == null ? Collections.emptyList() : Collections.singletonList(setExpression))
        );
    }

    private MethodCallExpr getValueExpression(String paramId, int index) {
        return new MethodCallExpr(getOrSetValueExpression(arrayAccessExpr(paramId, index), null), "get");
    }

    private Expression generateCopyExpression(DefinedParamType type, String inputParmId, int inputIndex, String outputParamId, int outputIndex) {
        // GOAL: ((InputSocket<Mat>) inputs[0]).getValue().get().assignTo(((OutputSocket<Mat>) outputs[0]).getValue().get());
        final ClassOrInterfaceType outputType = new ClassOrInterfaceType("OutputSocket");
        final ClassOrInterfaceType inputType = new ClassOrInterfaceType("InputSocket");
        outputType.setTypeArgs(Collections.singletonList(type.getType()));
        inputType.setTypeArgs(Collections.singletonList(type.getType()));

        final MethodCallExpr copyExpression = new MethodCallExpr(
                new MethodCallExpr(
                        getOrSetValueExpression(
                                new EnclosedExpr(
                                        new CastExpr(
                                                inputType,
                                                arrayAccessExpr(inputParmId, inputIndex)
                                        )
                                ),
                                null
                        ),
                        "get"
                ),
                "assignTo",
                Collections.singletonList(
                        new MethodCallExpr(
                                getOrSetValueExpression(
                                        new EnclosedExpr(
                                                new CastExpr(
                                                        outputType,
                                                        arrayAccessExpr(outputParamId, outputIndex)
                                                )
                                        ),
                                        null
                                ),
                                "get"
                        )
                )
        );
        copyExpression.setComment(new BlockComment(
                " Sets the value of the input Mat to the output because this operation does not have a destination Mat. "
        ));
        return copyExpression;
    }

    public List<Expression> getSocketAssignments(String inputParamId, String outputParamId) {
        List<Expression> assignments = new ArrayList<>();
        int inputIndex = 0;
        int outputIndex = 0;
        for (DefinedParamType paramType : paramTypes) {
            // We still need to increment the values if the param is ignored
            if (paramType.isIgnored()) continue;

            int index;
            String paramId;
            if (inputParamTypes.contains(paramType) && outputParamTypes.contains(paramType)) {
                if (paramType.getType().toStringWithoutComments().contains("Mat")) {
                    assignments.add(
                            generateCopyExpression(paramType, inputParamId, inputParamTypes.indexOf(paramType), outputParamId, outputParamTypes.indexOf(paramType))
                    );
                } else {
                    throw new IllegalStateException("Can not generate Input/Output Socket for type: " + paramType.getType().toString());
                }

                // We have used the input socket
                inputIndex++;
            }

            // Generate the output socket event if this is an input/output socket
            if (outputParamTypes.contains(paramType)) {
                paramId = outputParamId;
                index = outputIndex;
                outputIndex++;
            } else if (inputParamTypes.contains(paramType)) {
                paramId = inputParamId;
                index = inputIndex;
                inputIndex++;
            } else {
                assert false : "The paramType was not in either the input or output list";
                return null;
            }

            final MethodCallExpr getValueExpression = getValueExpression(paramId, index);
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

    public ObjectCreationExpr getSocketListParam(DefinedParamType definedParamType, ClassOrInterfaceType socketType, String inputOrOutputPostfix) {
        //System.out.println("Generating for default " + (definedParamType.getDefaultValue().isPresent() ? definedParamType.getDefaultValue().get().getName().toString() : "null"));
        return new ObjectCreationExpr(null, socketType, Arrays.asList(
                new NameExpr("eventBus"),
                new NameExpr(definedParamType.getName() + inputOrOutputPostfix + SocketHintDeclaration.HINT_POSTFIX)
        ));
    }

    private BlockStmt getInputOrOutputSocketBody(List<DefinedParamType> paramTypes, ClassOrInterfaceType socketType, String inputOrOutputPostfix) {
        List<Expression> passedExpressions = new ArrayList<>();
        for (DefinedParamType inputParam : paramTypes) {
            if (inputParam.isIgnored()) continue;
            passedExpressions.add(getSocketListParam(inputParam, socketType, inputOrOutputPostfix));
        }
        BlockStmt returnStatement = new BlockStmt(
                Arrays.asList(new ReturnStmt(
                        new ArrayCreationExpr(socketType, 1, new ArrayInitializerExpr(passedExpressions)))
                )
        );
        return returnStatement;
    }

    public BlockStmt getInputSocketBody() {
        return getInputOrOutputSocketBody(inputParamTypes, new ClassOrInterfaceType("InputSocket"), SocketHintDeclaration.INPUT_POSTFIX);
    }

    public BlockStmt getOutputSocketBody() {
        return getInputOrOutputSocketBody(outputParamTypes, new ClassOrInterfaceType("OutputSocket"), SocketHintDeclaration.OUTPUT_POSTFIX);
    }

    public List<SocketHintDeclaration> getAllSocketHints() {
        List<SocketHintDeclaration> socketHintDeclarations = new ArrayList<>();
        for (Map.Entry<Type, List<DefinedParamType>> type : inputHintsMap.entrySet()) {
            socketHintDeclarations.add(new SocketHintDeclaration(collector, type.getKey(), type.getValue(), DefinedParamType.DefinedParamDirection.INPUT));
        }
        for (Map.Entry<Type, List<DefinedParamType>> type : outputHintsMap.entrySet()) {
            socketHintDeclarations.add(new SocketHintDeclaration(collector, type.getKey(), type.getValue(), DefinedParamType.DefinedParamDirection.OUTPUT));
        }
        return socketHintDeclarations;
    }

    public static Type getSocketReturnParam(String socketNameType) {
        ClassOrInterfaceType socketType = new ClassOrInterfaceType(null, socketNameType);
        socketType.setTypeArgs(Arrays.asList(new WildcardType()));
        return new ReferenceType(socketType, 1);
    }

}
