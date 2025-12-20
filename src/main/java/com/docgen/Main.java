package com.docgen;

import com.docgen.config.DocGeneratorConfig;
import com.docgen.model.*;
import com.docgen.service.CodeAnalyzerService;
import com.docgen.service.FileDiscoveryService;
import com.docgen.service.FileReaderService;
import com.docgen.service.GitAnalyzerService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Main - Entry point for the AI Documentation Generator
 *
 * DAY 3 UPDATE: Added Git history analysis with JGit
 *
 * WORKFLOW:
 * 1. Configure (what to analyze)
 * 2. Discover (find .java files)
 * 3. Read (get file contents)
 * 4. Analyze Code (parse structure with JavaParser)
 * 5. Analyze Git (read history with JGit) ← NEW in Day 3
 * 6. Display results
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
            System.out.println("   Continuing with Git analysis only...");
        }

        System.out.println();

        // ============================================================
        // STEP 3: Read File Contents
        // ============================================================
        if (!javaFiles.isEmpty()) {
            System.out.println("📖 STEP 3: Reading file contents...");
            System.out.println("─".repeat(60));

            FileReaderService readerService = new FileReaderService();
            readerService.readAllFiles(javaFiles);

            System.out.println();
        }

        // ============================================================
        // STEP 4: Analyze Code Structure
        // ============================================================
        if (!javaFiles.isEmpty()) {
            System.out.println("🔬 STEP 4: Analyzing code structure...");
            System.out.println("─".repeat(60));

            CodeAnalyzerService analyzerService = new CodeAnalyzerService();
            analyzerService.analyzeAllFiles(javaFiles);

            System.out.println();
        }

        // ============================================================
        // STEP 5: Analyze Git History (NEW in Day 3!)
        // ============================================================
        System.out.println("📜 STEP 5: Analyzing Git history...");
        System.out.println("─".repeat(60));

        GitAnalyzerService gitService = new GitAnalyzerService();
        List<CommitInfo> commits = analyzeGitHistory(gitService, config.getProjectPath());

        System.out.println();

        // ============================================================
        // STEP 6: Display Results
        // ============================================================
        System.out.println("📊 STEP 6: Analysis Results");
        System.out.println("─".repeat(60));

        // Code analysis results
        if (!javaFiles.isEmpty()) {
            CodeAnalyzerService analyzerService = new CodeAnalyzerService();
            System.out.println(analyzerService.generateAnalysisSummary(javaFiles));
            System.out.println();
        }

        // Git analysis results
        if (!commits.isEmpty()) {
            System.out.println(gitService.generateGitSummary(commits));
            System.out.println();

            // Show recent commits
            printRecentCommits(commits, 5);
        }

        // Detailed file analysis
        if (!javaFiles.isEmpty()) {
            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println("📋 DETAILED FILE ANALYSIS:");
            System.out.println("─".repeat(60));

            for (JavaFileInfo file : javaFiles) {
                printFileAnalysis(file, commits);
            }
        }

        // Cleanup
        gitService.close();

        // ============================================================
        // COMPLETION
        // ============================================================
        System.out.println();
        System.out.println("═".repeat(60));
        System.out.println("✅ Day 3 Complete!");
        System.out.println("   Files analyzed: " + javaFiles.size());
        System.out.println("   Commits analyzed: " + commits.size());
        System.out.println("═".repeat(60));
        System.out.println();
        System.out.println("🎯 Next Steps (Day 4):");
        System.out.println("   - Add NLP processing for comments");
        System.out.println("   - Generate documentation descriptions");
        System.out.println("   - Improve code understanding");
    }

    /**
     * Analyze Git history for the project
     */
    private static List<CommitInfo> analyzeGitHistory(GitAnalyzerService gitService, Path projectPath) {
        List<CommitInfo> commits = List.of();

        // Try to open as Git repository
        if (gitService.openRepository(projectPath)) {
            // Get commit history (limit to 50 for demo)
            commits = gitService.getCommitHistory(50);
        } else {
            System.out.println("   ℹ️  No Git repository found at: " + projectPath);
            System.out.println("   ℹ️  To test Git features, run this on a Git repository.");
            System.out.println();
            System.out.println("   💡 TIP: Initialize a git repo in your project:");
            System.out.println("      cd " + projectPath);
            System.out.println("      git init");
            System.out.println("      git add .");
            System.out.println("      git commit -m \"Initial commit\"");
        }

        return commits;
    }

    /**
     * Print recent commits
     */
    private static void printRecentCommits(List<CommitInfo> commits, int limit) {
        System.out.println("─".repeat(60));
        System.out.println("📝 RECENT COMMITS:");
        System.out.println("─".repeat(60));

        int count = Math.min(commits.size(), limit);
        for (int i = 0; i < count; i++) {
            CommitInfo commit = commits.get(i);
            printCommitInfo(commit);
        }

        if (commits.size() > limit) {
            System.out.println("   ... and " + (commits.size() - limit) + " more commits");
        }
    }

    /**
     * Print info about a single commit
     */
    private static void printCommitInfo(CommitInfo commit) {
        System.out.println();
        System.out.println("   ┌─────────────────────────────────────────────────────");
        System.out.println("   │ 🔹 " + commit.getShortHash() + " - " + truncate(commit.getSubject(), 45));
        System.out.println("   │    Author: " + commit.getAuthorName() + " <" + commit.getAuthorEmail() + ">");
        System.out.println("   │    Date:   " + commit.getAuthorDate());

        // Show file changes
        if (!commit.getFileChanges().isEmpty()) {
            System.out.println("   │    Changes: " + commit.getFileCount() + " file(s) " +
                    "(+" + commit.getTotalLinesAdded() + "/-" + commit.getTotalLinesDeleted() + " lines)");

            // Show up to 3 file changes
            int shown = 0;
            for (FileChangeInfo change : commit.getFileChanges()) {
                if (shown >= 3) {
                    System.out.println("   │      ... and " +
                            (commit.getFileChanges().size() - 3) + " more files");
                    break;
                }
                System.out.println("   │      " + change.getChangeIcon() + " " + change.getPath());
                shown++;
            }
        }

        System.out.println("   └─────────────────────────────────────────────────────");
    }

    /**
     * Print detailed analysis of a file including Git history
     */
    private static void printFileAnalysis(JavaFileInfo file, List<CommitInfo> allCommits) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║ 📄 " + padRight(file.getFileName(), 55) + "║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        System.out.println("   Package: " +
                (file.getPackageName() != null ? file.getPackageName() : "(default)"));
        System.out.println("   Lines: " + file.getLineCount());

        // Code analysis
        if (file.isParsed()) {
            for (ClassInfo classInfo : file.getClasses()) {
                printClassInfoCompact(classInfo);
            }
        } else if (file.getParseError() != null) {
            System.out.println("   ⚠️  Parse Error: " + file.getParseError());
        }

        // Git history for this file
        if (!allCommits.isEmpty()) {
            String relativePath = getRelativePath(file);
            List<CommitInfo> fileCommits = findCommitsForFile(allCommits, relativePath);

            if (!fileCommits.isEmpty()) {
                System.out.println();
                System.out.println("   📜 GIT HISTORY (" + fileCommits.size() + " commits):");

                int shown = 0;
                for (CommitInfo commit : fileCommits) {
                    if (shown >= 3) {
                        System.out.println("      ... and " + (fileCommits.size() - 3) + " more commits");
                        break;
                    }
                    System.out.println("      • " + commit.getShortHash() + " - " +
                            truncate(commit.getSubject(), 35) + " (" + commit.getAuthorName() + ")");
                    shown++;
                }
            }
        }
    }

    /**
     * Find commits that affected a specific file
     */
    private static List<CommitInfo> findCommitsForFile(List<CommitInfo> commits, String filePath) {
        return commits.stream()
                .filter(c -> c.hasFileChange(filePath) ||
                        c.getFileChanges().stream()
                                .anyMatch(f -> f.getPath().endsWith(filePath) ||
                                        filePath.endsWith(f.getPath())))
                .toList();
    }

    /**
     * Get relative path for matching with git
     */
    private static String getRelativePath(JavaFileInfo file) {
        String path = file.getFilePath().toString();
        // Try to get just the filename for matching
        return file.getFileName();
    }

    /**
     * Print compact class info
     */
    private static void printClassInfoCompact(ClassInfo classInfo) {
        System.out.println();
        System.out.println("   ├─ " + classInfo.getClassType() + ": " + classInfo.getName());
        System.out.println("   │  Fields: " + classInfo.getFields().size() +
                ", Methods: " + classInfo.getMethods().size());

        if (classInfo.getJavadoc() != null) {
            System.out.println("   │  Description: " + truncate(classInfo.getJavadoc(), 50));
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
     * Truncate string to max length
     */
    private static String truncate(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 3) + "...";
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
        System.out.println("║     Day 3: Git History Integration                         ║");
        System.out.println("║                                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}