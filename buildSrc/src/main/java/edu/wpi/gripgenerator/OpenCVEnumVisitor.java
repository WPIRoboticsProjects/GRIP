package edu.wpi.gripgenerator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import edu.wpi.gripgenerator.defaults.DefaultValueCollector;
import edu.wpi.gripgenerator.defaults.EnumDefaultValue;
import edu.wpi.gripgenerator.templates.Enumeration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class OpenCVEnumVisitor extends VoidVisitorAdapter<Map<String, CompilationUnit>> {
    private static final String enumNamePostFix = "Enum";
    private PackageDeclaration collectedPackage;
    private final String baseClassName;
    private final Map<String, List<VariableDeclarator>> nameValuesMap = new HashMap();
    private final Map<String, String> nameParentClassMap = new HashMap();
    /**
     * @see <a href=http://fiddle.re/e0ek86>Regex Example</a>
     */
    private static final Pattern ENUM_REGEX = Pattern.compile(".*enum cv::([a-zA-Z_]*)?:?:?\\s?([a-zA-Z_]*)");
    private final DefaultValueCollector collector;

    public OpenCVEnumVisitor(String baseClassName, DefaultValueCollector collector) {
        this.baseClassName = baseClassName;
        this.collector = collector;
    }

    public Map<String, CompilationUnit> generateCompilationUnits() {
        assert collectedPackage != null : "The package was not collected before the compilation units were generated";
        // Add default values to the collector
        final List<Enumeration> enumerations = nameValuesMap
                .keySet()
                .stream()
                .map(k -> new Enumeration(
                        k,
                        collectedPackage,
                        baseClassName,
                        nameParentClassMap.get(k),
                        nameValuesMap.get(k)))
                .collect(Collectors.toList());
        collector.addAll(
                enumerations.stream()
                        .map(e -> new EnumDefaultValue(
                                e.getPackageDeclaration().getName().getName(),
                                e.getEnumerationClassName(),
                                nameValuesMap
                                        .get(e.getEnumerationClassName())
                                        .stream()
                                        .map(name -> name.getId().getName()).collect(Collectors.toSet())
                        ))
                        .collect(Collectors.toList())
        );
        // Generate the list of enumerations
        return enumerations.stream()
                .collect(Collectors.toMap(Enumeration::getEnumerationClassName, Enumeration::generateUnit));
    }

    @Override
    public void visit(final PackageDeclaration packageDeclaration, final Map<String, CompilationUnit> arg) {
        super.visit(packageDeclaration, arg);
        this.collectedPackage = packageDeclaration;
    }


    @Override
    public void visit(final FieldDeclaration declaration, final Map<String, CompilationUnit> arg) {
        super.visit(declaration, arg);

        Comment declarationComment = declaration.getComment();
        if (declarationComment != null) {
            Matcher matcher = ENUM_REGEX.matcher(declarationComment.toString());
            if (matcher.find()) {
                if (declaration.getParentNode() instanceof ClassOrInterfaceDeclaration) {
                    ClassOrInterfaceDeclaration clazz = (ClassOrInterfaceDeclaration) declaration.getParentNode();

                    /* Defines the name of the enumeration generated */
                    String name = matcher.group(1) + matcher.group(2) + enumNamePostFix;

                    nameValuesMap.putIfAbsent(name, new ArrayList());
                    nameValuesMap.get(name).addAll(declaration.getVariables());
                    if (!clazz.getName().equals(baseClassName)) {
                        nameParentClassMap.put(name, clazz.getName());
                    }
                } else {
                    throw new Error("Parent of Enum declaration was not a ClassOrInterfaceDeclaration");
                }
            }
        }
    }


}
