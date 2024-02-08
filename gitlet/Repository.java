package gitlet;

import edu.princeton.cs.algs4.ST;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author Elabyad & Znno
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    public static final File STAGING_DIR = join(GITLET_DIR, "staging");
    public static final File ADD_STAGE_FILE = join(STAGING_DIR, "add");
    public static final File REMOVE_STAGE_FILE = join(STAGING_DIR, "remove");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File BRANCH_DIR = join(GITLET_DIR, "branches");
    public static final File HEAD = join(GITLET_DIR, "head");
    public static String CURRENT_BRANCH;

    public static void init() throws IOException {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        COMMITS_DIR.mkdir();
        STAGING_DIR.mkdir();
        HEAD.createNewFile();
        ADD_STAGE_FILE.createNewFile();
        writeObject(ADD_STAGE_FILE, new HashMap<String, String>());
        REMOVE_STAGE_FILE.createNewFile();
        writeObject(REMOVE_STAGE_FILE, new HashMap<String, String>());
        BLOBS_DIR.mkdir();
        BRANCH_DIR.mkdir();
        Branch master = new Branch("master", null);
        Commit intialCommit = new Commit();
        Branch.setLastCommit(intialCommit.getUID());
        intialCommit.saveCommit();
        writeContents(HEAD, intialCommit.getUID());
        master.saveBranch();
        CURRENT_BRANCH = master.getUID();
    }

    public static void add(String name) throws IOException {
        Blob newBlob = new Blob(name);
        String sha = readContentsAsString(HEAD);
        File file = join(COMMITS_DIR, sha);
        Commit currentCommit = readObject(file, Commit.class);
        HashMap<String, String> addMap = readObject(ADD_STAGE_FILE, HashMap.class);
        if (currentCommit.getBlob(name) == null) {
            if (addMap.get(name) != null) {
                File temp = join(BLOBS_DIR, addMap.get(name));
                temp.delete();
            }
            addMap.put(name, newBlob.getUID());
            newBlob.saveBlob();
        } else {
            if (currentCommit.getBlob(name).equals(newBlob.getUID())) {
                addMap.remove(name);
            } else {
                if (addMap.get(name) != null) {
                    File temp = join(BLOBS_DIR, addMap.get(name));
                    temp.delete();
                }
                addMap.put(name, newBlob.getUID());
                newBlob.saveBlob();
            }
        }
        writeObject(ADD_STAGE_FILE, addMap);
    }
}
