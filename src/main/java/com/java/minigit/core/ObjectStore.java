package com.java.minigit.core;

import com.java.minigit.utils.HashUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ObjectStore {
    private final Path objectsRoot;

    public ObjectStore(Path repositoryRoot) {
        this.objectsRoot = repositoryRoot.resolve(".mygit").resolve("objects");
    }

    public String save(String content, String type) {
        return save(content.getBytes(StandardCharsets.UTF_8), type);
    }

    public String save(byte[] content, String type) {
        validateType(type);

        byte[] header = (type + " " + content.length + "\0").getBytes(StandardCharsets.UTF_8);
        byte[] objectBytes = new byte[header.length + content.length];
        System.arraycopy(header, 0, objectBytes, 0, header.length);
        System.arraycopy(content, 0, objectBytes, header.length, content.length);

        String hash = HashUtils.sha1Hex(objectBytes);
        Path objectPath = objectPath(hash);

        try {
            Files.createDirectories(objectPath.getParent());
            if (!Files.exists(objectPath)) {
                Files.write(objectPath, objectBytes);
            }
            return hash;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save object " + hash, exception);
        }
    }

    public StoredObject read(String hash) {
        validateHash(hash);
        Path objectPath = objectPath(hash);

        try {
            byte[] objectBytes = Files.readAllBytes(objectPath);
            int separatorIndex = indexOfByte(objectBytes, (byte) 0);
            if (separatorIndex <= 0) {
                throw new IllegalStateException("Corrupted object: missing header separator");
            }

            String header = new String(objectBytes, 0, separatorIndex, StandardCharsets.UTF_8);
            int firstSpace = header.indexOf(' ');
            if (firstSpace <= 0 || firstSpace == header.length() - 1) {
                throw new IllegalStateException("Corrupted object header: " + header);
            }

            String type = header.substring(0, firstSpace);
            int size = Integer.parseInt(header.substring(firstSpace + 1));
            int contentStart = separatorIndex + 1;
            int contentLength = objectBytes.length - contentStart;
            if (contentLength != size) {
                throw new IllegalStateException("Corrupted object content size for hash " + hash);
            }

            byte[] content = new byte[contentLength];
            System.arraycopy(objectBytes, contentStart, content, 0, contentLength);
            return new StoredObject(hash, type, content);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read object " + hash, exception);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Corrupted object header: invalid size", exception);
        }
    }

    public String resolveHash(String hashOrPrefix) {
        String prefix = validateAndNormalizeHashPrefix(hashOrPrefix);
        if (prefix.length() == 40) {
            Path fullPath = objectPath(prefix);
            if (Files.exists(fullPath)) {
                return prefix;
            }
            throw new IllegalArgumentException("Unknown object hash: " + hashOrPrefix);
        }

        String directoryPrefix = prefix.substring(0, 2);
        String filePrefix = prefix.length() > 2 ? prefix.substring(2) : "";
        Path directory = objectsRoot.resolve(directoryPrefix);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Unknown object hash prefix: " + hashOrPrefix);
        }

        List<String> matches = new ArrayList<>();
        try {
            Files.list(directory)
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith(filePrefix))
                    .forEach(name -> matches.add(directoryPrefix + name));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to resolve hash prefix: " + hashOrPrefix, exception);
        }

        if (matches.isEmpty()) {
            throw new IllegalArgumentException("Unknown object hash prefix: " + hashOrPrefix);
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Ambiguous object hash prefix: " + hashOrPrefix);
        }
        return matches.get(0);
    }

    private Path objectPath(String hash) {
        return objectsRoot.resolve(hash.substring(0, 2)).resolve(hash.substring(2));
    }

    private void validateType(String type) {
        if (type == null || type.isBlank() || type.contains(" ")) {
            throw new IllegalArgumentException("Object type must be non-empty and contain no spaces");
        }
    }

    private String validateAndNormalizeHashPrefix(String hashOrPrefix) {
        if (hashOrPrefix == null) {
            throw new IllegalArgumentException("Hash prefix must not be null");
        }
        String normalized = hashOrPrefix.trim().toLowerCase();
        if (!normalized.matches("[0-9a-f]{2,40}")) {
            throw new IllegalArgumentException("Invalid SHA-1 hash or prefix: " + hashOrPrefix);
        }
        return normalized;
    }

    private void validateHash(String hash) {
        if (hash == null || hash.length() != 40 || !hash.matches("[0-9a-f]{40}")) {
            throw new IllegalArgumentException("Invalid SHA-1 hash: " + hash);
        }
    }

    private int indexOfByte(byte[] bytes, byte value) {
        for (int index = 0; index < bytes.length; index++) {
            if (bytes[index] == value) {
                return index;
            }
        }
        return -1;
    }

    public static final class StoredObject {
        private final String hash;
        private final String type;
        private final byte[] content;

        public StoredObject(String hash, String type, byte[] content) {
            this.hash = hash;
            this.type = type;
            this.content = content;
        }

        public String hash() {
            return hash;
        }

        public String type() {
            return type;
        }

        public byte[] content() {
            return content;
        }

        public String contentAsString() {
            return new String(content, StandardCharsets.UTF_8);
        }
    }
}
