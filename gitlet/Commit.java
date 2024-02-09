package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Utils.*;

/**
 * Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author Elabyad & Znno
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private String message;
    private Date date;
    private String branch;
    private String parent;
    private HashMap<String, String> blobs = new HashMap<>();

    public Commit() {
        message = "Initial Commit";
        date = new Date(0);
        branch = Repository.CURRENT_BRANCH;
        parent = null;
    }
    public Commit(String _message,String _parent) {
        message=_message;
        parent=_parent;
        date=new Date();
    }

    @Override
    public String toString() {
        System.out.println(blobs.toString());
        return message + date + branch + parent + blobs.toString();
    }

    public String getUID() {
        return sha1(toString());
    }

    public void saveCommit() throws IOException {
        File commitFile = join(Repository.COMMITS_DIR, getUID());
        commitFile.createNewFile();
        writeObject(commitFile, this);
    }

    public void addBlobs(HashMap<String, String> oldAddMap) {
        blobs.putAll(oldAddMap);
    }

    public void removeBlobs(HashMap<String, String> oldRemoveMap) {
        for (Map.Entry<String, String> set : oldRemoveMap.entrySet()) {
            blobs.remove(set.getKey());
        }
    }

    public HashMap<String, String> getBlobs() {
        return blobs;
    }
    public String getBlob(String fileName) {
        return blobs.get(fileName);
    }

}
