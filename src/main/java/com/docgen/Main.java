package com.docgen;

import com.docgen.config.DocGeneratorConfig;
import com.docgen.model.JavaFileInfo;
import com.docgen.service.FileDiscoveryService;
import com.docgen.service.FileReaderService;

import java.io.IOException;
import java.util.List;

/**
 * Main - Entry point for the AI Documentation Generator
 *
 * DAY 1 FUNCTIONALITY:
 * - Configure the project path to analyze
 * - Discover all Java files
 * - Read their contents
 * - Display basic statistics
 *
 * USAGE:
 *   mvn compile exec:java
 *
 * Or with arguments:
 *   mvn compile exec:java -Dexec.args="/path/to/project"
 */
public class Main {

    /**
     * Application entry point
     */
    public static void main(String[] args) {

        // Print banner
        printBanner();

        try {
            // Get the project path (from args or use default)
            String projectPath = getProjectPath(args);

            // Run the documentation generator
            runDocGenerator(projectPath);

        } catch (Exception e) {
            System.err.println();
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Main documentation generation workflow
     */
    private static void runDocGenerator(String projectPath) throws IOException {

        // ============================================================
        // STEP 1: Create Configuration
        // ============================================================
        System.out.println("⚙️  STEP 1: Creating configuration...");
        System.out.println("─".repeat(50));

        DocGeneratorConfig config = DocGeneratorConfig.builder()
                .projectPath(projectPath)
                .projectName("Sample Project")
                .excludePattern("target")       // Skip Maven build folder
                .excludePattern("build")        // Skip Gradle build folder
                .excludePattern(".git")         // Skip git folder
                .excludePattern("test")         // Skip test files for now
                .recursive(true)
                .maxDepth(10)
                .build();

        System.out.println(config);
        System.out.println();

        // ============================================================
        // STEP 2: Discover Java Files
        // ============================================================
        System.out.println("🔍 STEP 2: Discovering Java files...");
        System.out.println("─".repeat(50));

        FileDiscoveryService discoveryService = new FileDiscoveryService(config);
        List<JavaFileInfo> javaFiles = discoveryService.discoverJavaFiles();

        // Check if we found any files
        if (javaFiles.isEmpty()) {
            System.out.println("⚠️  No Java files found in: " + projectPath);
            System.out.println("   Make sure the path contains .java files");
            return;
        }

        System.out.println();

        // ============================================================
        // STEP 3: Read File Contents
        // ============================================================
        System.out.println("📖 STEP 3: Reading file contents...");
        System.out.println("─".repeat(50));

        FileReaderService readerService = new FileReaderService();
        readerService.readAllFiles(javaFiles);

        System.out.println();

        // ============================================================
        // STEP 4: Display Results
        // ============================================================
        System.out.println("📊 STEP 4: Analysis Results");
        System.out.println("─".repeat(50));

        // Print statistics
        System.out.println(readerService.generateReadStats(javaFiles));

        // Print detailed info for each file
        System.out.println("📋 File Details:");
        System.out.println("─".repeat(50));

        for (JavaFileInfo file : javaFiles) {
            printFileDetails(file);
        }

        // Show preview of first file (if any)
        if (!javaFiles.isEmpty()) {
            System.out.println();
            System.out.println("👀 Preview of first file:");
            System.out.println("─".repeat(50));
            System.out.println(readerService.getContentPreview(javaFiles.get(0), 15));
        }

        // ============================================================
        // COMPLETION
        // ============================================================
        System.out.println();
        System.out.println("═".repeat(50));
        System.out.println("✅ Day 1 Complete!");
        System.out.println("   Files discovered: " + javaFiles.size());
        System.out.println("═".repeat(50));
        System.out.println();
        System.out.println("🎯 Next Steps (Day 2):");
        System.out.println("   - Add JavaParser to analyze code structure");
        System.out.println("   - Extract classes, methods, and fields");
        System.out.println("   - Build the Abstract Syntax Tree (AST)");
    }

    /**
     * Get the project path from command line args or use default
     */
    private static String getProjectPath(String[] args) {
        if (args.length > 0) {
            return args[0];
        }

        // Default: analyze our own project's sample folder
        // We'll create sample files to test with
        return "sample-project";
    }

    /**
     * Print details about a single file
     */
    private static void printFileDetails(JavaFileInfo file) {
        System.out.println();
        System.out.println("  📄 " + file.getFileName());
        System.out.println("     Package: " +
                (file.getPackageName().isEmpty() ? "(default)" : file.getPackageName()));
        System.out.println("     Lines: " + file.getLineCount());
        System.out.println("     Size: " + file.getFileSize() + " bytes");
        System.out.println("     Path: " + file.getFilePath());
    }

    /**
     * Print a nice banner
     */
    private static void printBanner() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║                                                    ║");
        System.out.println("║     🤖 AI Documentation Generator                  ║");
        System.out.println("║     ─────────────────────────────                  ║");
        System.out.println("║     Day 1: File Discovery & Reading                ║");
        System.out.println("║                                                    ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
        System.out.println();
    }
}