package com.docgen.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CommitInfo - Stores complete information about a Git commit.
 *
 * A Git commit contains:
 *
 *   commit abc123def456...              ← Hash (unique ID)
 *   Author: John Doe <john@email.com>   ← Author name and email
 *   Date:   Mon Dec 18 14:30:00 2024    ← When committed
 *
 *       Add user authentication          ← Commit message (first line = subject)
 *
 *       This commit adds login and       ← Commit message (rest = body)
 *       logout functionality.
 *
 *   Files changed:
 *     A  src/LoginService.java           ← Added file
 *     M  src/User.java                   ← Modified file
 *     D  src/OldAuth.java                ← Deleted file
 *
 * This class captures ALL of that information.
 */
public class CommitInfo {

    // ==================== FIELDS ====================

    /**
     * The full commit hash (40 characters)
     * Example: "abc123def456789012345678901234567890abcd"
     */
    private String hash;

    /**
     * The short commit hash (first 7 characters)
     * Example: "abc123d"
     */
    private String shortHash;

    /**
     * Author's name
     * Example: "John Doe"
     */
    private String authorName;

    /**
     * Author's email
     * Example: "john.doe@company.com"
     */
    private String authorEmail;

    /**
     * When the commit was authored
     */
    private LocalDateTime authorDate;

    /**
     * Committer's name (usually same as author)
     */
    private String committerName;

    /**
     * Committer's email
     */
    private String committerEmail;

    /**
     * When the commit was committed (may differ from author date)
     */
    private LocalDateTime commitDate;

    /**
     * The commit message subject (first line)
     * Example: "Add user authentication"
     */
    private String subject;

    /**
     * The full commit message (includes subject and body)
     */
    private String fullMessage;

    /**
     * List of file changes in this commit
     */
    private List<FileChangeInfo> fileChanges;

    /**
     * Parent commit hashes (usually 1, but 2 for merge commits)
     */
    private List<String> parentHashes;

    /**
     * Whether this is a merge commit (has multiple parents)
     */
    private boolean isMergeCommit;


    // ==================== CONSTRUCTOR ====================

    /**
     * Default constructor
     */
    public CommitInfo() {
        this.fileChanges = new ArrayList<>();
        this.parentHashes = new ArrayList<>();
    }

    /**
     * Constructor with hash
     */
    public CommitInfo(String hash) {
        this();
        setHash(hash);
    }


    // ==================== GETTERS & SETTERS ====================

    public String getHash() {
        return hash;
    }

    public CommitInfo setHash(String hash) {
        this.hash = hash;
        // Automatically set short hash
        if (hash != null && hash.length() >= 7) {
            this.shortHash = hash.substring(0, 7);
        }
        return this;
    }

    public String getShortHash() {
        return shortHash;
    }

    public String getAuthorName() {
        return authorName;
    }

    public CommitInfo setAuthorName(String authorName) {
        this.authorName = authorName;
        return this;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public CommitInfo setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
        return this;
    }

    public LocalDateTime getAuthorDate() {
        return authorDate;
    }

    public CommitInfo setAuthorDate(LocalDateTime authorDate) {
        this.authorDate = authorDate;
        return this;
    }

    public String getCommitterName() {
        return committerName;
    }

    public CommitInfo setCommitterName(String committerName) {
        this.committerName = committerName;
        return this;
    }

    public String getCommitterEmail() {
        return committerEmail;
    }

    public CommitInfo setCommitterEmail(String committerEmail) {
        this.committerEmail = committerEmail;
        return this;
    }

    public LocalDateTime getCommitDate() {
        return commitDate;
    }

    public CommitInfo setCommitDate(LocalDateTime commitDate) {
        this.commitDate = commitDate;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public CommitInfo setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public CommitInfo setFullMessage(String fullMessage) {
        this.fullMessage = fullMessage;
        // Extract subject (first line) from full message
        if (fullMessage != null && !fullMessage.isEmpty()) {
            int newlineIndex = fullMessage.indexOf('\n');
            if (newlineIndex > 0) {
                this.subject = fullMessage.substring(0, newlineIndex).trim();
            } else {
                this.subject = fullMessage.trim();
            }
        }
        return this;
    }

    public List<FileChangeInfo> getFileChanges() {
        return fileChanges;
    }

    public CommitInfo setFileChanges(List<FileChangeInfo> fileChanges) {
        this.fileChanges = fileChanges;
        return this;
    }

    public CommitInfo addFileChange(FileChangeInfo change) {
        this.fileChanges.add(change);
        return this;
    }

    public List<String> getParentHashes() {
        return parentHashes;
    }

    public CommitInfo setParentHashes(List<String> parentHashes) {
        this.parentHashes = parentHashes;
        this.isMergeCommit = parentHashes.size() > 1;
        return this;
    }

    public CommitInfo addParentHash(String parentHash) {
        this.parentHashes.add(parentHash);
        this.isMergeCommit = parentHashes.size() > 1;
        return this;
    }

    public boolean isMergeCommit() {
        return isMergeCommit;
    }


    // ==================== UTILITY METHODS ====================

    /**
     * Get the number of files changed in this commit
     */
    public int getFileCount() {
        return fileChanges.size();
    }

    /**
     * Get total lines added across all files
     */
    public int getTotalLinesAdded() {
        return fileChanges.stream()
                .mapToInt(FileChangeInfo::getLinesAdded)
                .sum();
    }

    /**
     * Get total lines deleted across all files
     */
    public int getTotalLinesDeleted() {
        return fileChanges.stream()
                .mapToInt(FileChangeInfo::getLinesDeleted)
                .sum();
    }

    /**
     * Get only Java file changes
     */
    public List<FileChangeInfo> getJavaFileChanges() {
        return fileChanges.stream()
                .filter(FileChangeInfo::isJavaFile)
                .collect(Collectors.toList());
    }

    /**
     * Get files by change type
     */
    public List<FileChangeInfo> getFilesByType(FileChangeInfo.ChangeType type) {
        return fileChanges.stream()
                .filter(f -> f.getChangeType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Check if a specific file was changed in this commit
     */
    public boolean hasFileChange(String filePath) {
        return fileChanges.stream()
                .anyMatch(f -> f.getPath().equals(filePath));
    }

    /**
     * Get a formatted author string
     * Example: "John Doe <john@email.com>"
     */
    public String getAuthorString() {
        if (authorEmail != null && !authorEmail.isEmpty()) {
            return authorName + " <" + authorEmail + ">";
        }
        return authorName;
    }

    /**
     * Get a short date string
     * Example: "Dec 18, 2024"
     */
    public String getShortDateString() {
        if (authorDate == null) return "";
        return String.format("%s %d, %d",
                authorDate.getMonth().toString().substring(0, 3),
                authorDate.getDayOfMonth(),
                authorDate.getYear()
        );
    }

    /**
     * Get a one-line summary of the commit
     * Example: "abc123d - Add user auth (John, Dec 18)"
     */
    public String getOneLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(shortHash);
        sb.append(" - ");

        String subj = subject != null ? subject : "(no message)";
        if (subj.length() > 50) {
            subj = subj.substring(0, 47) + "...";
        }
        sb.append(subj);

        sb.append(" (");
        sb.append(authorName != null ? authorName.split(" ")[0] : "Unknown");
        sb.append(", ");
        sb.append(getShortDateString());
        sb.append(")");

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("commit ").append(hash).append("\n");

        if (isMergeCommit) {
            sb.append("Merge: ");
            for (String parent : parentHashes) {
                sb.append(parent.substring(0, 7)).append(" ");
            }
            sb.append("\n");
        }

        sb.append("Author: ").append(getAuthorString()).append("\n");
        sb.append("Date:   ").append(authorDate).append("\n");
        sb.append("\n");
        sb.append("    ").append(subject != null ? subject : "(no message)").append("\n");

        if (fullMessage != null && fullMessage.contains("\n")) {
            String body = fullMessage.substring(fullMessage.indexOf('\n') + 1).trim();
            if (!body.isEmpty()) {
                sb.append("\n");
                for (String line : body.split("\n")) {
                    sb.append("    ").append(line).append("\n");
                }
            }
        }

        if (!fileChanges.isEmpty()) {
            sb.append("\n");
            sb.append("Files changed (").append(fileChanges.size()).append("):\n");
            for (FileChangeInfo change : fileChanges) {
                sb.append("  ").append(change).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Get a compact representation
     */
    public String toCompactString() {
        return String.format("%s | %s | %s | %s (+%d/-%d)",
                shortHash,
                authorName,
                getShortDateString(),
                subject,
                getTotalLinesAdded(),
                getTotalLinesDeleted()
        );
    }
}