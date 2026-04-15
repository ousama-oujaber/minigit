package com.java.minigit.objects;

public final class CommitData {
	private final String treeHash;
	private final String parentHash;
	private final String author;
	private final long timestamp;
	private final String message;

	public CommitData(String treeHash, String parentHash, String author, long timestamp, String message) {
		this.treeHash = treeHash;
		this.parentHash = parentHash;
		this.author = author;
		this.timestamp = timestamp;
		this.message = message;
	}

	public String treeHash() {
		return treeHash;
	}

	public String parentHash() {
		return parentHash;
	}

	public String author() {
		return author;
	}

	public long timestamp() {
		return timestamp;
	}

	public String message() {
		return message;
	}
}
