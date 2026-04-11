# 🧠 Project: Mini Git (Java CLI)

> A simplified, functionally accurate version of **Git**.  
> *Note: This is NOT a toy "copy-paste" backup tool. This is a real version control system.*

## 📋 Table of Contents
1. [Core Concept (Crucial)](#-core-concept-if-you-dont-get-this--stop)
2. [Mandatory Features](#-what-you-must-build)
3. [Required Architecture](#-required-architecture)
4. [Critical Engineering Points](#-critical-engineering-points)
5. [Example Internal Flow](#-example-flow)
6. [Failure Patterns](#-where-you-will-fail)
7. [Difficulty Matrix](#-difficulty-reality)
8. [Project Value](#-why-this-is-good-for-you)
9. [Next Steps](#-next-steps)

---

## 🎯 Core Concept (If you don't get this → stop)

To build this correctly, you must unlearn how you *think* Git works.

Git is **NOT**:
* A tool that "copies files into versioned folders."
* A tool that stores diffs (in its core object model).

Git **IS**:
* A **Content-Addressable File System**.
* A system where *everything* (files, directories, commits) is identified by a **Cryptographic Hash** of its contents.

---

## 🔥 What You Must Build

### 1. Repository Initialization
Command: `mygit init`
Creates the hidden directory structure required to track history:
```text
.mygit/
 ├── objects/   # Where hashed files/trees/commits live
 ├── refs/      # Where branch pointers live
 └── HEAD       # Pointer to the current active branch/commit
```

### 2. Blob (File Storage)
Command: `mygit add <file>`
When a file is added, you must:
1. Read the raw file content.
2. Hash it (using SHA-1).
3. Store the content in `.mygit/objects/<hash>`.
👉 **Deduplication:** Because the hash is based on *content*, identical files (even with different names) generate the exact same hash and are stored only once.

### 3. Index (Staging Area)
You must maintain a staging area file: `.mygit/index`.
This maps the user's file paths to their newly generated blob hashes:
```text
src/main.java → <blob_hash>
README.md     → <blob_hash>
```
👉 *This is what separates a real Git clone from a script that just zips everything.*

### 4. Tree Object (Directory Structure)
Directories must be represented as `Tree` objects. If you have a folder `src/` containing `main.java`, the tree object looks like:
```text
tree   <tree_hash>    src
blob   <blob_hash>    main.java
```
*Note: This is where the project complexity spikes.*

### 5. Commit Object
Command: `mygit commit -m "message"`
A commit takes the current Staging Area (Index), generates the necessary Tree objects, and creates a Commit object containing:
```json
{
  "tree": "<root_tree_hash>",
  "parent": "<previous_commit_hash>",
  "message": "User commit message",
  "timestamp": "1698765432"
}
```
This text is then hashed and stored in `objects/`.

### 6. Log
Command: `mygit log`
Traverse the commit history backwards:
`HEAD → current_commit → parent_commit → grandparent_commit ...`

### 7. Checkout
Command: `mygit checkout <commit_hash>`
You must:
1. Read the Tree object attached to the requested commit.
2. Recursively restore files into the working directory from the hashed blobs in `objects/`.

---

## 🧱 Required Architecture

A messy architecture will make the Tree and Commit logic impossible to debug. Stick to this separation of concerns:

```text
mygit/
 ├── cli/
 │    └── CommandDispatcher.java    # Parses user CLI input
 ├── commands/
 │    ├── InitCommand.java
 │    ├── AddCommand.java
 │    ├── CommitCommand.java
 │    └── LogCommand.java
 ├── core/
 │    ├── Repository.java           # Manages .mygit/ state
 │    ├── ObjectStore.java          # Handles reading/writing to objects/
 │    └── Index.java                # Manages the staging area
 ├── objects/
 │    ├── GitObject.java            # Interface/Abstract class
 │    ├── Blob.java                 # File data
 │    ├── Tree.java                 # Directory mapping
 │    └── Commit.java               # Snapshot metadata
 ├── utils/
 │    └── HashUtils.java            # SHA-1 generation
```

---

## ⚠️ Critical Engineering Points

1. **Hashing (Non-negotiable):** 
   You must use Java's built-in cryptography: `MessageDigest.getInstance("SHA-1")`. If you skip hashing, your project is fake.
2. **Immutability:** 
   Objects in the `.mygit/objects` folder must **NEVER** change. If a file is modified, it gets a brand new hash and a brand new blob.
3. **Storage Format:** 
   Do not just dump raw text into the object files. Prepend the type and size, exactly like real Git: 
   `type: blob\ncontent: ...`
4. **Separation of Concerns:** 
   If your `main` method looks like a giant `if (cmd.equals("add")) { ... } else if ...`, your design is weak. Use the Command pattern.

---

## 🧪 Example Flow

Let's look at what happens internally during a standard workflow:

```bash
mygit init
mygit add file.txt
mygit commit -m "first commit"
```

**Under the hood:**
1. `file.txt` content is read → generates **Hash A** (Blob).
2. The root directory structure is mapped → generates **Hash B** (Tree).
3. The commit metadata is created (pointing to Hash B) → generates **Hash C** (Commit).
4. `HEAD` is updated to point to **Hash C**.

---

## 💀 Where You Will Fail

* ❌ **No Index:** If you skip the staging area, your commits mean nothing.
* ❌ **No Tree:** If you don't implement Trees, you can't support nested directories.
* ❌ **No Hashing:** If you use sequential IDs (1, 2, 3), it's not Git.
* ❌ **Mutable Objects:** Modifying existing history will break the graph.

---

## 🧠 Difficulty Reality

| Component | Difficulty Level |
| :--- | :--- |
| CLI / Dispatcher | 🟢 Easy |
| Hashing / Blobs | 🟢 Easy |
| Index (Staging) | 🟡 Medium |
| Commit Graph | 🟡 Medium |
| Tree Objects | 🔴 Hard |
| Checkout (Restoring) | 🔴 Hard |

---

## 🧠 Why This Is Good For You

If you want "simple," this is the wrong project. Real Git concepts are complex. But building this will:
* Force you to practice **System Design** thinking.
* Teach you about **Storage and Immutability**.
* Give you a massive advantage in understanding **Backend and Distributed Systems**.
* Serve as a **Top-Tier Portfolio Project** (stronger than 90% of standard CRUD apps).

*(Context: Combining **Minishell** (OS/Process mastery) with **Mini Git** (Storage/Versioning mastery) creates an incredibly strong system-engineering profile, perfectly suited for building microservices or custom code runners).*
