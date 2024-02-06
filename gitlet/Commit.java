package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

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

    public Commit() {
        message = "Initial Commit";
        date = new Date(0);
    }

    public String getUID() {
        String objContent = this.toString();
        return sha1(objContent);
    }

    public void saveCommit() throws IOException {
        File commitFile = join(Repository.COMMITS_DIR, getUID());
        commitFile.createNewFile();
        writeObject(commitFile, this);
    }

}
