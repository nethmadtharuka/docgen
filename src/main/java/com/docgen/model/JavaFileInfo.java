package com.docgen.model;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * JavaFileInfo - Holds all information about a single Java source file.
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


    // ==================== UTILITY METHODS ====================

    /**
     * Get a nice human-readable representation of this object
     * Useful for debugging and logging
     */
    @Override
    public String toString() {
        return String.format(
                "JavaFileInfo{\n" +
                        "  fileName='%s'\n" +
                        "  package='%s'\n" +
                        "  lines=%d\n" +
                        "  size=%d bytes\n" +
                        "  path=%s\n" +
                        "}",
                fileName,
                packageName != null ? packageName : "(default package)",
                lineCount,
                fileSize,
                filePath
        );
    }

    /**
     * Get a short summary - useful for quick display
     */
    public String getSummary() {
        return String.format("%s (%d lines)", fileName, lineCount);
    }
}