package com.java.minigit.objects;

public final class CommitObject {
    private CommitObject() {
    }

    public static String content(String treeHash, String parentHash, String author, long timestampEpochSeconds, String message) {
        StringBuilder builder = new StringBuilder();
        builder.append("tree ").append(treeHash).append("\n");
        if (parentHash != null && !parentHash.isBlank()) {
            builder.append("parent ").append(parentHash).append("\n");
        }
        builder.append("author ").append(author).append("\n");
        builder.append("timestamp ").append(timestampEpochSeconds).append("\n");
        builder.append("\n");
        builder.append(message).append("\n");
        return builder.toString();
    }
}
