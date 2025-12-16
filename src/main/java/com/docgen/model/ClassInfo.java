package com.docgen.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassInfo - Stores complete information about a Java class, interface, or enum.
 *
 * This is the main container for parsed class data:
 *
 *   @Entity
 *   public class User extends BaseEntity implements Serializable {
 *       private String name;
 *       public void save() { }
 *   }
 *
 * ClassInfo captures:
 * - Type (CLASS, INTERFACE, ENUM, RECORD, ANNOTATION)
 * - Name ("User")
 * - Modifiers (public)
 * - Parent class ("BaseEntity")
 * - Interfaces (["Serializable"])
 * - Annotations (["@Entity"])
 * - Fields ([name])
 * - Methods ([save])
 * - Javadoc
 */
public class ClassInfo {

    // ==================== ENUMS ====================

    /**
     * The type of class declaration
     */
    public enum ClassType {
        CLASS,       // Regular class
        INTERFACE,   // Interface
        ENUM,        // Enumeration
        RECORD,      // Record (Java 14+)
        ANNOTATION   // Annotation type (@interface)
    }


    // ==================== FIELDS ====================

    /**
     * The class name (simple name, not fully qualified)
     * Example: "UserService", "HttpClient", "ResponseType"
     */
    private String name;

    /**
     * The fully qualified name including package
     * Example: "com.example.service.UserService"
     */
    private String fullyQualifiedName;

    /**
     * The type of this class declaration
     */
    private ClassType classType;

    /**
     * Access and other modifiers
     * Examples: "public", "abstract", "final"
     */
    private List<String> modifiers;

    /**
     * The parent class (extends clause)
     * Example: "BaseEntity", "ArrayList<String>"
     * Null if no explicit parent (implicitly extends Object)
     */
    private String superClass;

    /**
     * Interfaces implemented (implements clause)
     * Example: ["Serializable", "Comparable<User>"]
     */
    private List<String> interfaces;

    /**
     * Annotations on this class
     * Example: ["@Entity", "@Table(name=\"users\")", "@Deprecated"]
     */
    private List<String> annotations;

    /**
     * All fields in this class
     */
    private List<FieldInfo> fields;

    /**
     * All methods in this class (including constructors)
     */
    private List<MethodInfo> methods;

    /**
     * Nested/inner classes
     */
    private List<ClassInfo> nestedClasses;

    /**
     * The Javadoc comment for this class
     */
    private String javadoc;

    /**
     * Line number where this class starts
     */
    private int startLine;

    /**
     * Line number where this class ends
     */
    private int endLine;

    /**
     * Generic type parameters
     * Example: For "class Box<T extends Number>" this would be ["T extends Number"]
     */
    private List<String> typeParameters;


    // ==================== CONSTRUCTOR ====================

    /**
     * Default constructor - initializes all lists
     */
    public ClassInfo() {
        this.classType = ClassType.CLASS;
        this.modifiers = new ArrayList<>();
        this.interfaces = new ArrayList<>();
        this.annotations = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.nestedClasses = new ArrayList<>();
        this.typeParameters = new ArrayList<>();
    }

    /**
     * Constructor with name and type
     *
     * @param name Class name
     * @param classType Type of class
     */
    public ClassInfo(String name, ClassType classType) {
        this();
        this.name = name;
        this.classType = classType;
    }


    // ==================== GETTERS & SETTERS ====================

    public String getName() {
        return name;
    }

    public ClassInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public ClassInfo setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
        return this;
    }

    public ClassType getClassType() {
        return classType;
    }

    public ClassInfo setClassType(ClassType classType) {
        this.classType = classType;
        return this;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public ClassInfo setModifiers(List<String> modifiers) {
        this.modifiers = modifiers;
        return this;
    }

    public ClassInfo addModifier(String modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    public String getSuperClass() {
        return superClass;
    }

    public ClassInfo setSuperClass(String superClass) {
        this.superClass = superClass;
        return this;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public ClassInfo setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
        return this;
    }

    public ClassInfo addInterface(String iface) {
        this.interfaces.add(iface);
        return this;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public ClassInfo setAnnotations(List<String> annotations) {
        this.annotations = annotations;
        return this;
    }

    public ClassInfo addAnnotation(String annotation) {
        this.annotations.add(annotation);
        return this;
    }

    public List<FieldInfo> getFields() {
        return fields;
    }

    public ClassInfo setFields(List<FieldInfo> fields) {
        this.fields = fields;
        return this;
    }

    public ClassInfo addField(FieldInfo field) {
        this.fields.add(field);
        return this;
    }

    public List<MethodInfo> getMethods() {
        return methods;
    }

    public ClassInfo setMethods(List<MethodInfo> methods) {
        this.methods = methods;
        return this;
    }

    public ClassInfo addMethod(MethodInfo method) {
        this.methods.add(method);
        return this;
    }

    public List<ClassInfo> getNestedClasses() {
        return nestedClasses;
    }

    public ClassInfo setNestedClasses(List<ClassInfo> nestedClasses) {
        this.nestedClasses = nestedClasses;
        return this;
    }

    public ClassInfo addNestedClass(ClassInfo nestedClass) {
        this.nestedClasses.add(nestedClass);
        return this;
    }

    public String getJavadoc() {
        return javadoc;
    }

    public ClassInfo setJavadoc(String javadoc) {
        this.javadoc = javadoc;
        return this;
    }

    public int getStartLine() {
        return startLine;
    }

    public ClassInfo setStartLine(int startLine) {
        this.startLine = startLine;
        return this;
    }

    public int getEndLine() {
        return endLine;
    }

    public ClassInfo setEndLine(int endLine) {
        this.endLine = endLine;
        return this;
    }

    public List<String> getTypeParameters() {
        return typeParameters;
    }

    public ClassInfo setTypeParameters(List<String> typeParameters) {
        this.typeParameters = typeParameters;
        return this;
    }


    // ==================== UTILITY METHODS ====================

    /**
     * Check if this is a regular class
     */
    public boolean isClass() {
        return classType == ClassType.CLASS;
    }

    /**
     * Check if this is an interface
     */
    public boolean isInterface() {
        return classType == ClassType.INTERFACE;
    }

    /**
     * Check if this is an enum
     */
    public boolean isEnum() {
        return classType == ClassType.ENUM;
    }

    /**
     * Check if this is a record
     */
    public boolean isRecord() {
        return classType == ClassType.RECORD;
    }

    /**
     * Check if class is public
     */
    public boolean isPublic() {
        return modifiers.contains("public");
    }

    /**
     * Check if class is abstract
     */
    public boolean isAbstract() {
        return modifiers.contains("abstract");
    }

    /**
     * Check if class is final
     */
    public boolean isFinal() {
        return modifiers.contains("final");
    }

    /**
     * Get all constructors
     */
    public List<MethodInfo> getConstructors() {
        return methods.stream()
                .filter(MethodInfo::isConstructor)
                .collect(Collectors.toList());
    }

    /**
     * Get all non-constructor methods
     */
    public List<MethodInfo> getNonConstructorMethods() {
        return methods.stream()
                .filter(m -> !m.isConstructor())
                .collect(Collectors.toList());
    }

    /**
     * Get only public methods
     */
    public List<MethodInfo> getPublicMethods() {
        return methods.stream()
                .filter(MethodInfo::isPublic)
                .collect(Collectors.toList());
    }

    /**
     * Get only public fields
     */
    public List<FieldInfo> getPublicFields() {
        return fields.stream()
                .filter(FieldInfo::isPublic)
                .collect(Collectors.toList());
    }

    /**
     * Get the total line count
     */
    public int getLineCount() {
        if (endLine > 0 && startLine > 0) {
            return endLine - startLine + 1;
        }
        return 0;
    }

    /**
     * Get the class declaration signature
     * Example: "public abstract class UserService extends BaseService implements Runnable"
     */
    public String getSignature() {
        StringBuilder sb = new StringBuilder();

        // Modifiers
        if (!modifiers.isEmpty()) {
            sb.append(String.join(" ", modifiers)).append(" ");
        }

        // Class type
        sb.append(classType.name().toLowerCase()).append(" ");

        // Name with generics
        sb.append(name);
        if (!typeParameters.isEmpty()) {
            sb.append("<").append(String.join(", ", typeParameters)).append(">");
        }

        // Extends
        if (superClass != null && !superClass.isEmpty()) {
            sb.append(" extends ").append(superClass);
        }

        // Implements
        if (!interfaces.isEmpty()) {
            sb.append(" implements ").append(String.join(", ", interfaces));
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return getSignature();
    }

    /**
     * Get a detailed summary of this class
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== ").append(name).append(" ===\n");
        sb.append("Type: ").append(classType).append("\n");
        sb.append("Signature: ").append(getSignature()).append("\n");

        if (!annotations.isEmpty()) {
            sb.append("Annotations: ").append(annotations).append("\n");
        }

        sb.append("Fields: ").append(fields.size()).append("\n");
        sb.append("Methods: ").append(methods.size()).append("\n");
        sb.append("Lines: ").append(getLineCount()).append("\n");

        if (javadoc != null && !javadoc.isEmpty()) {
            sb.append("Description: ").append(javadoc).append("\n");
        }

        return sb.toString();
    }
}