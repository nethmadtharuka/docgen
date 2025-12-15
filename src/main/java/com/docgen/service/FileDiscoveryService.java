package com.docgen.service;

import com.docgen.config.DocGeneratorConfig;
import com.docgen.model.JavaFileInfo;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * FileDiscoveryService - Finds all Java source files in a project.
 *
 * This service walks through the directory tree and:
 * 1. Finds all files ending with .java
 * 2. Filters out excluded directories (like 'target', 'build')
 * 3. Returns a list of JavaFileInfo objects
 *
 * EXAMPLE USAGE:
 *
 *   FileDiscoveryService discovery = new FileDiscoveryService(config);
 *   List<JavaFileInfo> files = discovery.discoverJavaFiles();
 */
public class FileDiscoveryService {

    // The configuration tells us where to look and what to skip
    private final DocGeneratorConfig config;

    /**
     * Constructor - creates service with given configuration
     *
     * @param config The configuration containing project path and exclusions
     */
    public FileDiscoveryService(DocGeneratorConfig config) {
        this.config = config;
    }

    /**
     * Discover all Java files in the configured project path.
     *
     * This is the main method you'll call.
     *
     * @return List of JavaFileInfo for each .java file found
     * @throws IOException if there's a problem reading the file system
     */
    public List<JavaFileInfo> discoverJavaFiles() throws IOException {
        Path projectPath = config.getProjectPath();

        // Verify the project path exists
        if (!Files.exists(projectPath)) {
            throw new IOException(
                    "Project path does not exist: " + projectPath
            );
        }

        // Verify it's a directory (not a file)
        if (!Files.isDirectory(projectPath)) {
            throw new IOException(
                    "Project path is not a directory: " + projectPath
            );
        }

        System.out.println("🔍 Discovering Java files in: " + projectPath);
        System.out.println("   Exclude patterns: " + config.getExcludePatterns());
        System.out.println();

        // Use the appropriate discovery method based on config
        if (config.isRecursive()) {
            return discoverRecursively(projectPath);
        } else {
            return discoverTopLevelOnly(projectPath);
        }
    }

    /**
     * Discover Java files in all subdirectories (recursive)
     *
     * Uses Files.walk() which creates a Stream of all paths
     * in the directory tree.
     */
    private List<JavaFileInfo> discoverRecursively(Path rootPath) throws IOException {
        List<JavaFileInfo> javaFiles = new ArrayList<>();

        // Files.walk() traverses the entire directory tree
        // maxDepth limits how deep we go
        try (Stream<Path> pathStream = Files.walk(rootPath, config.getMaxDepth())) {

            pathStream
                    // Filter 1: Only include regular files (not directories)
                    .filter(Files::isRegularFile)

                    // Filter 2: Only include .java files
                    .filter(path -> path.toString().endsWith(".java"))

                    // Filter 3: Exclude paths matching our exclude patterns
                    .filter(path -> !config.shouldExclude(path))

                    // For each matching path, create a JavaFileInfo
                    .forEach(path -> {
                        JavaFileInfo fileInfo = new JavaFileInfo(path);
                        javaFiles.add(fileInfo);
                        System.out.println("   ✓ Found: " + path.getFileName());
                    });
        }

        System.out.println();
        System.out.println("📁 Total Java files found: " + javaFiles.size());

        return javaFiles;
    }

    /**
     * Discover Java files only in the top-level directory (non-recursive)
     *
     * Uses Files.list() which only lists immediate children,
     * not nested directories.
     */
    private List<JavaFileInfo> discoverTopLevelOnly(Path rootPath) throws IOException {
        List<JavaFileInfo> javaFiles = new ArrayList<>();

        // Files.list() only looks at immediate children
        try (Stream<Path> pathStream = Files.list(rootPath)) {

            pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !config.shouldExclude(path))
                    .forEach(path -> {
                        javaFiles.add(new JavaFileInfo(path));
                        System.out.println("   ✓ Found: " + path.getFileName());
                    });
        }

        System.out.println();
        System.out.println("📁 Total Java files found: " + javaFiles.size());

        return javaFiles;
    }

    /**
     * Alternative method using FileVisitor pattern
     *
     * This is more flexible and gives us more control over the traversal.
     * We're including this for learning purposes - it's another common approach.
     */
    public List<JavaFileInfo> discoverWithVisitor() throws IOException {
        List<JavaFileInfo> javaFiles = new ArrayList<>();
        Path projectPath = config.getProjectPath();

        // FileVisitor lets us define what happens at each step
        Files.walkFileTree(projectPath, new SimpleFileVisitor<Path>() {

            /**
             * Called BEFORE entering a directory
             * We can skip entire directories here
             */
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                // Check if we should skip this directory
                if (config.shouldExclude(dir)) {
                    System.out.println("   ⊘ Skipping directory: " + dir.getFileName());
                    return FileVisitResult.SKIP_SUBTREE;  // Don't go into this folder
                }
                return FileVisitResult.CONTINUE;  // Enter this folder
            }

            /**
             * Called for each FILE we encounter
             */
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                // Check if it's a Java file
                if (file.toString().endsWith(".java")) {
                    javaFiles.add(new JavaFileInfo(file));
                    System.out.println("   ✓ Found: " + file.getFileName());
                }
                return FileVisitResult.CONTINUE;
            }

            /**
             * Called if we couldn't access a file
             */
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                System.err.println("   ⚠ Could not access: " + file);
                return FileVisitResult.CONTINUE;  // Keep going despite the error
            }
        });

        return javaFiles;
    }

    /**
     * Get statistics about the discovered files
     */
    public String getDiscoveryStats(List<JavaFileInfo> files) {
        StringBuilder stats = new StringBuilder();
        stats.append("=== Discovery Statistics ===\n");
        stats.append("Total files: ").append(files.size()).append("\n");
        stats.append("Project: ").append(config.getProjectName()).append("\n");
        stats.append("Path: ").append(config.getProjectPath()).append("\n");

        // List all files found
        stats.append("\nFiles:\n");
        for (JavaFileInfo file : files) {
            stats.append("  - ").append(file.getFileName()).append("\n");
        }

        return stats.toString();
    }
}