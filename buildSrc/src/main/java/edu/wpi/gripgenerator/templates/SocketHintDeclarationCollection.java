package edu.wpi.gripgenerator.templates;

import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.WildcardType;
import edu.wpi.gripgenerator.settings.DefinedParamType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SocketHintDeclarationCollection {
    private final Map<Type, List<DefinedParamType>> inputHintsMap;
    private final Map<Type, List<DefinedParamType>> outputHintsMap;

    public SocketHintDeclarationCollection(Map<Type, List<DefinedParamType>> inputHintsMap, Map<Type, List<DefinedParamType>> outputHintsMap){
        this.inputHintsMap = inputHintsMap;
        this.outputHintsMap = outputHintsMap;
    }

    public BlockStmt getInputSocketBody(){
        ClassOrInterfaceType socketType = new ClassOrInterfaceType("Socket");
        BlockStmt returnStatement = new BlockStmt(
                Arrays.asList(
                    new ReturnStmt(
                            new ArrayCreationExpr(socketType, 1, new ArrayInitializerExpr(
                                    Arrays.asList(
                                            new ObjectCreationExpr(null, socketType, Arrays.asList(new NullLiteralExpr(), new NullLiteralExpr()))
                                    )
                            ))
                    )
                )
        );
        return returnStatement;
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
