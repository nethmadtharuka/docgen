package com.docgen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * MethodInfo - Stores complete information about a method.
 *
 * In Java, a method looks like:
 *
 *   /**
 *    * Calculates the sum of two numbers.
 *    * @param a First number
 *    * @param b Second number
 *    * @return The sum
 *    *\/
 *   public int add(int a, int b) throws ArithmeticException {
 *       return a + b;
 *   }
 *
 * This class captures ALL of that information:
 * - Method name ("add")
 * - Return type ("int")
 * - Parameters (a, b)
 * - Modifiers (public)
 * - Exceptions thrown (ArithmeticException)
 * - Javadoc (description, @param, @return, @throws)
 * - Whether it's a constructor
 */
public class MethodInfo {

    // ==================== FIELDS ====================

    /**
     * The method name
     * Example: "calculateTotal", "getUserById", "toString"
     */
    private String name;

    /**
     * The return type
     * Example: "void", "String", "List<User>", "CompletableFuture<Response>"
     * For constructors, this is null
     */
    private String returnType;

    /**
     * List of parameters
     * Each parameter has a name and type
     */
    private List<ParameterInfo> parameters;

    /**
     * Access and other modifiers
     * Examples: "public", "private", "static", "synchronized", "abstract"
     */
    private List<String> modifiers;

    /**
     * Exceptions declared in throws clause
     * Example: ["IOException", "SQLException"]
     */
    private List<String> thrownExceptions;

    /**
     * The main Javadoc description (before any tags)
     */
    private String javadoc;

    /**
     * The @return tag description from Javadoc
     */
    private String returnDescription;

    /**
     * Whether this is a constructor (not a regular method)
     * Constructors have no return type and same name as class
     */
    private boolean isConstructor;

    /**
     * Line number where this method starts
     */
    private int startLine;

    /**
     * Line number where this method ends
     */
    private int endLine;

    /**
     * Annotations on this method
     * Examples: "@Override", "@Deprecated", "@Test"
     */
    private List<String> annotations;


    // ==================== CONSTRUCTOR ====================

    /**
     * Default constructor - initializes empty lists
     */
    public MethodInfo() {
        this.parameters = new ArrayList<>();
        this.modifiers = new ArrayList<>();
        this.thrownExceptions = new ArrayList<>();
        this.annotations = new ArrayList<>();
    }

    /**
     * Constructor with method name
     *
     * @param name The method name
     */
    public MethodInfo(String name) {
        this();
        this.name = name;
    }


    // ==================== GETTERS & SETTERS ====================

    public String getName() {
        return name;
    }

    public MethodInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getReturnType() {
        return returnType;
    }

    public MethodInfo setReturnType(String returnType) {
        this.returnType = returnType;
        return this;
    }

    public List<ParameterInfo> getParameters() {
        return parameters;
    }

    public MethodInfo setParameters(List<ParameterInfo> parameters) {
        this.parameters = parameters;
        return this;
    }

    public MethodInfo addParameter(ParameterInfo param) {
        this.parameters.add(param);
        return this;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public MethodInfo setModifiers(List<String> modifiers) {
        this.modifiers = modifiers;
        return this;
    }

    public MethodInfo addModifier(String modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    public List<String> getThrownExceptions() {
        return thrownExceptions;
    }

    public MethodInfo setThrownExceptions(List<String> thrownExceptions) {
        this.thrownExceptions = thrownExceptions;
        return this;
    }

    public MethodInfo addThrownException(String exception) {
        this.thrownExceptions.add(exception);
        return this;
    }

    public String getJavadoc() {
        return javadoc;
    }

    public MethodInfo setJavadoc(String javadoc) {
        this.javadoc = javadoc;
        return this;
    }

    public String getReturnDescription() {
        return returnDescription;
    }

    public MethodInfo setReturnDescription(String returnDescription) {
        this.returnDescription = returnDescription;
        return this;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public MethodInfo setConstructor(boolean constructor) {
        isConstructor = constructor;
        return this;
    }

    public int getStartLine() {
        return startLine;
    }

    public MethodInfo setStartLine(int startLine) {
        this.startLine = startLine;
        return this;
    }

    public int getEndLine() {
        return endLine;
    }

    public MethodInfo setEndLine(int endLine) {
        this.endLine = endLine;
        return this;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public MethodInfo setAnnotations(List<String> annotations) {
        this.annotations = annotations;
        return this;
    }

    public MethodInfo addAnnotation(String annotation) {
        this.annotations.add(annotation);
        return this;
    }


    // ==================== UTILITY METHODS ====================

    /**
     * Check if method is public
     */
    public boolean isPublic() {
        return modifiers.contains("public");
    }

    /**
     * Check if method is private
     */
    public boolean isPrivate() {
        return modifiers.contains("private");
    }

    /**
     * Check if method is static
     */
    public boolean isStatic() {
        return modifiers.contains("static");
    }

    /**
     * Check if method is abstract
     */
    public boolean isAbstract() {
        return modifiers.contains("abstract");
    }

    /**
     * Get visibility level
     */
    public String getVisibility() {
        if (modifiers.contains("public")) return "public";
        if (modifiers.contains("protected")) return "protected";
        if (modifiers.contains("private")) return "private";
        return "package-private";
    }

    /**
     * Get the number of lines in this method
     */
    public int getLineCount() {
        if (endLine > 0 && startLine > 0) {
            return endLine - startLine + 1;
        }
        return 0;
    }

    /**
     * Get a formatted method signature
     * Example: "public String getUserById(Long id)"
     */
    public String getSignature() {
        StringBuilder sb = new StringBuilder();

        // Modifiers
        if (!modifiers.isEmpty()) {
            sb.append(String.join(" ", modifiers)).append(" ");
        }

        // Return type (skip for constructors)
        if (!isConstructor && returnType != null) {
            sb.append(returnType).append(" ");
        }

        // Method name
        sb.append(name);

        // Parameters
        sb.append("(");
        List<String> paramStrings = new ArrayList<>();
        for (ParameterInfo param : parameters) {
            paramStrings.add(param.getType() + " " + param.getName());
        }
        sb.append(String.join(", ", paramStrings));
        sb.append(")");

        // Throws clause
        if (!thrownExceptions.isEmpty()) {
            sb.append(" throws ").append(String.join(", ", thrownExceptions));
        }

        return sb.toString();
    }

    /**
     * Get a short signature (just name and parameter types)
     * Example: "getUserById(Long)"
     */
    public String getShortSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("(");

        List<String> types = new ArrayList<>();
        for (ParameterInfo param : parameters) {
            types.add(param.getType());
        }
        sb.append(String.join(", ", types));
        sb.append(")");

        return sb.toString();
    }

    @Override
    public String toString() {
        return getSignature();
    }

    /**
     * Get detailed string representation
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();

        // Annotations
        for (String annotation : annotations) {
            sb.append(annotation).append("\n");
        }

        // Signature
        sb.append(getSignature());

        // Javadoc summary
        if (javadoc != null && !javadoc.isEmpty()) {
            sb.append("\n  → ").append(javadoc);
        }

        return sb.toString();
    }
}