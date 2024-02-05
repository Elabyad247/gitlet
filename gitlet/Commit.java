package gitlet;

// TODO: any imports you need here

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
     * TODO: add instance variables here.
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

    public void save_commit() throws IOException {
        String objContent = this.toString();
        File tempfile = join(Repository.COMMITS_DIR, sha1(objContent));
        tempfile.createNewFile();
        writeObject(tempfile, this);
    }
    /* TODO: fill in the rest of this class. */
}
