package gitlet;

/**
 * @author Saifaldin Elabyad & Zeyad Tabour
 */
public class Main {
    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                checkNumberOfArgs(1, args.length);
                Repository.init();
                break;
            case "add":
                checkInitializedGitlet();
                checkNumberOfArgs(2, args.length);
                Repository.add(args[1]);
                break;
            case "rm":
                checkInitializedGitlet();
                checkNumberOfArgs(2, args.length);
                Repository.rm(args[1]);
                break;
            case "commit":
                checkInitializedGitlet();
                checkNumberOfArgs(2, args.length);
                if (args[1].isEmpty()) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                Repository.commit(args[1],null);
                break;
            case "log":
                checkInitializedGitlet();
                checkNumberOfArgs(1, args.length);
                Repository.log();
                break;
            case "global-log":
                checkInitializedGitlet();
                checkNumberOfArgs(1, args.length);
                Repository.globalLog();
                break;
            case "find":
                checkInitializedGitlet();
                checkNumberOfArgs(2, args.length);
                Repository.find(args[1]);
                break;
            case "checkout":
                checkInitializedGitlet();
                if (args.length != 2 && args.length != 4 && args.length != 3) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (args.length == 3 && !args[1].equals("--")) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (args.length == 4 && !args[2].equals("--")) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (args.length == 3) {
                    Repository.checkoutFile(args[2]);
                } else if (args.length == 4) {
                    Repository.checkoutCommit(args[1], args[3]);
                } else if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                }
                break;
            case "status":
                checkInitializedGitlet();
                checkNumberOfArgs(1, args.length);
                Repository.status();
                break;
            case "branch":
                checkInitializedGitlet();
                checkNumberOfArgs(2, args.length);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                checkInitializedGitlet();
                checkNumberOfArgs(2, args.length);
                Repository.rmbranch(args[1]);
                break;
            case "reset":
                checkInitializedGitlet();
                checkNumberOfArgs(2, args.length);
                Repository.reset(args[1]);
                break;
            case "merge":
                checkInitializedGitlet();
                checkNumberOfArgs(2, args.length);
                Repository.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
                break;
        }
    }
    private static void checkNumberOfArgs(int correct, int provided) {
        if (correct != provided) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
    private static void checkInitializedGitlet() {
        if (!Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}
