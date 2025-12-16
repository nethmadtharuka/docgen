package com.docgen.model;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JavaFileInfo - Holds all information about a single Java source file.
 *
 * DAY 2 UPDATE: Now includes parsed structure (classes, imports)
 *
 * Think of this as a "container" or "box" that stores:
 * - Where the file is located
 * - What's inside the file
 * - When it was last modified
 * - Basic statistics about the file
 *
 * This is a POJO (Plain Old Java Object) - just data, no complex logic.
 */
public class JavaFileInfo {

    // ==================== FIELDS ====================
    // These are the pieces of information we store for each file

    /**
     * The full path to the file
     * Example: /home/user/project/src/Main.java
     */
    private final Path filePath;

    /**
     * Just the file name without the directory path
     * Example: Main.java
     */
    private final String fileName;

    /**
     * The package this file belongs to
     * Example: com.docgen.service
     * Can be empty if no package declared
     */
    private String packageName;

    /**
     * The entire content of the file as a String
     * We read this from disk
     */
    private String content;

    /**
     * How many lines of code in this file
     */
    private int lineCount;

    /**
     * File size in bytes
     */
    private long fileSize;

    /**
     * When the file was last modified
     */
    private LocalDateTime lastModified;

    // ==================== DAY 2: PARSED STRUCTURE ====================
    // These fields store the result of parsing with JavaParser

    /**
     * Import statements in this file
     * Example: ["java.util.List", "java.io.IOException"]
     */
    private List<String> imports;

    /**
     * All classes defined in this file
     * Usually one main class, but can have inner classes
     */
    private List<ClassInfo> classes;

    /**
     * Whether this file has been successfully parsed
     */
    private boolean parsed;

    /**
     * Error message if parsing failed
     */
    private String parseError;


    // ==================== CONSTRUCTOR ====================
    // This runs when you create a new JavaFileInfo object

    /**
     * Constructor - Creates a new JavaFileInfo
     *
     * @param filePath The path to the Java file
     *
     * Note: We only require filePath. Other fields are set later
     *       using setter methods. This is a common pattern.
     */
    public JavaFileInfo(Path filePath) {
        // 'this' refers to the current object
        // We're saying: this object's filePath = the filePath parameter
        this.filePath = filePath;

        // Extract just the filename from the full path
        // Example: /home/user/Main.java → Main.java
        this.fileName = filePath.getFileName().toString();

        // Initialize Day 2 lists
        this.imports = new ArrayList<>();
        this.classes = new ArrayList<>();
        this.parsed = false;
    }


    // ==================== GETTERS ====================
    // Methods to READ the data (get values out)

    /**
     * Get the full file path
     */
    public Path getFilePath() {
        return filePath;
    }

    /**
     * Get just the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Get the package name
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Get the file content
     */
    public String getContent() {
        return content;
    }

    /**
     * Get the line count
     */
    public int getLineCount() {
        return lineCount;
    }

    /**
     * Get the file size in bytes
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Get last modified timestamp
     */
    public LocalDateTime getLastModified() {
        return lastModified;
    }


    // ==================== SETTERS ====================
    // Methods to WRITE data (put values in)
    // We return 'this' to allow method chaining (fluent interface)

    /**
     * Set the package name
     *
     * @param packageName The package (e.g., "com.docgen.model")
     * @return this object (for method chaining)
     *
     * Method chaining example:
     *   fileInfo.setPackageName("com.docgen").setContent("...");
     */
    public JavaFileInfo setPackageName(String packageName) {
        this.packageName = packageName;
        return this;  // Return 'this' enables chaining
    }

    /**
     * Set the file content
     */
    public JavaFileInfo setContent(String content) {
        this.content = content;
        // Automatically calculate line count when content is set
        this.lineCount = content.isEmpty() ? 0 : content.split("\n").length;
        return this;
    }

    /**
     * Set the file size
     */
    public JavaFileInfo setFileSize(long fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    /**
     * Set the last modified time
     */
    public JavaFileInfo setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }


    // ==================== DAY 2: GETTERS & SETTERS ====================

    /**
     * Get the import statements
     */
    public List<String> getImports() {
        return imports;
    }

    /**
     * Set the import statements
     */
    public JavaFileInfo setImports(List<String> imports) {
        this.imports = imports;
        return this;
    }

    /**
     * Add a single import
     */
    public JavaFileInfo addImport(String importStatement) {
        this.imports.add(importStatement);
        return this;
    }

    /**
     * Get all classes in this file
     */
    public List<ClassInfo> getClasses() {
        return classes;
    }

    /**
     * Set the classes
     */
    public JavaFileInfo setClasses(List<ClassInfo> classes) {
        this.classes = classes;
        return this;
    }

    /**
     * Add a class
     */
    public JavaFileInfo addClass(ClassInfo classInfo) {
        this.classes.add(classInfo);
        return this;
    }

    /**
     * Check if file was successfully parsed
     */
    public boolean isParsed() {
        return parsed;
    }

    /**
     * Set parsed status
     */
    public JavaFileInfo setParsed(boolean parsed) {
        this.parsed = parsed;
        return this;
    }

    /**
     * Get parse error message
     */
    public String getParseError() {
        return parseError;
    }

    /**
     * Set parse error message
     */
    public JavaFileInfo setParseError(String parseError) {
        this.parseError = parseError;
        return this;
    }

    /**
     * Get the main/primary class (usually the public class matching filename)
     */
    public ClassInfo getMainClass() {
        if (classes.isEmpty()) {
            return null;
        }
        // Try to find public class matching filename
        String expectedName = fileName.replace(".java", "");
        for (ClassInfo cls : classes) {
            if (cls.getName().equals(expectedName)) {
                return cls;
            }
        }
        // Return first class if no match
        return classes.get(0);
    }

    /**
     * Get total number of methods across all classes
     */
    public int getTotalMethodCount() {
        return classes.stream()
                .mapToInt(c -> c.getMethods().size())
                .sum();
    }

    /**
     * Get total number of fields across all classes
     */
    public int getTotalFieldCount() {
        return classes.stream()
                .mapToInt(c -> c.getFields().size())
                .sum();
    }


    // ==================== UTILITY METHODS ====================

    /**
     * Get a nice human-readable representation of this object
     * Useful for debugging and logging
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "JavaFileInfo{\n" +
                        "  fileName='%s'\n" +
                        "  package='%s'\n" +
                        "  lines=%d\n" +
                        "  size=%d bytes\n" +
                        "  path=%s\n",
                fileName,
                packageName != null ? packageName : "(default package)",
                lineCount,
                fileSize,
                filePath
        ));

        // Day 2: Add parsed structure info
        if (parsed) {
            sb.append(String.format(
                    "  parsed=true\n" +
                            "  imports=%d\n" +
                            "  classes=%d\n" +
                            "  totalMethods=%d\n" +
                            "  totalFields=%d\n",
                    imports.size(),
                    classes.size(),
                    getTotalMethodCount(),
                    getTotalFieldCount()
            ));
        } else if (parseError != null) {
            sb.append("  parsed=false\n");
            sb.append("  error=").append(parseError).append("\n");
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Get a short summary - useful for quick display
     */
    public String getSummary() {
        return String.format("%s (%d lines)", fileName, lineCount);
    }
}