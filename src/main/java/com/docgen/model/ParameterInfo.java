package com.docgen.model;

/**
 * ParameterInfo - Stores information about a method parameter.
 *
 * In Java, method parameters look like:
 *   public void greet(String name, int times)
 *                     ^^^^^^^^^^^  ^^^^^^^^^
 *                     Parameter 1  Parameter 2
 *
 * This class captures:
 * - Parameter name ("name", "times")
 * - Parameter type ("String", "int")
 * - Whether it's varargs (String... args)
 * - Whether it's final (final String name)
 *
 * EXAMPLE:
 *   For: public void process(final List<String> items, int... counts)
 *
 *   Parameter 1:
 *     name = "items"
 *     type = "List<String>"
 *     isFinal = true
 *     isVarArgs = false
 *
 *   Parameter 2:
 *     name = "counts"
 *     type = "int"
 *     isFinal = false
 *     isVarArgs = true
 */
public class ParameterInfo {

    // ==================== FIELDS ====================

    /**
     * The parameter name
     * Example: "userId", "callback", "args"
     */
    private String name;

    /**
     * The parameter type
     * Example: "String", "int", "List<User>", "Consumer<String>"
     */
    private String type;

    /**
     * Whether this parameter is declared final
     * Example: final String name
     */
    private boolean isFinal;

    /**
     * Whether this is a varargs parameter (...)
     * Example: String... args
     * Note: Only the last parameter can be varargs
     */
    private boolean isVarArgs;

    /**
     * Description from Javadoc @param tag (if present)
     * Example: "The user's unique identifier"
     */
    private String description;


    // ==================== CONSTRUCTORS ====================

    /**
     * Default constructor
     */
    public ParameterInfo() {
    }

    /**
     * Constructor with name and type
     *
     * @param name Parameter name
     * @param type Parameter type
     */
    public ParameterInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }


    // ==================== GETTERS & SETTERS ====================

    public String getName() {
        return name;
    }

    public ParameterInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public ParameterInfo setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public ParameterInfo setFinal(boolean isFinal) {
        this.isFinal = isFinal;
        return this;
    }

    public boolean isVarArgs() {
        return isVarArgs;
    }

    public ParameterInfo setVarArgs(boolean varArgs) {
        isVarArgs = varArgs;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ParameterInfo setDescription(String description) {
        this.description = description;
        return this;
    }


    // ==================== UTILITY METHODS ====================

    /**
     * Get the full type including varargs notation
     * Example: "String..." for varargs, "String" otherwise
     */
    public String getFullType() {
        if (isVarArgs) {
            return type + "...";
        }
        return type;
    }

    /**
     * Get a formatted signature
     * Example: "final String name" or "int... values"
     */
    public String getSignature() {
        StringBuilder sb = new StringBuilder();

        if (isFinal) {
            sb.append("final ");
        }

        sb.append(getFullType()).append(" ").append(name);

        return sb.toString();
    }

    @Override
    public String toString() {
        return getSignature();
    }
}