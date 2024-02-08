package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static gitlet.Utils.*;

public class Branch implements Serializable {
    private String name;
    private static String lastCommit;

    public Branch(String _name, String _lastCommit) {
        name = _name;
        lastCommit = _lastCommit;
    }

    public static void setLastCommit(String newCommit) {
        lastCommit = newCommit;
    }

    public String getUID() {
        String objContent = this.toString();
        return sha1(objContent);
    }

    public void saveBranch() throws IOException {
        File bracnhFile = join(Repository.BRANCH_DIR, getUID());
        bracnhFile.createNewFile();
        writeObject(bracnhFile, this);
    }

    public String getLastCommit() {
        return lastCommit;
    }
}
