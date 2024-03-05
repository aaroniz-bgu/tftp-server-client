package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.tftp.packets.AcknowledgementPacket;
import bgu.spl.net.impl.tftp.packets.BroadcastPacket;
import bgu.spl.net.impl.tftp.packets.DataPacket;
import bgu.spl.net.impl.tftp.packets.ErrorPacket;

public class TftpProtocol implements MessagingProtocol<byte[]> {
    /**
     * The current directory list that the client is building from the server.
     * Supports directory lists that are larger than 1 block in size.
     * As required in the instructions.
     */
    private StringBuilder directoryList = new StringBuilder();
    /**
     * When the server/client is working on a file, this will handle all the operations on the file.
     * It will also hold the file name that the server/client is working on.
     * If the server/client is not working on a file, this will be null.
     * Shouldn't require synchronization since when the server/client is working on a file, it's only working on
     * one file at a time. The CLI will wait when the Listener thread is working on a file. Once the
     * Listener thread is done working on the file, it will notify the CLI and reset the filesHandler to null.
     */
    private FilesHandler filesHandler = null;
    /**
     * True if the protocol should terminate, false otherwise.
     */
    private volatile boolean terminate = false;

    /**
     * Process the given message from the server
     * @param msg the received message
     * @return the response to send back to the server or null if no response is expected by the client
     * @exception RuntimeException if the message received without operation code or unknown operation code
     * @exception IllegalArgumentException if the file name is empty or contains null character.
     * @exception IllegalStateException if the server is already working on a file and
     * the CLI is trying to work on another file.
     */
    @Override
    public byte[] process(byte[] msg) {
        if(msg.length < 2) {
            throw new RuntimeException("Message received without operation code");
        }
        // Retrieve op code:
        short opCode = EncodeDecodeHelper.byteToShort(new byte[]{msg[0], msg[1]});
        Operation op = Operation.OPS[opCode];

        /*
         Types of operations that can be received:
         1. ACK - will have block 0 after sending a LOGRQ/DELRQ/WRQ/DISC or block number if sending a DATA packet
                  as a result of a WRQ.
         2. DATA - will have block number and data. As a result of a RRQ or DIRQ.
         3. BCAST - will have a file name and a 0/1 byte to indicate if the file was added/deleted.
         4. ERROR - will have an error code and a message.
        */
        switch(op) {
            case ACK:
                /*
                 If block number is 0, then it's a response to a LOGRQ/DELRQ/WRQ/DISC.
                 In which case, shouldn't return anything to the server.
                 If block number is not 0, then it's a response to a DATA packet.
                 In which case should prepare the next DATA packet to send to the server from according to the
                 filesHandler due to a WRQ command.
                 Shouldn't return anything to the server.
                 Always print to the terminal the following:
                 ACK <block number>
                */
                AcknowledgementPacket packet = new AcknowledgementPacket(msg);
                // TODO Decide if printing to the terminal should be done here or with some other method.
                System.out.println("ACK " + packet.getBlockNumber());
                if (packet.getBlockNumber() != 0 && filesHandler != null) {
                    // Prepare the next DATA packet to send to the server.
                    // If the file is done, then filesHandler will be reset to null.
                    byte[] data = filesHandler.ReadFile(packet.getBlockNumber() + 1);
                    // Reached the end of the file.
                    if (data.length < GlobalConstants.MAX_DATA_PACKET_SIZE) {
                        filesHandler = null;
                    }
                    return new DataPacket((short)data.length, (short) (packet.getBlockNumber() + 1), data).getBytes();
                }

                return null;
            case DATA:
                /*
                 When received a DATA packet save the data to a
                 file or a buffer depending on if we are in RRQ Command or DIRQ
                 Command and send an ACK packet in return with the
                 corresponding block number written in the DATA packet.
                */
                DataPacket dataPacket = new DataPacket(msg);

                if (filesHandler != null) {
                    filesHandler.WriteData(dataPacket.getData());
                    if (dataPacket.getPacketSize() < GlobalConstants.MAX_DATA_PACKET_SIZE) {
                        // This is the last packet.
                        filesHandler = null;
                    }
                } else {
                    directoryList.append(new String(dataPacket.getData()));
                    if (dataPacket.getPacketSize() < GlobalConstants.MAX_DATA_PACKET_SIZE) {
                        // This is the last packet.
                        // TODO decide if directly printing to the terminal or through some other method.
                        System.out.println(directoryList.toString());
                        directoryList = new StringBuilder();
                    }
                }
                AcknowledgementPacket ack = new AcknowledgementPacket(dataPacket.getBlockNumber());

                return ack.getBytes();
            case BCAST:
                /*
                 Shouldn't return anything to the server.
                 Print to the terminal the following:
                 BCAST <del/add> <file name>
                */
                BroadcastPacket broadcastPacket = new BroadcastPacket(msg);
                // TODO decide if printing to the terminal should be done here or with some other method.
                System.out.println("BCAST " + (broadcastPacket.isAdd() ? "add" : "del") + " " + broadcastPacket.getFileName());

                return null;
            case ERROR:
                /*
                 Shouldn't return anything to the server.
                 Print to the terminal the following:
                 Error <Error number> <Error Message if exist>
                 Should delete the file if the client is copying a file from the server and the server
                 encountered an error.
                 Should reset the filesHandler to null if it's not null.
                 Should also reset the directoryList to an empty string.
                */

                ErrorPacket errorPacket = new ErrorPacket(msg);
                System.out.println("Error " + errorPacket.getErrorCode() + " " + errorPacket.getErrorMessage());
                if (filesHandler != null) {
                    try {
                        filesHandler.DeleteFile();
                    }
                    catch (Exception e) {
                        // TODO decide if printing to the terminal should be done here or with some other method.
                        System.out.println("Failed to delete file: " + e.getMessage());
                    }
                    filesHandler = null;
                }
                directoryList = new StringBuilder();

                // TODO make sure this is the correct way to deal with this type of error
                //      Added a method to cancel the termination of the protocol in case the user failed to disconnect
                //      from the server which can be accessed elsewhere if this isn't the right spot.
                // If the request was a disconnect request, this means the user failed to disconnect from the server.
                // Therefore, the client should not be terminated yet.
                if (terminate) {
                    terminate = false;
                }

                return null;
            default:
                // Unrecognized operation.
                System.out.println("Unrecognized operation code: " + opCode);
                return null;
        }
    }

    /**
     * Checks if the protocol should terminate.
     * @return true if the protocol should terminate, false otherwise
     */
    @Override
    public boolean shouldTerminate() {
        return terminate;
    }

    /**
     * Terminate the protocol.
     */
    public void terminate() {
        terminate = true;
    }

    /**
     * Cancel the termination of the protocol in case the user failed to disconnect from the server.
     */
    public void cancelTerminate() {
        terminate = false;
    }

    /**
     * Should be called by the CLI to let the Listener thread know which file to work on.
     * Should be used for RRQ and WRQ commands.
     * Updates the current file that the server is working on.
     * @param fileName The new file name to work on.
     * @exception IllegalArgumentException if the file name is empty or contains null character.
     * @exception IllegalStateException if the server is already working on a file.
     */
    public void updateWorkingFile(String fileName) throws IllegalArgumentException, IllegalStateException{
        if (filesHandler != null) {
            throw new IllegalStateException("Server is already working on a file.");
        }
        filesHandler = new FilesHandler(fileName);
    }
}
