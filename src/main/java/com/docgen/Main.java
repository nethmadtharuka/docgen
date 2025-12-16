package com.docgen;

import com.docgen.config.DocGeneratorConfig;
import com.docgen.model.*;
import com.docgen.service.CodeAnalyzerService;
import com.docgen.service.FileDiscoveryService;
import com.docgen.service.FileReaderService;

import java.io.IOException;
import java.util.List;

/**
 * Main - Entry point for the AI Documentation Generator
 *
 * DAY 2 UPDATE: Added code analysis with JavaParser
 *
 * WORKFLOW:
 * 1. Configure (what to analyze)
 * 2. Discover (find .java files)
 * 3. Read (get file contents)
 * 4. Analyze (parse code structure) ← NEW in Day 2
 * 5. Display results
 */
public class Main {

    public static void main(String[] args) {
        printBanner();

        try {
            String projectPath = getProjectPath(args);
            runDocGenerator(projectPath);

        } catch (Exception e) {
            System.err.println();
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void runDocGenerator(String projectPath) throws IOException {

        // ============================================================
        // STEP 1: Create Configuration
        // ============================================================
        System.out.println("⚙️  STEP 1: Creating configuration...");
        System.out.println("─".repeat(60));

        DocGeneratorConfig config = DocGeneratorConfig.builder()
                .projectPath(projectPath)
                .projectName("Sample Project")
                .excludePattern("target")
                .excludePattern("build")
                .excludePattern(".git")
                .excludePattern("test")
                .recursive(true)
                .maxDepth(10)
                .build();

        System.out.println("Project: " + config.getProjectPath());
        System.out.println();

        // ============================================================
        // STEP 2: Discover Java Files
        // ============================================================
        System.out.println("🔍 STEP 2: Discovering Java files...");
        System.out.println("─".repeat(60));

        FileDiscoveryService discoveryService = new FileDiscoveryService(config);
        List<JavaFileInfo> javaFiles = discoveryService.discoverJavaFiles();

        if (javaFiles.isEmpty()) {
            System.out.println("⚠️  No Java files found in: " + projectPath);
            return;
        }

        System.out.println();

        // ============================================================
        // STEP 3: Read File Contents
        // ============================================================
        System.out.println("📖 STEP 3: Reading file contents...");
        System.out.println("─".repeat(60));

        FileReaderService readerService = new FileReaderService();
        readerService.readAllFiles(javaFiles);

        System.out.println();

        // ============================================================
        // STEP 4: Analyze Code Structure (NEW in Day 2!)
        // ============================================================
        System.out.println("🔬 STEP 4: Analyzing code structure with JavaParser...");
        System.out.println("─".repeat(60));

        CodeAnalyzerService analyzerService = new CodeAnalyzerService();
        analyzerService.analyzeAllFiles(javaFiles);

        System.out.println();

        // ============================================================
        // STEP 5: Display Detailed Results
        // ============================================================
        System.out.println("📊 STEP 5: Analysis Results");
        System.out.println("─".repeat(60));

        // Print summary
        System.out.println(analyzerService.generateAnalysisSummary(javaFiles));

        // Print detailed info for each file
        System.out.println("─".repeat(60));
        System.out.println("📋 DETAILED FILE ANALYSIS:");
        System.out.println("─".repeat(60));

        for (JavaFileInfo file : javaFiles) {
            printFileAnalysis(file);
        }

        // ============================================================
        // COMPLETION
        // ============================================================
        System.out.println();
        System.out.println("═".repeat(60));
        System.out.println("✅ Day 2 Complete!");
        System.out.println("   Files analyzed: " + javaFiles.size());
        System.out.println("   Successfully parsed: " +
                javaFiles.stream().filter(JavaFileInfo::isParsed).count());
        System.out.println("═".repeat(60));
        System.out.println();
        System.out.println("🎯 Next Steps (Day 3):");
        System.out.println("   - Add Git integration with JGit");
        System.out.println("   - Extract commit history");
        System.out.println("   - Track file changes over time");
    }

    /**
     * Print detailed analysis of a single file
     */
    private static void printFileAnalysis(JavaFileInfo file) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║ 📄 " + padRight(file.getFileName(), 55) + "║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        System.out.println("   Package: " +
                (file.getPackageName() != null ? file.getPackageName() : "(default)"));
        System.out.println("   Lines: " + file.getLineCount());

        if (!file.isParsed()) {
            System.out.println("   ⚠️  Parse Error: " + file.getParseError());
            return;
        }

        // Print imports
        if (!file.getImports().isEmpty()) {
            System.out.println("   Imports: " + file.getImports().size());
            for (String imp : file.getImports()) {
                System.out.println("      └─ " + imp);
            }
        }

        // Print each class
        for (ClassInfo classInfo : file.getClasses()) {
            printClassInfo(classInfo, "   ");
        }
    }

    /**
     * Print detailed info about a class
     */
    private static void printClassInfo(ClassInfo classInfo, String indent) {
        System.out.println();
        System.out.println(indent + "┌─────────────────────────────────────────────────────");
        System.out.println(indent + "│ " + classInfo.getClassType() + ": " + classInfo.getName());
        System.out.println(indent + "├─────────────────────────────────────────────────────");

        // Print signature
        System.out.println(indent + "│ Signature: " + classInfo.getSignature());

        // Print annotations
        if (!classInfo.getAnnotations().isEmpty()) {
            System.out.println(indent + "│ Annotations: " + classInfo.getAnnotations());
        }

        // Print Javadoc
        if (classInfo.getJavadoc() != null && !classInfo.getJavadoc().isEmpty()) {
            String javadoc = classInfo.getJavadoc();
            if (javadoc.length() > 60) {
                javadoc = javadoc.substring(0, 57) + "...";
            }
            System.out.println(indent + "│ Description: " + javadoc);
        }

        // Print fields
        if (!classInfo.getFields().isEmpty()) {
            System.out.println(indent + "│");
            System.out.println(indent + "│ 📦 FIELDS (" + classInfo.getFields().size() + "):");
            for (FieldInfo field : classInfo.getFields()) {
                System.out.println(indent + "│    • " + field.getSignature());
            }
        }

        // Print constructors
        List<MethodInfo> constructors = classInfo.getConstructors();
        if (!constructors.isEmpty()) {
            System.out.println(indent + "│");
            System.out.println(indent + "│ 🔨 CONSTRUCTORS (" + constructors.size() + "):");
            for (MethodInfo ctor : constructors) {
                System.out.println(indent + "│    • " + ctor.getShortSignature());
            }
        }

        // Print methods
        List<MethodInfo> methods = classInfo.getNonConstructorMethods();
        if (!methods.isEmpty()) {
            System.out.println(indent + "│");
            System.out.println(indent + "│ ⚡ METHODS (" + methods.size() + "):");
            for (MethodInfo method : methods) {
                String signature = method.getShortSignature();
                String returnType = method.getReturnType();

                // Add visibility icon
                String visibility = switch (method.getVisibility()) {
                    case "public" -> "🟢";
                    case "private" -> "🔴";
                    case "protected" -> "🟡";
                    default -> "⚪";
                };

                System.out.println(indent + "│    " + visibility + " " +
                        returnType + " " + signature);

                // Print method description if available
                if (method.getJavadoc() != null && !method.getJavadoc().isEmpty()) {
                    String desc = method.getJavadoc();
                    if (desc.length() > 50) {
                        desc = desc.substring(0, 47) + "...";
                    }
                    System.out.println(indent + "│       └─ " + desc);
                }
            }
        }

        System.out.println(indent + "└─────────────────────────────────────────────────────");

        // Print nested classes
        for (ClassInfo nested : classInfo.getNestedClasses()) {
            printClassInfo(nested, indent + "   ");
        }
    }

    /**
     * Get project path from args or default
     */
    private static String getProjectPath(String[] args) {
        if (args.length > 0) {
            return args[0];
        }
        return "sample-project";
    }

    /**
     * Pad string to fixed width
     */
    private static String padRight(String s, int width) {
        if (s.length() >= width) {
            return s.substring(0, width);
        }
        return s + " ".repeat(width - s.length());
    }

    /**
     * Print banner
     */
    private static void printBanner() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                            ║");
        System.out.println("║     🤖 AI Documentation Generator                          ║");
        System.out.println("║     ─────────────────────────────                          ║");
        System.out.println("║     Day 2: Code Analysis with JavaParser                   ║");
        System.out.println("║                                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}