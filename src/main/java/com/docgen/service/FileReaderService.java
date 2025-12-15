package com.docgen.service;

import com.docgen.model.JavaFileInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FileReaderService - Reads Java source files and extracts basic information.
 *
 * This service:
 * 1. Reads file content from disk
 * 2. Extracts the package name using regex
 * 3. Gets file metadata (size, modified date)
 *
 * NOTE: This is a simple text-based reader. In Day 2, we'll add
 *       proper parsing with JavaParser to understand the code structure.
 */
public class FileReaderService {

    /**
     * Regex pattern to find package declaration
     *
     * Breakdown of: ^\\s*package\\s+([\\w.]+)\\s*;
     *
     *   ^           = Start of line
     *   \\s*        = Zero or more whitespace
     *   package     = Literal word "package"
     *   \\s+        = One or more whitespace
     *   ([\\w.]+)   = Capture group: word characters and dots (the package name)
     *   \\s*        = Zero or more whitespace
     *   ;           = Semicolon
     *
     * Example matches:
     *   "package com.docgen.service;"  → captures "com.docgen.service"
     *   "  package org.example  ;"     → captures "org.example"
     */
    private static final Pattern PACKAGE_PATTERN =
            Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);


    /**
     * Read a single Java file and populate the JavaFileInfo
     *
     * @param fileInfo The JavaFileInfo with filePath set
     * @return The same JavaFileInfo, now populated with content and metadata
     * @throws IOException if the file cannot be read
     */
    public JavaFileInfo readFile(JavaFileInfo fileInfo) throws IOException {
        Path path = fileInfo.getFilePath();

        // Read the entire file content as a String
        // StandardCharsets.UTF_8 ensures we handle special characters correctly
        String content = Files.readString(path, StandardCharsets.UTF_8);

        // Get file attributes (metadata)
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

        // Convert file modification time to LocalDateTime
        LocalDateTime lastModified = LocalDateTime.ofInstant(
                attrs.lastModifiedTime().toInstant(),
                ZoneId.systemDefault()
        );

        // Extract package name from the content
        String packageName = extractPackageName(content);

        // Populate the JavaFileInfo with all the data
        // This uses method chaining (each setter returns 'this')
        fileInfo
                .setContent(content)
                .setFileSize(attrs.size())
                .setLastModified(lastModified)
                .setPackageName(packageName);

        return fileInfo;
    }

    /**
     * Read multiple Java files at once
     *
     * @param fileInfos List of JavaFileInfo objects to populate
     * @return The same list, with all files populated
     */
    public List<JavaFileInfo> readAllFiles(List<JavaFileInfo> fileInfos) {
        System.out.println("📖 Reading " + fileInfos.size() + " Java files...");
        System.out.println();

        int successCount = 0;
        int errorCount = 0;

        for (JavaFileInfo fileInfo : fileInfos) {
            try {
                readFile(fileInfo);
                System.out.println("   ✓ Read: " + fileInfo.getFileName() +
                        " (" + fileInfo.getLineCount() + " lines)");
                successCount++;
            } catch (IOException e) {
                System.err.println("   ✗ Error reading: " + fileInfo.getFileName() +
                        " - " + e.getMessage());
                errorCount++;
            }
        }

        System.out.println();
        System.out.println("📊 Read complete: " + successCount + " success, " +
                errorCount + " errors");

        return fileInfos;
    }

    /**
     * Extract the package name from Java source code
     *
     * @param content The Java file content
     * @return The package name, or empty string if no package declared
     */
    private String extractPackageName(String content) {
        Matcher matcher = PACKAGE_PATTERN.matcher(content);

        if (matcher.find()) {
            // group(1) returns the first captured group (the package name)
            return matcher.group(1);
        }

        // No package declaration found = default package
        return "";
    }

    /**
     * Get a preview of the file content (first N lines)
     * Useful for debugging and quick inspection
     *
     * @param fileInfo The file to preview
     * @param maxLines Maximum number of lines to show
     * @return A string containing the first N lines
     */
    public String getContentPreview(JavaFileInfo fileInfo, int maxLines) {
        String content = fileInfo.getContent();

        if (content == null || content.isEmpty()) {
            return "(no content)";
        }

        String[] lines = content.split("\n");
        int linesToShow = Math.min(lines.length, maxLines);

        StringBuilder preview = new StringBuilder();
        preview.append("--- Preview of ").append(fileInfo.getFileName())
                .append(" (showing ").append(linesToShow)
                .append(" of ").append(lines.length).append(" lines) ---\n");

        for (int i = 0; i < linesToShow; i++) {
            // Line numbers start at 1, not 0
            preview.append(String.format("%4d | %s\n", i + 1, lines[i]));
        }

        if (lines.length > maxLines) {
            preview.append("     | ... (").append(lines.length - maxLines)
                    .append(" more lines)\n");
        }

        preview.append("--- End of preview ---");

        return preview.toString();
    }

    /**
     * Generate statistics about the files read
     */
    public String generateReadStats(List<JavaFileInfo> fileInfos) {
        int totalLines = 0;
        long totalSize = 0;
        int fileCount = fileInfos.size();

        for (JavaFileInfo file : fileInfos) {
            totalLines += file.getLineCount();
            totalSize += file.getFileSize();
        }

        // Calculate averages
        double avgLines = fileCount > 0 ? (double) totalLines / fileCount : 0;
        double avgSize = fileCount > 0 ? (double) totalSize / fileCount : 0;

        return String.format(
                "=== File Reading Statistics ===\n" +
                        "Total files: %d\n" +
                        "Total lines of code: %d\n" +
                        "Total size: %s\n" +
                        "Average lines per file: %.1f\n" +
                        "Average file size: %s\n",
                fileCount,
                totalLines,
                formatFileSize(totalSize),
                avgLines,
                formatFileSize((long) avgSize)
        );
    }

    /**
     * Format file size in human-readable format
     * Example: 1536 bytes → "1.5 KB"
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
}