package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static gitlet.Utils.*;

public class Blob implements Serializable {
    private String content;

    public Blob(String fileName) {
        File file = join(Repository.CWD, fileName);
        content = readContentsAsString(file);
    }

    public String getUID() {
        return sha1(content);
    }

    public String getContent() {
        return content;
    }

    public void saveBlob() throws IOException {
        File blobFile = join(Repository.BLOBS_DIR, getUID());
        blobFile.createNewFile();
        writeObject(blobFile, this);
    }
}
