package com.java.minigit.objects;

public final class TreeEntry {
	private final String type;
	private final String hash;
	private final String name;

	public TreeEntry(String type, String hash, String name) {
		this.type = type;
		this.hash = hash;
		this.name = name;
	}

	public String type() {
		return type;
	}

	public String hash() {
		return hash;
	}

	public String name() {
		return name;
	}
}
