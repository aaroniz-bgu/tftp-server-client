package bgu.spl.net.impl.tftp.packets;

import static bgu.spl.net.impl.tftp.DisplayMessage.print;

/**
 * A factory class for creating packets according to the user input
 */
public class PacketFactory {
    /**
     * Takes the user input and creates a packet according to the command
     * @param userInput the user input
     * @return the packet created according to the command or null if the command is not recognized
     */
    public static AbstractPacket createPacket(String userInput) {
        // If the input is empty, return null
        if (userInput == null || userInput.isEmpty()) {
            return null;
        }
        // Find the first space in the string in order to get the operation type
        int endIndex = userInput.indexOf(" ");
        String endOfCommand = "";
        // If there is no space, the command is the entire string
        if (endIndex == -1) {
            endIndex = userInput.length();
        }
        else {
            endOfCommand = userInput.substring(endIndex + 1);
        }
        String command = userInput.substring(0, endIndex);
        // Create the packet according to the command
        // If the command is not recognized, return null
        switch (command) {
            case "LOGRQ":
                try {
                    LoginRequestPacket packet = new LoginRequestPacket(endOfCommand);
                    return packet;
                }
                catch (IllegalArgumentException e) {
                    print(e.getMessage());
                    return null;
                }
            case "DELRQ":
                try {
                    DeleteRequestPacket packet = new DeleteRequestPacket(endOfCommand);
                    return packet;
                }
                catch (IllegalArgumentException e) {
                    print(e.getMessage());
                    return null;
                }
            case "RRQ":
                try {
                    ReadRequestPacket packet = new ReadRequestPacket(endOfCommand);
                    return packet;
                }
                catch (IllegalArgumentException e) {
                    print("Illegal file name");
                    return null;
                }
            case "WRQ":
                try {
                    WriteRequestPacket packet = new WriteRequestPacket(endOfCommand);
                    return packet;
                }
                catch (IllegalArgumentException e) {
                    print("file already exists");
                    return null;
                }
            case "DIRQ":
                return new DirectoryRequestPacket();
            case "DISC":
                return new DisconnectPacket();
            default:
                return null;
        }
    }
}