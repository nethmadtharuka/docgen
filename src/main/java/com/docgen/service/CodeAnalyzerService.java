package com.docgen.service;

import com.docgen.model.*;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CodeAnalyzerService - Parses Java source code using JavaParser.
 *
 * ╔═══════════════════════════════════════════════════════════════════╗
 * ║                     HOW JAVAPARSER WORKS                          ║
 * ╠═══════════════════════════════════════════════════════════════════╣
 * ║                                                                   ║
 * ║  1. INPUT: Java source code as String                            ║
 * ║     "public class Dog { private String name; }"                   ║
 * ║                           │                                       ║
 * ║                           ▼                                       ║
 * ║  2. PARSE: Convert to AST (Abstract Syntax Tree)                  ║
 * ║                                                                   ║
 * ║     CompilationUnit                                               ║
 * ║           │                                                       ║
 * ║           └── ClassOrInterfaceDeclaration (Dog)                   ║
 * ║                     │                                             ║
 * ║                     └── FieldDeclaration (name: String)           ║
 * ║                           │                                       ║
 * ║                           ▼                                       ║
 * ║  3. EXTRACT: Walk the tree and collect information                ║
 * ║                                                                   ║
 * ║     ClassInfo { name="Dog", fields=[FieldInfo{name="name"...}] }  ║
 * ║                                                                   ║
 * ╚═══════════════════════════════════════════════════════════════════╝
 *
 * KEY JAVAPARSER CLASSES:
 *
 * - CompilationUnit     : The entire file (package, imports, classes)
 * - ClassOrInterfaceDeclaration : A class or interface
 * - MethodDeclaration   : A method
 * - FieldDeclaration    : A field/variable
 * - ConstructorDeclaration : A constructor
 * - Parameter           : Method/constructor parameter
 */
public class CodeAnalyzerService {

    // JavaParser instance - reusable for multiple files
    private final JavaParser javaParser;

    /**
     * Constructor - creates a new JavaParser instance
     */
    public CodeAnalyzerService() {
        this.javaParser = new JavaParser();
    }

    /**
     * Analyze a single Java file.
     *
     * This is the main entry point. It:
     * 1. Parses the file content using JavaParser
     * 2. Extracts all classes, methods, fields, etc.
     * 3. Populates the JavaFileInfo with parsed data
     *
     * @param fileInfo The file to analyze (must have content set)
     * @return The same fileInfo, now with parsed structure
     */
    public JavaFileInfo analyzeFile(JavaFileInfo fileInfo) {
        String content = fileInfo.getContent();

        if (content == null || content.isEmpty()) {
            fileInfo.setParseError("No content to parse");
            return fileInfo;
        }

        try {
            // ========== STEP 1: Parse the source code ==========
            // JavaParser.parse() returns a ParseResult containing the AST
            ParseResult<CompilationUnit> parseResult = javaParser.parse(content);

            // Check if parsing was successful
            if (!parseResult.isSuccessful()) {
                String errors = parseResult.getProblems().stream()
                        .map(p -> p.getMessage())
                        .collect(Collectors.joining("; "));
                fileInfo.setParseError("Parse failed: " + errors);
                return fileInfo;
            }

            // Get the CompilationUnit (root of the AST)
            CompilationUnit cu = parseResult.getResult().orElse(null);
            if (cu == null) {
                fileInfo.setParseError("No compilation unit produced");
                return fileInfo;
            }

            // ========== STEP 2: Extract imports ==========
            List<String> imports = extractImports(cu);
            fileInfo.setImports(imports);

            // ========== STEP 3: Extract all type declarations ==========
            // This includes classes, interfaces, enums, records
            List<ClassInfo> classes = new ArrayList<>();

            // findAll() walks the entire AST and finds matching nodes
            for (TypeDeclaration<?> typeDecl : cu.findAll(TypeDeclaration.class)) {
                // Skip nested classes - we'll handle them as part of parent
                if (isNestedClass(typeDecl)) {
                    continue;
                }

                ClassInfo classInfo = extractClassInfo(typeDecl, fileInfo.getPackageName());
                if (classInfo != null) {
                    classes.add(classInfo);
                }
            }

            fileInfo.setClasses(classes);
            fileInfo.setParsed(true);

            System.out.println("   ✓ Parsed: " + fileInfo.getFileName() +
                    " → " + classes.size() + " class(es), " +
                    fileInfo.getTotalMethodCount() + " method(s)");

        } catch (Exception e) {
            fileInfo.setParseError("Exception: " + e.getMessage());
            System.err.println("   ✗ Failed to parse " + fileInfo.getFileName() +
                    ": " + e.getMessage());
        }

        return fileInfo;
    }

    /**
     * Analyze multiple files at once
     *
     * @param fileInfos List of files to analyze
     * @return The same list, with all files analyzed
     */
    public List<JavaFileInfo> analyzeAllFiles(List<JavaFileInfo> fileInfos) {
        System.out.println("🔬 Analyzing " + fileInfos.size() + " Java files with JavaParser...");
        System.out.println();

        int successCount = 0;
        int errorCount = 0;

        for (JavaFileInfo fileInfo : fileInfos) {
            analyzeFile(fileInfo);

            if (fileInfo.isParsed()) {
                successCount++;
            } else {
                errorCount++;
            }
        }

        System.out.println();
        System.out.println("📊 Parse complete: " + successCount + " success, " +
                errorCount + " errors");

        return fileInfos;
    }

    // ==================== EXTRACTION METHODS ====================

    /**
     * Extract all import statements from a CompilationUnit
     */
    private List<String> extractImports(CompilationUnit cu) {
        List<String> imports = new ArrayList<>();

        for (ImportDeclaration importDecl : cu.getImports()) {
            // Get the full import path
            String importPath = importDecl.getNameAsString();

            // Add asterisk for wildcard imports (import java.util.*)
            if (importDecl.isAsterisk()) {
                importPath += ".*";
            }

            // Add "static" prefix for static imports
            if (importDecl.isStatic()) {
                importPath = "static " + importPath;
            }

            imports.add(importPath);
        }

        return imports;
    }

    /**
     * Extract ClassInfo from a type declaration (class, interface, enum, record)
     */
    private ClassInfo extractClassInfo(TypeDeclaration<?> typeDecl, String packageName) {
        ClassInfo classInfo = new ClassInfo();

        // Set the name
        classInfo.setName(typeDecl.getNameAsString());

        // Set fully qualified name
        if (packageName != null && !packageName.isEmpty()) {
            classInfo.setFullyQualifiedName(packageName + "." + typeDecl.getNameAsString());
        } else {
            classInfo.setFullyQualifiedName(typeDecl.getNameAsString());
        }

        // Determine the class type and extract type-specific info
        if (typeDecl instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration classDecl = (ClassOrInterfaceDeclaration) typeDecl;

            if (classDecl.isInterface()) {
                classInfo.setClassType(ClassInfo.ClassType.INTERFACE);
            } else {
                classInfo.setClassType(ClassInfo.ClassType.CLASS);
            }

            // Extract extends (parent class)
            classDecl.getExtendedTypes().forEach(ext ->
                    classInfo.setSuperClass(ext.asString())
            );

            // Extract implements (interfaces)
            classDecl.getImplementedTypes().forEach(impl ->
                    classInfo.addInterface(impl.asString())
            );

            // Extract type parameters (generics)
            classDecl.getTypeParameters().forEach(tp ->
                    classInfo.getTypeParameters().add(tp.asString())
            );

        } else if (typeDecl instanceof EnumDeclaration) {
            classInfo.setClassType(ClassInfo.ClassType.ENUM);

        } else if (typeDecl instanceof RecordDeclaration) {
            classInfo.setClassType(ClassInfo.ClassType.RECORD);

        } else if (typeDecl instanceof AnnotationDeclaration) {
            classInfo.setClassType(ClassInfo.ClassType.ANNOTATION);
        }

        // Extract modifiers (public, abstract, final, etc.)
        typeDecl.getModifiers().forEach(mod ->
                classInfo.addModifier(mod.getKeyword().asString())
        );

        // Extract annotations
        typeDecl.getAnnotations().forEach(ann ->
                classInfo.addAnnotation("@" + ann.getNameAsString())
        );

        // Extract Javadoc
        extractJavadoc(typeDecl).ifPresent(classInfo::setJavadoc);

        // Extract line numbers
        typeDecl.getBegin().ifPresent(pos -> classInfo.setStartLine(pos.line));
        typeDecl.getEnd().ifPresent(pos -> classInfo.setEndLine(pos.line));

        // Extract fields
        for (FieldDeclaration fieldDecl : typeDecl.getFields()) {
            List<FieldInfo> fields = extractFieldInfo(fieldDecl);
            fields.forEach(classInfo::addField);
        }

        // Extract constructors (for classes and enums)
        for (ConstructorDeclaration ctorDecl : typeDecl.getConstructors()) {
            MethodInfo ctorInfo = extractConstructorInfo(ctorDecl);
            classInfo.addMethod(ctorInfo);
        }

        // Extract methods
        for (MethodDeclaration methodDecl : typeDecl.getMethods()) {
            MethodInfo methodInfo = extractMethodInfo(methodDecl);
            classInfo.addMethod(methodInfo);
        }

        // Extract nested classes
        for (TypeDeclaration<?> nestedType : typeDecl.findAll(TypeDeclaration.class)) {
            if (nestedType != typeDecl && isDirectChild(typeDecl, nestedType)) {
                ClassInfo nestedClass = extractClassInfo(nestedType,
                        classInfo.getFullyQualifiedName());
                if (nestedClass != null) {
                    classInfo.addNestedClass(nestedClass);
                }
            }
        }

        return classInfo;
    }

    /**
     * Extract FieldInfo from a field declaration.
     *
     * Note: A single FieldDeclaration can declare multiple variables:
     *   private int x, y, z;  // 3 fields in one declaration
     */
    private List<FieldInfo> extractFieldInfo(FieldDeclaration fieldDecl) {
        List<FieldInfo> fields = new ArrayList<>();

        // Get the type (shared by all variables in this declaration)
        String type = fieldDecl.getElementType().asString();

        // Get modifiers (shared)
        List<String> modifiers = fieldDecl.getModifiers().stream()
                .map(m -> m.getKeyword().asString())
                .collect(Collectors.toList());

        // Get Javadoc (shared)
        String javadoc = extractJavadoc(fieldDecl).orElse(null);

        // Each VariableDeclarator is a separate field
        for (VariableDeclarator variable : fieldDecl.getVariables()) {
            FieldInfo fieldInfo = new FieldInfo();

            fieldInfo.setName(variable.getNameAsString());
            fieldInfo.setType(type);
            fieldInfo.setModifiers(new ArrayList<>(modifiers));
            fieldInfo.setJavadoc(javadoc);

            // Get initial value if present
            variable.getInitializer().ifPresent(init ->
                    fieldInfo.setInitialValue(init.toString())
            );

            // Get line number
            variable.getBegin().ifPresent(pos ->
                    fieldInfo.setLineNumber(pos.line)
            );

            fields.add(fieldInfo);
        }

        return fields;
    }

    /**
     * Extract MethodInfo from a method declaration
     */
    private MethodInfo extractMethodInfo(MethodDeclaration methodDecl) {
        MethodInfo methodInfo = new MethodInfo();

        // Name
        methodInfo.setName(methodDecl.getNameAsString());

        // Return type
        methodInfo.setReturnType(methodDecl.getTypeAsString());

        // Parameters
        methodDecl.getParameters().forEach(param -> {
            ParameterInfo paramInfo = new ParameterInfo();
            paramInfo.setName(param.getNameAsString());
            paramInfo.setType(param.getTypeAsString());
            paramInfo.setVarArgs(param.isVarArgs());
            paramInfo.setFinal(param.isFinal());
            methodInfo.addParameter(paramInfo);
        });

        // Modifiers
        methodDecl.getModifiers().forEach(mod ->
                methodInfo.addModifier(mod.getKeyword().asString())
        );

        // Thrown exceptions
        methodDecl.getThrownExceptions().forEach(ex ->
                methodInfo.addThrownException(ex.asString())
        );

        // Annotations
        methodDecl.getAnnotations().forEach(ann ->
                methodInfo.addAnnotation("@" + ann.getNameAsString())
        );

        // Javadoc
        extractJavadoc(methodDecl).ifPresent(methodInfo::setJavadoc);

        // Extract @param descriptions from Javadoc
        extractParamDescriptions(methodDecl, methodInfo);

        // Extract @return description
        extractReturnDescription(methodDecl, methodInfo);

        // Line numbers
        methodDecl.getBegin().ifPresent(pos -> methodInfo.setStartLine(pos.line));
        methodDecl.getEnd().ifPresent(pos -> methodInfo.setEndLine(pos.line));

        return methodInfo;
    }

    /**
     * Extract MethodInfo from a constructor declaration
     */
    private MethodInfo extractConstructorInfo(ConstructorDeclaration ctorDecl) {
        MethodInfo methodInfo = new MethodInfo();

        // Constructors use class name as method name
        methodInfo.setName(ctorDecl.getNameAsString());
        methodInfo.setConstructor(true);
        // No return type for constructors

        // Parameters
        ctorDecl.getParameters().forEach(param -> {
            ParameterInfo paramInfo = new ParameterInfo();
            paramInfo.setName(param.getNameAsString());
            paramInfo.setType(param.getTypeAsString());
            paramInfo.setVarArgs(param.isVarArgs());
            paramInfo.setFinal(param.isFinal());
            methodInfo.addParameter(paramInfo);
        });

        // Modifiers
        ctorDecl.getModifiers().forEach(mod ->
                methodInfo.addModifier(mod.getKeyword().asString())
        );

        // Thrown exceptions
        ctorDecl.getThrownExceptions().forEach(ex ->
                methodInfo.addThrownException(ex.asString())
        );

        // Annotations
        ctorDecl.getAnnotations().forEach(ann ->
                methodInfo.addAnnotation("@" + ann.getNameAsString())
        );

        // Javadoc
        extractJavadoc(ctorDecl).ifPresent(methodInfo::setJavadoc);

        // Line numbers
        ctorDecl.getBegin().ifPresent(pos -> methodInfo.setStartLine(pos.line));
        ctorDecl.getEnd().ifPresent(pos -> methodInfo.setEndLine(pos.line));

        return methodInfo;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Extract Javadoc description from a node
     */
    private Optional<String> extractJavadoc(Node node) {
        if (node instanceof NodeWithJavadoc<?>) {
            NodeWithJavadoc<?> nodeWithJavadoc = (NodeWithJavadoc<?>) node;
            Optional<Javadoc> javadocOpt = nodeWithJavadoc.getJavadoc();

            if (javadocOpt.isPresent()) {
                Javadoc javadoc = javadocOpt.get();
                // Get the main description (before any @tags)
                String description = javadoc.getDescription().toText().trim();
                if (!description.isEmpty()) {
                    return Optional.of(description);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Extract @param descriptions from Javadoc and add to parameters
     */
    private void extractParamDescriptions(MethodDeclaration methodDecl, MethodInfo methodInfo) {
        methodDecl.getJavadoc().ifPresent(javadoc -> {
            for (JavadocBlockTag tag : javadoc.getBlockTags()) {
                if (tag.getType() == JavadocBlockTag.Type.PARAM) {
                    String paramName = tag.getName().orElse("");
                    String description = tag.getContent().toText().trim();

                    // Find matching parameter and set description
                    for (ParameterInfo param : methodInfo.getParameters()) {
                        if (param.getName().equals(paramName)) {
                            param.setDescription(description);
                            break;
                        }
                    }
                }
            }
        });
    }

    /**
     * Extract @return description from Javadoc
     */
    private void extractReturnDescription(MethodDeclaration methodDecl, MethodInfo methodInfo) {
        methodDecl.getJavadoc().ifPresent(javadoc -> {
            for (JavadocBlockTag tag : javadoc.getBlockTags()) {
                if (tag.getType() == JavadocBlockTag.Type.RETURN) {
                    methodInfo.setReturnDescription(tag.getContent().toText().trim());
                    break;
                }
            }
        });
    }

    /**
     * Check if a type is a nested class (inside another class)
     */
    private boolean isNestedClass(TypeDeclaration<?> typeDecl) {
        return typeDecl.getParentNode()
                .filter(parent -> parent instanceof TypeDeclaration)
                .isPresent();
    }

    /**
     * Check if nestedType is a direct child of parentType
     */
    private boolean isDirectChild(TypeDeclaration<?> parentType, TypeDeclaration<?> nestedType) {
        return nestedType.getParentNode()
                .filter(parent -> parent == parentType)
                .isPresent();
    }

    // ==================== STATISTICS ====================

    /**
     * Generate a summary of analyzed files
     */
    public String generateAnalysisSummary(List<JavaFileInfo> fileInfos) {
        StringBuilder sb = new StringBuilder();

        int totalClasses = 0;
        int totalInterfaces = 0;
        int totalEnums = 0;
        int totalMethods = 0;
        int totalFields = 0;

        for (JavaFileInfo file : fileInfos) {
            if (!file.isParsed()) continue;

            for (ClassInfo cls : file.getClasses()) {
                switch (cls.getClassType()) {
                    case CLASS -> totalClasses++;
                    case INTERFACE -> totalInterfaces++;
                    case ENUM -> totalEnums++;
                    default -> {}
                }
                totalMethods += cls.getMethods().size();
                totalFields += cls.getFields().size();
            }
        }

        sb.append("=== Code Analysis Summary ===\n");
        sb.append("Files analyzed: ").append(fileInfos.size()).append("\n");
        sb.append("Classes: ").append(totalClasses).append("\n");
        sb.append("Interfaces: ").append(totalInterfaces).append("\n");
        sb.append("Enums: ").append(totalEnums).append("\n");
        sb.append("Total methods: ").append(totalMethods).append("\n");
        sb.append("Total fields: ").append(totalFields).append("\n");

        return sb.toString();
    }
}