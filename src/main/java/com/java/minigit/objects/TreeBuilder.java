package com.java.minigit.objects;

import com.java.minigit.core.ObjectStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TreeBuilder {
    private final ObjectStore objectStore;

    public TreeBuilder(ObjectStore objectStore) {
        this.objectStore = objectStore;
    }

    public String buildRootTree(Map<String, String> indexEntries) {
        Node root = new Node();
        for (Map.Entry<String, String> entry : indexEntries.entrySet()) {
            addPath(root, entry.getKey(), entry.getValue());
        }
        return writeTree(root);
    }

    private void addPath(Node root, String path, String blobHash) {
        String[] parts = path.split("/");
        Node current = root;
        for (int index = 0; index < parts.length - 1; index++) {
            String directory = parts[index];
            current = current.directories.computeIfAbsent(directory, key -> new Node());
        }

        String fileName = parts[parts.length - 1];
        current.blobs.put(fileName, blobHash);
    }

    private String writeTree(Node node) {
        List<String> lines = new ArrayList<>();

        for (Map.Entry<String, Node> directory : node.directories.entrySet()) {
            String childHash = writeTree(directory.getValue());
            lines.add("tree " + childHash + " " + directory.getKey());
        }

        for (Map.Entry<String, String> blob : node.blobs.entrySet()) {
            lines.add("blob " + blob.getValue() + " " + blob.getKey());
        }

        String content = String.join("\n", lines);
        if (!content.isEmpty()) {
            content += "\n";
        }
        return objectStore.save(content, "tree");
    }

    private static final class Node {
        private final Map<String, Node> directories = new TreeMap<>();
        private final Map<String, String> blobs = new TreeMap<>();
    }
}
