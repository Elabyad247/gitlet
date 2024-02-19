package gitlet;

class GitletException extends RuntimeException {
    GitletException() {
        super();
    }
    GitletException(String msg) {
        super(msg);
    }
}
