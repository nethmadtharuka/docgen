package com.docgen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * FieldInfo - Stores information about a class field (member variable).
 *
 * In Java, a field looks like:
 *   private String name;
 *   public static final int MAX_SIZE = 100;
 *
 * This class captures:
 * - The field name ("name", "MAX_SIZE")
 * - The data type ("String", "int")
 * - Modifiers (private, public, static, final)
 * - Initial value if any ("100")
 * - Javadoc comment if present
 *
 * EXAMPLE:
 *   For: private String name = "John";
 *
 *   FieldInfo will contain:
 *     name = "name"
 *     type = "String"
 *     modifiers = ["private"]
 *     initialValue = "\"John\""
 */
public class FieldInfo {

    // ==================== FIELDS ====================

    /**
     * The name of the field
     * Example: "userName", "MAX_COUNT", "isActive"
     */
    private String name;

    /**
     * The data type of the field
     * Example: "String", "int", "List<User>", "Map<String, Integer>"
     */
    private String type;

    /**
     * Access modifiers and other modifiers
     * Examples: "private", "public", "static", "final", "volatile"
     */
    private List<String> modifiers;

    /**
     * The initial value assigned to the field (if any)
     * Example: For 'int count = 5;' this would be "5"
     * Can be null if no initial value
     */
    private String initialValue;

    /**
     * The Javadoc comment for this field (if present)
     * Example: "The user's display name"
     */
    private String javadoc;

    /**
     * Line number where this field is declared
     * Useful for navigation and error reporting
     */
    private int lineNumber;


    // ==================== CONSTRUCTOR ====================

    /**
     * Default constructor
     */
    public FieldInfo() {
        this.modifiers = new ArrayList<>();
    }

    /**
     * Constructor with name and type (most common fields)
     *
     * @param name Field name
     * @param type Field type
     */
    public FieldInfo(String name, String type) {
        this();
        this.name = name;
        this.type = type;
    }


    // ==================== GETTERS & SETTERS ====================

    public String getName() {
        return name;
    }

    public FieldInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public FieldInfo setType(String type) {
        this.type = type;
        return this;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public FieldInfo setModifiers(List<String> modifiers) {
        this.modifiers = modifiers;
        return this;
    }

    public FieldInfo addModifier(String modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    public String getInitialValue() {
        return initialValue;
    }

    public FieldInfo setInitialValue(String initialValue) {
        this.initialValue = initialValue;
        return this;
    }

    public String getJavadoc() {
        return javadoc;
    }

    public FieldInfo setJavadoc(String javadoc) {
        this.javadoc = javadoc;
        return this;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public FieldInfo setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
        return this;
    }


    // ==================== UTILITY METHODS ====================

    /**
     * Check if this field is private
     */
    public boolean isPrivate() {
        return modifiers.contains("private");
    }

    /**
     * Check if this field is public
     */
    public boolean isPublic() {
        return modifiers.contains("public");
    }

    /**
     * Check if this field is static
     */
    public boolean isStatic() {
        return modifiers.contains("static");
    }

    /**
     * Check if this field is final (constant)
     */
    public boolean isFinal() {
        return modifiers.contains("final");
    }

    /**
     * Get the visibility level as a string
     */
    public String getVisibility() {
        if (modifiers.contains("public")) return "public";
        if (modifiers.contains("protected")) return "protected";
        if (modifiers.contains("private")) return "private";
        return "package-private";  // Default in Java
    }

    /**
     * Get a formatted signature like: "private String name"
     */
    public String getSignature() {
        StringBuilder sb = new StringBuilder();

        // Add modifiers
        if (!modifiers.isEmpty()) {
            sb.append(String.join(" ", modifiers)).append(" ");
        }

        // Add type and name
        sb.append(type).append(" ").append(name);

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getSignature());

        if (initialValue != null) {
            sb.append(" = ").append(initialValue);
        }

        return sb.toString();
    }
}