package gitlet;

import java.io.IOException;
import java.io.File;

import static gitlet.Utils.*;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author Elabyad & Znno
 */
public class Main {
    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                if (args.length > 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.init();
                break;
            case "add":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                File testfile = join(Repository.CWD, args[1]);
                if (!testfile.exists()) {
                    System.out.println("File does not exist.");
                    System.exit(0);
                }
                Repository.add(args[1]);
                break;

            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
                break;
        }
    }
}
