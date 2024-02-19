package gitlet;

import java.io.File;
import java.util.*;
import java.util.List;
import static gitlet.Utils.*;

/**
 * @author Saifaldin Elabyad & Zeyad Tabour
 */
public class Repository {
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    public static final File STAGING_DIR = join(GITLET_DIR, "staging");
    public static final File ADD_STAGE_FILE = join(STAGING_DIR, "add");
    public static final File REMOVE_STAGE_FILE = join(STAGING_DIR, "remove");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File BRANCH_DIR = join(GITLET_DIR, "branches");
    public static final File HEAD = join(GITLET_DIR, "head");
    public static final File BRANCHES = join(GITLET_DIR, "branchesMap");
    public static final File CURRENT_BRANCH = join(GITLET_DIR, "current_branch");

    public static void init() {
        if (GITLET_DIR.exists()) {
            String msg = "A Gitlet version-control system already exists in the current directory.";
            System.out.println(msg);
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        COMMITS_DIR.mkdir();
        STAGING_DIR.mkdir();
        BLOBS_DIR.mkdir();
        BRANCH_DIR.mkdir();
        Commit intialCommit = new Commit();
        intialCommit.saveCommit();
        Branch master = new Branch("master", null);
        master.setBranchHead(intialCommit.getUID());
        master.saveBranch();
        TreeMap<String, String> branchesMap = new TreeMap<>();
        branchesMap.put("master", master.getUID());
        writeObject(BRANCHES, branchesMap);
        writeObject(ADD_STAGE_FILE, new TreeMap<String, String>());
        writeObject(REMOVE_STAGE_FILE, new TreeMap<String, String>());
        writeContents(HEAD, intialCommit.getUID());
        writeContents(CURRENT_BRANCH, "master");
    }

    private static Commit getCurrentCommit() {
        String commitID = readContentsAsString(HEAD);
        File commitFile = join(COMMITS_DIR, commitID);
        return readObject(commitFile, Commit.class);
    }

    public static void add(String name) {
        File checkFile = join(CWD, name);
        if (!checkFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Blob newBlob = new Blob(name);
        Commit currentCommit = getCurrentCommit();
        TreeMap<String, String> addMap = readObject(ADD_STAGE_FILE, TreeMap.class);
        if (currentCommit.getBlobID(name) == null) {
            addMap.put(name, newBlob.getUID());
            newBlob.saveBlob();
        } else {
            if (currentCommit.getBlobID(name).equals(newBlob.getUID())) {
                addMap.remove(name);
            } else {
                addMap.put(name, newBlob.getUID());
                newBlob.saveBlob();
            }
        }
        writeObject(ADD_STAGE_FILE, addMap);
        TreeMap<String, String> removeMap = readObject(REMOVE_STAGE_FILE, TreeMap.class);
        if (removeMap.get(name) != null) {
            removeMap.remove(name);
            writeObject(REMOVE_STAGE_FILE, removeMap);
        }
    }

    public static void rm(String name) {
        TreeMap<String, String> addMap = readObject(ADD_STAGE_FILE, TreeMap.class);
        Commit currentCommit = getCurrentCommit();
        if (addMap.get(name) == null && currentCommit.getBlobID(name) == null) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (addMap.get(name) != null) {
            addMap.remove(name);
            writeObject(ADD_STAGE_FILE, addMap);
        }
        if (currentCommit.getBlobID(name) != null) {
            TreeMap<String, String> removeMap = readObject(REMOVE_STAGE_FILE, TreeMap.class);
            removeMap.put(name, currentCommit.getBlobID(name));
            File temp = join(CWD, name);
            temp.delete();
            writeObject(REMOVE_STAGE_FILE, removeMap);
        }
    }

    public static void commit(String message, String secondParent) {
        TreeMap<String, String> addMap = readObject(ADD_STAGE_FILE, TreeMap.class);
        TreeMap<String, String> removeMap = readObject(REMOVE_STAGE_FILE, TreeMap.class);
        if (addMap.isEmpty() && removeMap.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Commit currentCommit = getCurrentCommit();
        Commit newCommit = new Commit(message, currentCommit.getUID(), secondParent);
        newCommit.addBlobs(currentCommit.getBlobs());
        newCommit.addBlobs(addMap);
        newCommit.removeBlobs(removeMap);
        newCommit.saveCommit();
        addMap.clear();
        removeMap.clear();
        writeObject(ADD_STAGE_FILE, addMap);
        writeObject(REMOVE_STAGE_FILE, removeMap);
        writeContents(HEAD, newCommit.getUID());
        String currentBranchName = readContentsAsString(CURRENT_BRANCH);
        File branchFile = join(BRANCH_DIR, currentBranchName);
        Branch currentBranch = readObject(branchFile, Branch.class);
        currentBranch.setBranchHead(newCommit.getUID());
        currentBranch.saveBranch();
        TreeMap<String, String> branchesMap = readObject(BRANCHES, TreeMap.class);
        branchesMap.put(currentBranch.getName(), currentBranch.getUID());
        writeObject(BRANCHES, branchesMap);
    }

    public static void log() {
        Commit currentCommit = getCurrentCommit();
        while (currentCommit != null) {
            currentCommit.displayLog();
            if (currentCommit.getParent() == null) {
                break;
            }
            File nextCommitFile = join(COMMITS_DIR, currentCommit.getParent());
            currentCommit = readObject(nextCommitFile, Commit.class);
        }
    }

    public static void globalLog() {
        List<String> files = plainFilenamesIn(COMMITS_DIR);
        for (String commitID : files) {
            File file = join(COMMITS_DIR, commitID);
            Commit currentCommit = readObject(file, Commit.class);
            currentCommit.displayLog();
        }
    }

    public static void find(String message) {
        List<String> files = plainFilenamesIn(COMMITS_DIR);
        boolean found = false;
        for (String commitID : files) {
            File file = join(COMMITS_DIR, commitID);
            Commit wantedCommit = readObject(file, Commit.class);
            if (!(wantedCommit.getMessage().equals(message))) {
                continue;
            }
            System.out.println(wantedCommit.getUID());
            found = true;
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }

    }

    private static Blob getBlob(String blobID) {
        File blobFile = join(BLOBS_DIR, blobID);
        return readObject(blobFile, Blob.class);
    }

    public static void checkoutFile(String fileName) {
        Commit currentCommit = getCurrentCommit();
        String currentCommitBlobID = currentCommit.getBlobID(fileName);
        if (currentCommitBlobID == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Blob wantedBlob = getBlob(currentCommitBlobID);
        File cwdFile = join(CWD, fileName);
        writeContents(cwdFile, wantedBlob.getContent());
    }

    public static void checkoutCommit(String commitID, String fileName) {
        List<String> possibleCommits = plainFilenamesIn(COMMITS_DIR);
        for (String id: possibleCommits) {
            if (id.contains(commitID)) {
                commitID = id;
                break;
            }
        }
        File commitFile = join(COMMITS_DIR, commitID);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit wantedCommit = readObject(commitFile, Commit.class);
        String wantedCommitBlobID = wantedCommit.getBlobID(fileName);
        if (wantedCommitBlobID == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Blob wantedBlob = getBlob(wantedCommitBlobID);
        File cwdFile = join(CWD, fileName);
        writeContents(cwdFile, wantedBlob.getContent());
    }

    private static Branch getCurrentBranch() {
        String currentBranchName = readContentsAsString(CURRENT_BRANCH);
        File branchFile = join(BRANCH_DIR, currentBranchName);
        return readObject(branchFile, Branch.class);
    }

    public static void status() {
        System.out.println("=== Branches ===");
        TreeMap<String, String> branchesMap = readObject(BRANCHES, TreeMap.class);
        Branch currentBranch = getCurrentBranch();
        for (Map.Entry<String, String> set : branchesMap.entrySet()) {
            if (set.getKey().equals(currentBranch.getName())) {
                System.out.print("*");
            }
            System.out.println(set.getKey());
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        TreeMap<String, String> addMap = readObject(ADD_STAGE_FILE, TreeMap.class);
        for (Map.Entry<String, String> set : addMap.entrySet()) {
            System.out.println(set.getKey());
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        TreeMap<String, String> removeMap = readObject(REMOVE_STAGE_FILE, TreeMap.class);
        for (Map.Entry<String, String> set : removeMap.entrySet()) {
            System.out.println(set.getKey());
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        TreeMap<String, String> untrackedFiles = getUntrackedFiles();
        for (Map.Entry<String, String> set : untrackedFiles.entrySet()) {
            System.out.println(set.getKey());
        }
        System.out.println();
    }

    public static void branch(String branchName) {
        TreeMap<String, String> branchesMap = readObject(BRANCHES, TreeMap.class);
        if (branchesMap.get(branchName) != null) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        String currHead = readContentsAsString(HEAD);
        Branch newBranch = new Branch(branchName, currHead);
        newBranch.saveBranch();
        branchesMap.put(branchName, newBranch.getUID());
        writeObject(BRANCHES, branchesMap);
    }

    public static void checkoutBranch(String branchName) {
        TreeMap<String, String> branchesMap = readObject(BRANCHES, TreeMap.class);
        if (branchesMap.get(branchName) == null) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        Branch currentBranch = getCurrentBranch();
        if (branchName.equals(currentBranch.getName())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        File wantedBranchFile = join(BRANCH_DIR, branchName);
        Branch wantedBranch = readObject(wantedBranchFile, Branch.class);
        String wantedBranchHead = wantedBranch.getBranchHead();
        File wantedCommitFile = join(COMMITS_DIR, wantedBranchHead);
        Commit wantedCommit = readObject(wantedCommitFile, Commit.class);
        TreeMap<String, String> addMap = readObject(ADD_STAGE_FILE, TreeMap.class);
        TreeMap<String, String> removeMap = readObject(REMOVE_STAGE_FILE, TreeMap.class);
        if (!checkUntrackedFiles(wantedCommit)) {
            String m1 = "There is an untracked file in the way;";
            String m2 = " delete it, or add and commit it first.";
            System.out.println(m1 + m2);
            System.exit(0);
        }
        for (Map.Entry<String, String> set : wantedCommit.getBlobs().entrySet()) {
            Blob wantedBlob = getBlob(set.getValue());
            String blobContent = wantedBlob.getContent();
            File overwriteFile = join(CWD, set.getKey());
            writeContents(overwriteFile, blobContent);
        }
        List<String> newFiles = plainFilenamesIn(CWD);
        for (String fileName : newFiles) {
            if (wantedCommit.getBlobs().get(fileName) == null) {
                File deletedFile = join(CWD, fileName);
                deletedFile.delete();
            }
        }
        addMap.clear();
        removeMap.clear();
        branchesMap.put(wantedBranch.getName(), wantedBranch.getUID());
        writeContents(CURRENT_BRANCH, branchName);
        writeContents(HEAD, wantedCommit.getUID());
        writeObject(BRANCHES, branchesMap);
        writeObject(ADD_STAGE_FILE, addMap);
        writeObject(REMOVE_STAGE_FILE, removeMap);
    }

    public static void rmbranch(String branchName) {
        TreeMap<String, String> branchesMap = readObject(BRANCHES, TreeMap.class);
        if (branchesMap.get(branchName) == null) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        Branch currentBranch = getCurrentBranch();
        if (currentBranch.getName().equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        File wantedBranchFile = join(BRANCH_DIR, branchName);
        wantedBranchFile.delete();
        branchesMap.remove(branchName);
        writeObject(BRANCHES, branchesMap);
    }

    public static void reset(String commitID) {
        File commitFile = join(COMMITS_DIR, commitID);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Branch currentBranch = getCurrentBranch();
        Commit wantedCommit = readObject(commitFile, Commit.class);
        TreeMap<String, String> addMap = readObject(ADD_STAGE_FILE, TreeMap.class);
        TreeMap<String, String> removeMap = readObject(REMOVE_STAGE_FILE, TreeMap.class);
        if (!checkUntrackedFiles(wantedCommit)) {
            String m1 = "There is an untracked file in the way;";
            String m2 = " delete it, or add and commit it first.";
            System.out.println(m1 + m2);
            System.exit(0);
        }
        for (Map.Entry<String, String> set : wantedCommit.getBlobs().entrySet()) {
            Blob wantedBlob = getBlob(set.getValue());
            String content = wantedBlob.getContent();
            File overwrite = join(CWD, set.getKey());
            writeContents(overwrite, content);
        }
        List<String> newFiles = plainFilenamesIn(CWD);
        for (String fileName : newFiles) {
            if (wantedCommit.getBlobID(fileName) == null) {
                File deletedFile = join(CWD, fileName);
                deletedFile.delete();
            }
        }
        currentBranch.setBranchHead(wantedCommit.getUID());
        currentBranch.saveBranch();
        TreeMap<String, String> branchesMap = readObject(BRANCHES, TreeMap.class);
        branchesMap.put(currentBranch.getName(), currentBranch.getUID());
        addMap.clear();
        removeMap.clear();
        writeObject(ADD_STAGE_FILE, addMap);
        writeObject(REMOVE_STAGE_FILE, removeMap);
        writeObject(BRANCHES, branchesMap);
        writeContents(CURRENT_BRANCH, currentBranch.getName());
        writeContents(HEAD, wantedCommit.getUID());
    }

    private static String getSplitPoint(Commit wantedCommit) {
        TreeMap<String, Boolean> exist = new TreeMap<String, Boolean>();
        Queue<String> bfsList = new ArrayDeque<>();
        bfsList.add(getCurrentCommit().getUID());
        while (!bfsList.isEmpty()) {
            String node = bfsList.remove();
            exist.put(node, true);
            File commitFile = join(COMMITS_DIR, node);
            Commit tempCommit = readObject(commitFile, Commit.class);
            if (tempCommit.getParent() != null) {
                bfsList.add(tempCommit.getParent());
            }
            if (tempCommit.getSecParent() != null) {
                bfsList.add(tempCommit.getSecParent());
            }
        }
        bfsList.add(wantedCommit.getUID());
        while (!bfsList.isEmpty()) {
            String node = bfsList.remove();
            if (exist.get(node) != null) {
                return node;
            }
            File commitFile = join(COMMITS_DIR, node);
            Commit tempCommit = readObject(commitFile, Commit.class);
            if (tempCommit.getParent() != null) {
                bfsList.add(tempCommit.getParent());
            }
            if (tempCommit.getSecParent() != null) {
                bfsList.add(tempCommit.getSecParent());
            }
        }
        return null;
    }

    private static boolean checkConflict(String sP, String p1, String p2, String name) {
        boolean firstSecond = Objects.equals(p1, p2);
        boolean splitFirst = Objects.equals(sP, p1);
        boolean splitSecond = Objects.equals(sP, p2);
        if (!firstSecond && !splitFirst && !splitSecond) {
            String s = "<<<<<<< HEAD\n";
            if (p1 != null) {
                Blob blobObject = getBlob(p1);
                s += blobObject.getContent();
            }
            s += "=======\n";
            if (p2 != null) {
                Blob blobObject = getBlob(p2);
                s += blobObject.getContent();
            }
            s += ">>>>>>>\n";
            File conflictFile = join(CWD, name);
            writeContents(conflictFile, s);
            add(name);
            return true;
        }
        return false;
    }

    public static void merge(String branchName) {
        TreeMap<String, String> branchesMap = readObject(BRANCHES, TreeMap.class);
        if (branchesMap.get(branchName) == null) {
            showErrorMessage("A branch with that name does not exist.", null);
        }
        TreeMap<String, String> addMap = readObject(ADD_STAGE_FILE, TreeMap.class);
        TreeMap<String, String> removeMap = readObject(REMOVE_STAGE_FILE, TreeMap.class);
        if (!addMap.isEmpty() || !removeMap.isEmpty()) {
            showErrorMessage("You have uncommitted changes.", null);
        }
        Branch currentBranch = getCurrentBranch();
        if (currentBranch.getName().equals(branchName)) {
            showErrorMessage("Cannot merge a branch with itself.", null);
        }
        Branch wantedBranch = readObject(join(BRANCH_DIR, branchName), Branch.class);
        File wantedCommitFile = join(COMMITS_DIR, wantedBranch.getBranchHead());
        Commit wantedCommit = readObject(wantedCommitFile, Commit.class);
        Commit currentCommit = getCurrentCommit();
        if (!checkUntrackedFiles(wantedCommit)) {
            String m1 = "There is an untracked file in the way;";
            String m2 = " delete it, or add and commit it first.";
            showErrorMessage(m1, m2);
        }
        String splitPoint = getSplitPoint(wantedCommit);
        if (splitPoint.equals(wantedCommit.getUID())) {
            showErrorMessage("Given branch is an ancestor of the current branch.", null);
        }
        if (splitPoint.equals(currentCommit.getUID())) {
            checkoutBranch(branchName);
            showErrorMessage("Current branch fast-forwarded.", null);
        }
        Commit splitPointCommit = readObject(join(COMMITS_DIR, splitPoint), Commit.class);
        TreeMap<String, Boolean> all = new TreeMap<>();
        for (Map.Entry<String, String> set : wantedCommit.getBlobs().entrySet()) {
            all.put(set.getKey(), true);
        }
        for (Map.Entry<String, String> set : currentCommit.getBlobs().entrySet()) {
            all.put(set.getKey(), true);
        }
        for (Map.Entry<String, String> set : splitPointCommit.getBlobs().entrySet()) {
            all.put(set.getKey(), true);
        }
        boolean conflict = false;
        for (Map.Entry<String, Boolean> set : all.entrySet()) {
            String splitBlobID = splitPointCommit.getBlobID(set.getKey());
            String currentBlobID = currentCommit.getBlobID(set.getKey());
            String newBlobID = wantedCommit.getBlobID(set.getKey());
            if (checkConflict(splitBlobID, currentBlobID, newBlobID, set.getKey())) {
                conflict = true;
            }
            if (splitBlobID != null) {
                if (newBlobID != null) {
                    if (splitBlobID.equals(currentBlobID) && !splitBlobID.equals(newBlobID)) {
                        File writeOver = join(CWD, set.getKey());
                        Blob blobObject = getBlob(newBlobID);
                        writeContents(writeOver, blobObject.getContent());
                        add(set.getKey());
                    }
                } else {
                    if (splitBlobID.equals(currentBlobID)) {
                        rm(set.getKey());
                    }
                }
            } else {
                if (currentBlobID == null && newBlobID != null) {
                    File writeOver = join(CWD, set.getKey());
                    Blob blobObject = getBlob(newBlobID);
                    writeContents(writeOver, blobObject.getContent());
                    add(set.getKey());
                }
            }
        }
        String msg = "Merged " + branchName + " into " + currentBranch.getName() + ".";
        commit(msg, wantedCommit.getUID());
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private static TreeMap<String, String> getUntrackedFiles() {
        Commit currentCommit = getCurrentCommit();
        TreeMap<String, String> addMap = readObject(ADD_STAGE_FILE, TreeMap.class);
        TreeMap<String, String> removeMap = readObject(REMOVE_STAGE_FILE, TreeMap.class);
        TreeMap<String, String> untrackedFiles = new TreeMap<String, String>();
        List<String> files = plainFilenamesIn(CWD);
        for (String fileName : files) {
            if (removeMap.get(fileName) != null) {
                untrackedFiles.put(fileName, fileName);
            }
            if (addMap.get(fileName) == null && currentCommit.getBlobID(fileName) == null) {
                untrackedFiles.put(fileName, fileName);
            }
        }
        return untrackedFiles;
    }
    private static boolean checkUntrackedFiles(Commit wantedCommit) {
        TreeMap<String, String> untrackedFiles = getUntrackedFiles();
        for (Map.Entry<String, String> set : untrackedFiles.entrySet()) {
            if (wantedCommit.getBlobID(set.getValue()) != null) {
                return false;
            }
        }
        return true;
    }
    private static void showErrorMessage(String msg1, String msg2) {
        System.out.print(msg1);
        if (msg2 != null) {
            System.out.println(' ' + msg2);
        } else {
            System.out.println();
        }
        System.exit(0);
    }
}
