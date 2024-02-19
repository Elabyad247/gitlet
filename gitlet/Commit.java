package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Formatter;
import java.util.Map;
import java.util.TreeMap;
import static gitlet.Utils.*;

public class Commit implements Serializable {
    private String message;
    private Date date;
    private String parent;
    private String secParent;
    private TreeMap<String, String> blobs = new TreeMap<>();

    public Commit() {
        message = "initial commit";
        date = new Date(0);
        parent = null;
        secParent = null;
    }

    public Commit(String message, String parent) {
        this.message = message;
        this.parent = parent;
        this.secParent = null;
        date = new Date();
    }

    public Commit(String message, String firstParent, String secondParent) {
        this.message = message;
        this.parent = firstParent;
        this.secParent = secondParent;
        this.date = new Date();
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return message + date.toString() + parent + secParent + blobs.toString();
    }

    public String getUID() {
        return sha1(this.toString());
    }

    public void saveCommit() {
        File commitFile = join(Repository.COMMITS_DIR, getUID());
        writeObject(commitFile, this);
    }

    public void addBlobs(TreeMap<String, String> oldAddMap) {
        blobs.putAll(oldAddMap);
    }

    public void removeBlobs(TreeMap<String, String> oldRemoveMap) {
        for (Map.Entry<String, String> set : oldRemoveMap.entrySet()) {
            blobs.remove(set.getKey());
        }
    }

    public TreeMap<String, String> getBlobs() {
        return blobs;
    }

    public String getBlobID(String fileName) {
        return blobs.get(fileName);
    }

    public String getParent() {
        return parent;
    }

    public String getMessage() {
        return message;
    }

    public String getSecParent() {
        return secParent;
    }

    public void displayLog() {
        System.out.println("===");
        System.out.println("commit " + getUID());
        Date time = getDate();
        Formatter f = new Formatter().format("%1$ta %1$tb %1$td %1$tT %1$tY %1$tz", time);
        String fDate = f.toString();
        System.out.println("Date: " + fDate);
        System.out.println(getMessage());
        System.out.println();
    }
}
