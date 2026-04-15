package com.java.minigit.objects;

public final class CommitParser {
    private CommitParser() {
    }

    public static CommitData parse(String content) {
        String[] lines = content.split("\n", -1);
        String treeHash = null;
        String parentHash = null;
        String author = "unknown";
        long timestamp = 0L;

        int index = 0;
        while (index < lines.length) {
            String line = lines[index];
            if (line.isEmpty()) {
                index++;
                break;
            }

            if (line.startsWith("tree ")) {
                treeHash = line.substring("tree ".length());
            } else if (line.startsWith("parent ")) {
                parentHash = line.substring("parent ".length());
            } else if (line.startsWith("author ")) {
                author = line.substring("author ".length());
            } else if (line.startsWith("timestamp ")) {
                timestamp = Long.parseLong(line.substring("timestamp ".length()));
            }
            index++;
        }

        if (treeHash == null || treeHash.isBlank()) {
            throw new IllegalStateException("Corrupted commit object: missing tree hash");
        }

        StringBuilder messageBuilder = new StringBuilder();
        while (index < lines.length) {
            messageBuilder.append(lines[index]);
            if (index < lines.length - 1) {
                messageBuilder.append('\n');
            }
            index++;
        }

        String message = messageBuilder.toString();
        if (message.endsWith("\n")) {
            message = message.substring(0, message.length() - 1);
        }

        return new CommitData(treeHash, parentHash, author, timestamp, message);
    }
}
