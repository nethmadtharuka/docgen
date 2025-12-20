package com.docgen.model;

/**
 * FileChangeInfo - Stores information about a file change in a Git commit.
 *
 * When you commit to Git, each file can have one of these change types:
 *
 *   ADD      - New file was created
 *   MODIFY   - Existing file was changed
 *   DELETE   - File was removed
 *   RENAME   - File was renamed (possibly also modified)
 *   COPY     - File was copied from another file
 *
 * EXAMPLE:
 *   In commit "abc123" that "Added user authentication":
 *
 *   FileChangeInfo {
 *     path = "src/main/java/LoginService.java"
 *     changeType = ADD
 *     linesAdded = 150
 *     linesDeleted = 0
 *   }
 *
 *   FileChangeInfo {
 *     path = "src/main/java/User.java"
 *     changeType = MODIFY
 *     linesAdded = 25
 *     linesDeleted = 5
 *   }
 */
public class FileChangeInfo {

    // ==================== ENUMS ====================

    /**
     * Type of change made to the file
     */
    public enum ChangeType {
        ADD,      // New file added
        MODIFY,   // Existing file modified
        DELETE,   // File deleted
        RENAME,   // File renamed
        COPY      // File copied
    }


    // ==================== FIELDS ====================

    /**
     * The file path (relative to repository root)
     * Example: "src/main/java/com/example/User.java"
     */
    private String path;

    /**
     * The old path (only for RENAME or COPY)
     * Example: "src/main/java/com/example/OldUser.java"
     */
    private String oldPath;

    /**
     * Type of change
     */
    private ChangeType changeType;

    /**
     * Number of lines added in this change
     */
    private int linesAdded;

    /**
     * Number of lines deleted in this change
     */
    private int linesDeleted;


    // ==================== CONSTRUCTORS ====================

    /**
     * Default constructor
     */
    public FileChangeInfo() {
    }

    /**
     * Constructor with path and change type
     */
    public FileChangeInfo(String path, ChangeType changeType) {
        this.path = path;
        this.changeType = changeType;
    }


    // ==================== GETTERS & SETTERS ====================

    public String getPath() {
        return path;
    }

    public FileChangeInfo setPath(String path) {
        this.path = path;
        return this;
    }

    public String getOldPath() {
        return oldPath;
    }

    public FileChangeInfo setOldPath(String oldPath) {
        this.oldPath = oldPath;
        return this;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public FileChangeInfo setChangeType(ChangeType changeType) {
        this.changeType = changeType;
        return this;
    }

    public int getLinesAdded() {
        return linesAdded;
    }

    public FileChangeInfo setLinesAdded(int linesAdded) {
        this.linesAdded = linesAdded;
        return this;
    }

    public int getLinesDeleted() {
        return linesDeleted;
    }

    public FileChangeInfo setLinesDeleted(int linesDeleted) {
        this.linesDeleted = linesDeleted;
        return this;
    }


    // ==================== UTILITY METHODS ====================

    /**
     * Check if this is a Java file
     */
    public boolean isJavaFile() {
        return path != null && path.endsWith(".java");
    }

    /**
     * Get just the filename without path
     * Example: "src/main/java/User.java" → "User.java"
     */
    public String getFileName() {
        if (path == null) return null;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }

    /**
     * Get total lines changed (added + deleted)
     */
    public int getTotalLinesChanged() {
        return linesAdded + linesDeleted;
    }

    /**
     * Get a symbol representing the change type
     */
    public String getChangeSymbol() {
        return switch (changeType) {
            case ADD -> "+";
            case MODIFY -> "~";
            case DELETE -> "-";
            case RENAME -> "→";
            case COPY -> "©";
            default -> "?";
        };
    }

    /**
     * Get a colored/emoji representation of change type
     */
    public String getChangeIcon() {
        return switch (changeType) {
            case ADD -> "🟢";     // Green for add
            case MODIFY -> "🟡";  // Yellow for modify
            case DELETE -> "🔴";  // Red for delete
            case RENAME -> "🔀";  // Shuffle for rename
            case COPY -> "📋";    // Clipboard for copy
            default -> "⚪";
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getChangeIcon()).append(" ");
        sb.append(changeType.name()).append(": ");

        if (changeType == ChangeType.RENAME && oldPath != null) {
            sb.append(oldPath).append(" → ").append(path);
        } else {
            sb.append(path);
        }

        if (linesAdded > 0 || linesDeleted > 0) {
            sb.append(" (+").append(linesAdded).append("/-").append(linesDeleted).append(")");
        }

        return sb.toString();
    }
}