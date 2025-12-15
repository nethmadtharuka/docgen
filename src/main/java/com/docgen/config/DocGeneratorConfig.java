package com.docgen.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * DocGeneratorConfig - Configuration settings for the documentation generator.
 *
 * This class uses the BUILDER PATTERN which allows us to:
 * 1. Create objects with many optional parameters cleanly
 * 2. Make the configuration immutable (can't change after creation)
 * 3. Have readable code when creating config
 *
 * USAGE EXAMPLE:
 *
 *   DocGeneratorConfig config = DocGeneratorConfig.builder()
 *       .projectPath("/path/to/project")
 *       .outputPath("/path/to/output")
 *       .excludePattern("test")
 *       .build();
 */
public class DocGeneratorConfig {

    // ==================== FIELDS ====================
    // All fields are 'final' = cannot be changed after construction
    // This is called IMMUTABILITY - safer and easier to reason about

    /**
     * Root directory of the project to analyze
     * Example: /home/user/my-java-project
     */
    private final Path projectPath;

    /**
     * Where to write the generated documentation
     * Example: /home/user/my-java-project/docs
     */
    private final Path outputPath;

    /**
     * Patterns to exclude (file/folder names containing these strings)
     * Example: ["test", "build", "target"] - skip test files and build folders
     */
    private final List<String> excludePatterns;

    /**
     * Whether to scan subdirectories (recursive)
     * true = scan all nested folders
     * false = only scan the root directory
     */
    private final boolean recursive;

    /**
     * Maximum depth to scan (if recursive is true)
     * Example: 10 means go 10 folders deep maximum
     */
    private final int maxDepth;

    /**
     * Project name for documentation headers
     */
    private final String projectName;


    // ==================== PRIVATE CONSTRUCTOR ====================
    // Private = only the Builder can create instances
    // This enforces that everyone must use the Builder

    private DocGeneratorConfig(Builder builder) {
        this.projectPath = builder.projectPath;
        this.outputPath = builder.outputPath;
        this.excludePatterns = new ArrayList<>(builder.excludePatterns);
        this.recursive = builder.recursive;
        this.maxDepth = builder.maxDepth;
        this.projectName = builder.projectName;
    }


    // ==================== GETTERS ONLY (No setters!) ====================
    // Immutable = we can get values but never change them

    public Path getProjectPath() {
        return projectPath;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public List<String> getExcludePatterns() {
        // Return a copy to prevent external modification
        return new ArrayList<>(excludePatterns);
    }

    public boolean isRecursive() {
        return recursive;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public String getProjectName() {
        return projectName;
    }

    /**
     * Check if a path should be excluded based on exclude patterns
     *
     * @param path The path to check
     * @return true if this path should be skipped
     */
    public boolean shouldExclude(Path path) {
        String pathString = path.toString().toLowerCase();

        // Check each exclude pattern
        for (String pattern : excludePatterns) {
            if (pathString.contains(pattern.toLowerCase())) {
                return true;  // This path matches an exclude pattern
            }
        }
        return false;  // No patterns matched, include this path
    }


    // ==================== BUILDER PATTERN ====================

    /**
     * Create a new Builder instance
     * This is the entry point for creating a config
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class - Constructs DocGeneratorConfig step by step
     *
     * The Builder pattern works like ordering a custom sandwich:
     * - Start with: DocGeneratorConfig.builder()
     * - Add what you want: .projectPath(...).outputPath(...)
     * - Finish with: .build()
     */
    public static class Builder {

        // Builder has the same fields, but mutable (can change)
        private Path projectPath;
        private Path outputPath;
        private List<String> excludePatterns = new ArrayList<>();
        private boolean recursive = true;  // Default: scan recursively
        private int maxDepth = 20;         // Default: 20 levels deep
        private String projectName = "My Project";  // Default name

        /**
         * Set the project path to analyze (REQUIRED)
         *
         * @param path Path as a String
         * @return this Builder (for chaining)
         */
        public Builder projectPath(String path) {
            this.projectPath = Paths.get(path);
            return this;
        }

        /**
         * Set the project path using a Path object
         */
        public Builder projectPath(Path path) {
            this.projectPath = path;
            return this;
        }

        /**
         * Set where to output documentation (REQUIRED)
         */
        public Builder outputPath(String path) {
            this.outputPath = Paths.get(path);
            return this;
        }

        /**
         * Set output path using a Path object
         */
        public Builder outputPath(Path path) {
            this.outputPath = path;
            return this;
        }

        /**
         * Add a single exclude pattern
         * Files/folders containing this string will be skipped
         *
         * @param pattern String to match against paths
         */
        public Builder excludePattern(String pattern) {
            this.excludePatterns.add(pattern);
            return this;
        }

        /**
         * Add multiple exclude patterns at once
         */
        public Builder excludePatterns(List<String> patterns) {
            this.excludePatterns.addAll(patterns);
            return this;
        }

        /**
         * Set whether to scan recursively (default: true)
         */
        public Builder recursive(boolean recursive) {
            this.recursive = recursive;
            return this;
        }

        /**
         * Set maximum scan depth (default: 20)
         */
        public Builder maxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        /**
         * Set the project name for documentation
         */
        public Builder projectName(String name) {
            this.projectName = name;
            return this;
        }

        /**
         * Build the final configuration object
         *
         * @return A new immutable DocGeneratorConfig
         * @throws IllegalStateException if required fields are missing
         */
        public DocGeneratorConfig build() {
            // Validate required fields
            if (projectPath == null) {
                throw new IllegalStateException(
                        "Project path is required! Use .projectPath(\"/your/path\")"
                );
            }

            // Set default output path if not specified
            if (outputPath == null) {
                outputPath = projectPath.resolve("generated-docs");
            }

            // Add common exclude patterns if none specified
            if (excludePatterns.isEmpty()) {
                excludePatterns.add("target");     // Maven build folder
                excludePatterns.add("build");      // Gradle build folder
                excludePatterns.add(".git");       // Git folder
                excludePatterns.add(".idea");      // IntelliJ folder
                excludePatterns.add("node_modules"); // Node modules
            }

            // Create the immutable config
            return new DocGeneratorConfig(this);
        }
    }


    // ==================== DISPLAY ====================

    @Override
    public String toString() {
        return String.format(
                "DocGeneratorConfig{\n" +
                        "  projectPath=%s\n" +
                        "  outputPath=%s\n" +
                        "  projectName='%s'\n" +
                        "  recursive=%s\n" +
                        "  maxDepth=%d\n" +
                        "  excludePatterns=%s\n" +
                        "}",
                projectPath,
                outputPath,
                projectName,
                recursive,
                maxDepth,
                excludePatterns
        );
    }
}