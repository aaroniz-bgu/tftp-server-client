package bgu.spl.net.impl.tftp;

public class DisplayMessage {
    /**
     * Used to print messages to the CLI without having messed up messages across threads.
     * @param msg The message to print.
     */
    public static synchronized void print(String msg) {
        System.out.println(msg);
    }
}
