package com.docgen.service;

import com.docgen.model.CommitInfo;
import com.docgen.model.FileChangeInfo;
import com.docgen.model.FileChangeInfo.ChangeType;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GitAnalyzerService - Analyzes Git repository history using JGit.
 *
 * ╔═══════════════════════════════════════════════════════════════════╗
 * ║                     HOW JGIT WORKS                                ║
 * ╠═══════════════════════════════════════════════════════════════════╣
 * ║                                                                   ║
 * ║  1. OPEN REPOSITORY                                               ║
 * ║     Git git = Git.open(new File("/path/to/repo"))                ║
 * ║                                                                   ║
 * ║  2. GET COMMIT LOG                                                ║
 * ║     Iterable<RevCommit> log = git.log().all().call()             ║
 * ║                                                                   ║
 * ║  3. FOR EACH COMMIT:                                              ║
 * ║     - Get hash:    commit.getName()                              ║
 * ║     - Get author:  commit.getAuthorIdent()                       ║
 * ║     - Get message: commit.getFullMessage()                       ║
 * ║     - Get changes: Use DiffFormatter to compare with parent      ║
 * ║                                                                   ║
 * ║  KEY JGIT CLASSES:                                                ║
 * ║  ─────────────────                                                ║
 * ║  • Repository  - Represents the .git directory                   ║
 * ║  • Git         - High-level API for git operations               ║
 * ║  • RevCommit   - A commit object                                 ║
 * ║  • RevWalk     - Iterator for walking commit history             ║
 * ║  • DiffEntry   - Represents a file change                        ║
 * ║  • PersonIdent - Author/committer info (name, email, date)       ║
 * ║                                                                   ║
 * ╚═══════════════════════════════════════════════════════════════════╝
 */
public class GitAnalyzerService {

    // The Git repository we're analyzing
    private Repository repository;
    private Git git;

    // Path to the repository
    private Path repositoryPath;

    // Whether we successfully connected to a repo
    private boolean isConnected;

    // Error message if connection failed
    private String connectionError;

    /**
     * Default constructor
     */
    public GitAnalyzerService() {
        this.isConnected = false;
    }

    /**
     * Open a Git repository at the given path.
     *
     * The path can be:
     * - The .git directory itself
     * - The project root (parent of .git)
     *
     * @param projectPath Path to the project or .git directory
     * @return true if successfully opened
     */
    public boolean openRepository(Path projectPath) {
        this.repositoryPath = projectPath;

        try {
            // FileRepositoryBuilder can find .git in the given path or parent
            FileRepositoryBuilder builder = new FileRepositoryBuilder();

            repository = builder
                    .setGitDir(findGitDir(projectPath))
                    .readEnvironment()  // Read system git config
                    .findGitDir()       // Scan up to find .git
                    .build();

            git = new Git(repository);
            isConnected = true;

            System.out.println("   ✓ Connected to Git repository: " +
                    repository.getDirectory().getAbsolutePath());

            return true;

        } catch (IOException e) {
            connectionError = "Could not open repository: " + e.getMessage();
            isConnected = false;
            System.out.println("   ⚠️  " + connectionError);
            return false;
        }
    }

    /**
     * Find the .git directory from a project path
     */
    private File findGitDir(Path projectPath) {
        File path = projectPath.toFile();

        // If path is already .git directory
        if (path.getName().equals(".git") && path.isDirectory()) {
            return path;
        }

        // Check if .git exists in the given path
        File gitDir = new File(path, ".git");
        if (gitDir.exists() && gitDir.isDirectory()) {
            return gitDir;
        }

        // Return the path and let JGit find .git
        return path;
    }

    /**
     * Get all commits from the repository.
     *
     * @param maxCommits Maximum number of commits to retrieve (0 = all)
     * @return List of CommitInfo objects
     */
    public List<CommitInfo> getCommitHistory(int maxCommits) {
        List<CommitInfo> commits = new ArrayList<>();

        if (!isConnected) {
            System.out.println("   ⚠️  Not connected to repository");
            return commits;
        }

        try {
            // git.log() is like running "git log" command
            Iterable<RevCommit> log = git.log()
                    .all()  // Get all branches, not just current
                    .call();

            int count = 0;
            for (RevCommit revCommit : log) {
                if (maxCommits > 0 && count >= maxCommits) {
                    break;
                }

                CommitInfo commitInfo = extractCommitInfo(revCommit);
                commits.add(commitInfo);
                count++;
            }

            System.out.println("   ✓ Retrieved " + commits.size() + " commits");

        } catch (GitAPIException | IOException e) {
            System.err.println("   ✗ Error getting commit history: " + e.getMessage());
        }

        return commits;
    }

    /**
     * Get commits that affected a specific file.
     *
     * @param filePath Path to the file (relative to repo root)
     * @param maxCommits Maximum commits to retrieve
     * @return List of commits that changed this file
     */
    public List<CommitInfo> getFileHistory(String filePath, int maxCommits) {
        List<CommitInfo> commits = new ArrayList<>();

        if (!isConnected) {
            return commits;
        }

        try {
            // Add path filter to only get commits affecting this file
            Iterable<RevCommit> log = git.log()
                    .addPath(filePath)
                    .call();

            int count = 0;
            for (RevCommit revCommit : log) {
                if (maxCommits > 0 && count >= maxCommits) {
                    break;
                }

                CommitInfo commitInfo = extractCommitInfo(revCommit);
                commits.add(commitInfo);
                count++;
            }

        } catch (GitAPIException | IOException e) {
            System.err.println("Error getting file history: " + e.getMessage());
        }

        return commits;
    }

    /**
     * Get commits by a specific author.
     *
     * @param authorName Author name or email to search for
     * @param maxCommits Maximum commits to retrieve
     * @return List of commits by this author
     */
    public List<CommitInfo> getCommitsByAuthor(String authorName, int maxCommits) {
        List<CommitInfo> allCommits = getCommitHistory(0);

        return allCommits.stream()
                .filter(c ->
                        (c.getAuthorName() != null && c.getAuthorName().contains(authorName)) ||
                                (c.getAuthorEmail() != null && c.getAuthorEmail().contains(authorName))
                )
                .limit(maxCommits > 0 ? maxCommits : Long.MAX_VALUE)
                .collect(Collectors.toList());
    }

    /**
     * Extract CommitInfo from a JGit RevCommit object.
     * This is where we convert JGit's format to our model.
     */
    private CommitInfo extractCommitInfo(RevCommit revCommit) throws IOException {
        CommitInfo info = new CommitInfo();

        // ===== Basic commit info =====
        info.setHash(revCommit.getName());  // Full SHA-1 hash
        info.setFullMessage(revCommit.getFullMessage());

        // ===== Author info =====
        PersonIdent author = revCommit.getAuthorIdent();
        if (author != null) {
            info.setAuthorName(author.getName());
            info.setAuthorEmail(author.getEmailAddress());
            info.setAuthorDate(convertToLocalDateTime(author.getWhen()));
        }

        // ===== Committer info =====
        PersonIdent committer = revCommit.getCommitterIdent();
        if (committer != null) {
            info.setCommitterName(committer.getName());
            info.setCommitterEmail(committer.getEmailAddress());
            info.setCommitDate(convertToLocalDateTime(committer.getWhen()));
        }

        // ===== Parent commits =====
        for (RevCommit parent : revCommit.getParents()) {
            info.addParentHash(parent.getName());
        }

        // ===== File changes =====
        List<FileChangeInfo> fileChanges = getFileChanges(revCommit);
        info.setFileChanges(fileChanges);

        return info;
    }

    /**
     * Get the list of file changes in a commit.
     *
     * This compares the commit to its parent to see what changed.
     * For the first commit (no parent), compares to empty tree.
     */
    private List<FileChangeInfo> getFileChanges(RevCommit commit) throws IOException {
        List<FileChangeInfo> changes = new ArrayList<>();

        try (ObjectReader reader = repository.newObjectReader();
             DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {

            diffFormatter.setRepository(repository);
            diffFormatter.setDetectRenames(true);  // Detect file renames

            // Get the tree iterator for this commit
            AbstractTreeIterator newTreeIter = prepareTreeParser(commit);

            // Get the tree iterator for parent (or empty tree if first commit)
            AbstractTreeIterator oldTreeIter;
            if (commit.getParentCount() > 0) {
                RevCommit parent = commit.getParent(0);
                // Need to parse the parent to access its tree
                try (RevWalk revWalk = new RevWalk(repository)) {
                    parent = revWalk.parseCommit(parent.getId());
                }
                oldTreeIter = prepareTreeParser(parent);
            } else {
                // First commit - compare to empty tree
                oldTreeIter = new EmptyTreeIterator();
            }

            // Get the diff entries (list of changed files)
            List<DiffEntry> diffs = diffFormatter.scan(oldTreeIter, newTreeIter);

            for (DiffEntry diff : diffs) {
                FileChangeInfo change = new FileChangeInfo();

                // Set path based on change type
                switch (diff.getChangeType()) {
                    case ADD:
                        change.setPath(diff.getNewPath());
                        change.setChangeType(ChangeType.ADD);
                        break;
                    case DELETE:
                        change.setPath(diff.getOldPath());
                        change.setChangeType(ChangeType.DELETE);
                        break;
                    case MODIFY:
                        change.setPath(diff.getNewPath());
                        change.setChangeType(ChangeType.MODIFY);
                        break;
                    case RENAME:
                        change.setPath(diff.getNewPath());
                        change.setOldPath(diff.getOldPath());
                        change.setChangeType(ChangeType.RENAME);
                        break;
                    case COPY:
                        change.setPath(diff.getNewPath());
                        change.setOldPath(diff.getOldPath());
                        change.setChangeType(ChangeType.COPY);
                        break;
                }

                // Get line counts (added/deleted)
                try {
                    EditList editList = diffFormatter.toFileHeader(diff).toEditList();
                    int linesAdded = 0;
                    int linesDeleted = 0;

                    for (Edit edit : editList) {
                        linesAdded += edit.getEndB() - edit.getBeginB();
                        linesDeleted += edit.getEndA() - edit.getBeginA();
                    }

                    change.setLinesAdded(linesAdded);
                    change.setLinesDeleted(linesDeleted);
                } catch (Exception e) {
                    // Line count calculation failed, leave at 0
                }

                changes.add(change);
            }
        }

        return changes;
    }

    /**
     * Prepare a tree parser for a commit (needed for diff comparison)
     */
    private AbstractTreeIterator prepareTreeParser(RevCommit commit) throws IOException {
        try (RevWalk walk = new RevWalk(repository)) {
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            return treeParser;
        }
    }

    /**
     * Convert java.util.Date to LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(date.getTime()),
                ZoneId.systemDefault()
        );
    }

    // ==================== STATISTICS & ANALYSIS ====================

    /**
     * Get a map of author names to their commit counts
     */
    public Map<String, Integer> getAuthorCommitCounts(List<CommitInfo> commits) {
        Map<String, Integer> counts = new HashMap<>();

        for (CommitInfo commit : commits) {
            String author = commit.getAuthorName();
            counts.merge(author, 1, Integer::sum);
        }

        return counts;
    }

    /**
     * Get statistics about file changes
     */
    public Map<String, Integer> getFileChangeCounts(List<CommitInfo> commits) {
        Map<String, Integer> counts = new HashMap<>();

        for (CommitInfo commit : commits) {
            for (FileChangeInfo change : commit.getFileChanges()) {
                String path = change.getPath();
                counts.merge(path, 1, Integer::sum);
            }
        }

        return counts;
    }

    /**
     * Get the most frequently changed files
     */
    public List<Map.Entry<String, Integer>> getMostChangedFiles(List<CommitInfo> commits, int limit) {
        Map<String, Integer> counts = getFileChangeCounts(commits);

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Generate a summary of the Git history
     */
    public String generateGitSummary(List<CommitInfo> commits) {
        if (commits.isEmpty()) {
            return "No commits found.";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("=== Git History Summary ===\n");
        sb.append("Total commits: ").append(commits.size()).append("\n");

        // Date range
        CommitInfo oldest = commits.get(commits.size() - 1);
        CommitInfo newest = commits.get(0);
        sb.append("Date range: ").append(oldest.getShortDateString())
                .append(" → ").append(newest.getShortDateString()).append("\n");

        // Authors
        Map<String, Integer> authorCounts = getAuthorCommitCounts(commits);
        sb.append("Authors: ").append(authorCounts.size()).append("\n");

        // Top authors
        sb.append("\nTop contributors:\n");
        authorCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> sb.append("  • ").append(e.getKey())
                        .append(": ").append(e.getValue()).append(" commits\n"));

        // Most changed files
        sb.append("\nMost frequently changed files:\n");
        getMostChangedFiles(commits, 5).forEach(e ->
                sb.append("  • ").append(e.getKey())
                        .append(": ").append(e.getValue()).append(" changes\n")
        );

        // Total changes
        int totalAdded = commits.stream().mapToInt(CommitInfo::getTotalLinesAdded).sum();
        int totalDeleted = commits.stream().mapToInt(CommitInfo::getTotalLinesDeleted).sum();
        sb.append("\nTotal lines: +").append(totalAdded).append(" / -").append(totalDeleted).append("\n");

        return sb.toString();
    }

    // ==================== CLEANUP ====================

    /**
     * Close the repository connection
     */
    public void close() {
        if (git != null) {
            git.close();
        }
        if (repository != null) {
            repository.close();
        }
        isConnected = false;
    }

    // ==================== GETTERS ====================

    public boolean isConnected() {
        return isConnected;
    }

    public String getConnectionError() {
        return connectionError;
    }

    public Path getRepositoryPath() {
        return repositoryPath;
    }
}