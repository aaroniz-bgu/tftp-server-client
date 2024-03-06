package bgu.spl.net.impl.tftp;

import bgu.spl.net.impl.tftp.packets.*;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The client coordinator class.
 *
 * Types of operations that can be received from the server:
 * 1. ACK - will have block 0 after sending a LOGRQ/DELRQ/WRQ/DISC or block number if sending a DATA packet
 *          as a result of a WRQ.
 * 2. DATA - will have block number and data. As a result of a RRQ or DIRQ.
 * 3. BCAST - will have a file name and a 0/1 byte to indicate if the file was added/deleted.
 * 4. ERROR - will have an error code and a message.
 *
 * Types of operations that can be receiving from the CLI:
 * 1. LOGRQ - will receive a LoginRequestPacket.
 * 2. DELRQ - will receive a DeleteRequestPacket.
 * 3. RRQ - will receive a ReadRequestPacket.
 * 4. WRQ - will receive a WriteRequestPacket.
 * 5. DIRQ - will receive a DirectoryRequestPacket.
 * 6. DISC - will receive a DisconnectPacket.
 */
public class ClientCoordinator {
    private final BlockingQueue<AbstractPacket> requestQueue;
    /**
     * The last request that was sent to the server.
     */
    private volatile Operation lastSentRequest;
    /**
     * When the server/client is working on a file, this will handle all the operations on the file.
     * It will also hold the file name that the server/client is working on.
     * If the server/client is not working on a file, this will be null.
     * Shouldn't require synchronization since when the server/client is working on a file, it's only working on
     * one file at a time. The CLI will wait when the Listener thread is working on a file.
     */
    private FilesHandler filesHandler;
    /**
     * The current directory list that the client is building from the server.
     * Supports directory lists that are larger than 1 block in size.
     * As required in the instructions.
     */
    private StringBuilder directoryList = new StringBuilder();

    /**
     * Reference so that the protocol can be terminated.
     */
    private TftpProtocol protocol;
    public ClientCoordinator(TftpProtocol protocol) {
        this.requestQueue = new ArrayBlockingQueue<>(1);
        this.lastSentRequest = Operation.NO_OP;
        this.filesHandler = null;
        this.protocol = protocol;
    }

    /**
     * Add a Login Request to the queue.
     * Called by the CLI when the user wants to login to the server.
     * @param packet the request packet to add to the queue.
     * @return true if the request was added to the queue, false otherwise.
     */
    public boolean addRequest(LoginRequestPacket packet) throws InterruptedException {
        if (lastSentRequest == Operation.NO_OP) {
            lastSentRequest = Operation.LOGRQ;
            if (requestQueue.add(packet)) {
                synchronized (requestQueue) {
                    requestQueue.wait();
                }
                return true;
            } else {
                lastSentRequest = Operation.NO_OP;
                return false;
            }
        }
        return false;
    }

    /**
     * Add a Delete Request to the queue.
     * Called by the CLI when the user wants to delete a file from the server.
     * @param packet the request packet to add to the queue.
     * @return true if the request was added to the queue, false otherwise.
     */
    public boolean addRequest(DeleteRequestPacket packet) throws InterruptedException {
        if (lastSentRequest == Operation.NO_OP) {
            lastSentRequest = Operation.DELRQ;
            if (requestQueue.add(packet)) {
                synchronized (requestQueue) {
                    requestQueue.wait();
                }
                return true;
            } else {
                lastSentRequest = Operation.NO_OP;
                return false;
            }
        }
        return false;
    }

    /**
     * Add a Read Request to the queue.
     * Called by the CLI when the user wants to read a file from the server.
     * @param packet the request packet to add to the queue.
     * @return true if the request was added to the queue, false otherwise.
     * @throws IllegalArgumentException if the file name is empty or contains null character.
     */
    public boolean addRequest(ReadRequestPacket packet) throws InterruptedException {
        if (lastSentRequest == Operation.NO_OP && requestQueue.add(packet)) {
            try {
                lastSentRequest = Operation.RRQ;
                filesHandler = new FilesHandler(packet.getFileName());
                filesHandler.createNewFile();
                synchronized (requestQueue) {
                    requestQueue.wait();
                }
                return true;
            }
            catch (IllegalArgumentException e) {
                lastSentRequest = Operation.NO_OP;
                throw e;
            } catch (IOException e) {
                lastSentRequest = Operation.NO_OP;
                filesHandler = null;
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    /**
     * Add a Write Request to the queue.
     * Called by the CLI when the user wants to write a file to the server.
     * @param packet the request packet to add to the queue.
     * @return true if the request was added to the queue, false otherwise.
     */
    public boolean addRequest(WriteRequestPacket packet) throws InterruptedException {
        if (lastSentRequest == Operation.NO_OP) {
            try {
                lastSentRequest = Operation.WRQ;
                filesHandler = new FilesHandler(packet.getFileName());
                if (requestQueue.add(packet)) {
                    synchronized (requestQueue) {
                        requestQueue.wait();
                    }
                    return true;
                } else {
                    lastSentRequest = Operation.NO_OP;
                    return false;
                }
            }
            catch (IllegalArgumentException e) {
                lastSentRequest = Operation.NO_OP;
                throw e;
            }
        }
        return false;
    }

    /**
     * Add a Directory Request to the queue.
     * @param packet the request packet to add to the queue.
     * @return true if the request was added to the queue, false otherwise.
     */
    public boolean addRequest(DirectoryRequestPacket packet) throws InterruptedException {
        if (lastSentRequest == Operation.NO_OP) {
            lastSentRequest = Operation.DIRQ;
            boolean added = requestQueue.add(packet);
            if(added) {
                synchronized (requestQueue) {
                    requestQueue.wait();
                }
            }
            return added;
        }
        return false;
    }

    /**
     * Add a Disconnect Request to the queue.
     * Switches the attemptingToTerminate flag to true.
     * Will only terminate the protocol if the server responds with an ACK packet with block number 0.
     * Otherwise, the protocol will not terminate. And the attemptingToTerminate flag will be reset to false.
     * @param packet the request packet to add to the queue.
     * @return true if the request was added to the queue, false otherwise.
     */
    public boolean addRequest(DisconnectPacket packet) throws InterruptedException {
        if (lastSentRequest == Operation.NO_OP) {
            lastSentRequest = Operation.DISC;
            boolean added = requestQueue.add(packet);
            if(added) {
                synchronized (requestQueue) {
                    requestQueue.wait();
                }
            }
            return added;
        }
        return false;
    }

    /**
     * When received a DATA packet save the data to a file or a buffer depending on if we are in
     * RRQ Command or DIRQ Command and send an ACK packet in return with the corresponding block
     * number written in the DATA packet. Once the last block is received, the filesHandler will be
     * reset to null and the lastSentRequest will be reset to NO_OP.
     * @param packet the received packet.
     * @return the ACK packet to send back to the server.
     */
    public byte[] handle(DataPacket packet) {
        if (lastSentRequest == Operation.RRQ) {
            filesHandler.WriteData(packet.getData());
            if (packet.getPacketSize() < GlobalConstants.MAX_DATA_PACKET_SIZE) {
                // This is the last packet.
                filesHandler = null;
                lastSentRequest = Operation.NO_OP;
                wakeCLI();
            }
        } else { // DIRQ
            directoryList.append(new String(packet.getData()));
            if (packet.getPacketSize() < GlobalConstants.MAX_DATA_PACKET_SIZE) {
                // This is the last packet.
                System.out.println(directoryList.toString());
                directoryList = new StringBuilder();
                lastSentRequest = Operation.NO_OP;
                wakeCLI();
            }
        }
        AcknowledgementPacket ack = new AcknowledgementPacket(packet.getBlockNumber());

        return ack.getBytes();
    }

    /**
     * If block number is 0, then it's a response to a LOGRQ/DELRQ/WRQ/DISC.
     * In which case, shouldn't return anything to the server.
     * If block number is not 0, then it's a response to a DATA packet.
     * In which case should prepare the next DATA packet to send to the server from according to the
     * filesHandler due to a WRQ command.
     * Shouldn't return anything to the server.
     * Always print to the terminal the following:
     * ACK <block number>
     * @param packet
     * @return
     */
    public byte[] handle(AcknowledgementPacket packet) {
        System.out.println("ACK " + packet.getBlockNumber());
        if (packet.getBlockNumber() != 0 && lastSentRequest == Operation.WRQ) {
            // Prepare the next DATA packet to send to the server.
            // If the file is done, then filesHandler will be reset to null.
            byte[] data = filesHandler.ReadFile(packet.getBlockNumber() + 1);
            // Reached the end of the file.
            if (data.length < GlobalConstants.MAX_DATA_PACKET_SIZE) {
                filesHandler = null;
                lastSentRequest = Operation.NO_OP;
                wakeCLI();
            }
            return new DataPacket((short)data.length, (short) (packet.getBlockNumber() + 1), data).getBytes();
        }
        else if (lastSentRequest == Operation.DISC && packet.getBlockNumber() == 0) {
            // The server responded with an ACK packet with block number 0 when the client asked to disconnect.
            // Therefore, the protocol should terminate.
            lastSentRequest = Operation.NO_OP;
            protocol.terminate();
            wakeCLI();
            // TODO terminate CLI
            return null;
        }
        else {
            lastSentRequest = Operation.NO_OP;
            return null;
        }
    }

    /**
     * Shouldn't return anything to the server.
     * Print to the terminal the following:
     * BCAST <del/add> <file name>
     * @param packet
     * @return
     */
    public byte[] handle(BroadcastPacket packet) {
        System.out.println("BCAST " + (packet.isAdd() ? "add" : "del") + " " + packet.getFileName());
        return null;
    }

    /**
     * Shouldn't return anything to the server.
     * Print to the terminal the following:
     * Error <Error number> <Error Message if exist>
     * Should delete the file if the client is copying a file from the server and the server
     * encountered an error.
     * Should reset the filesHandler to null if it's not null.
     * Should also reset the directoryList to an empty string.
     * @param packet
     * @return
     */
    public byte[] handle(ErrorPacket packet) {
        System.out.println("Error " + packet.getErrorCode() + " " + packet.getErrorMessage());
        if (lastSentRequest == Operation.RRQ) {
            try {
                filesHandler = null;
                filesHandler.DeleteFile();
            }
            catch (Exception e) {
                System.out.println("Failed to delete file: " + e.getMessage());
            }
        }
        else if (lastSentRequest == Operation.DIRQ) {
            directoryList = new StringBuilder();
        }
        else if (lastSentRequest == Operation.WRQ) {
            filesHandler = null;
        }
        lastSentRequest = Operation.NO_OP;
        wakeCLI();
        return null;
    }

    /**
     * Empty the request queue and notify all the waiting threads (CLI).
     * @return the packet that was in the queue, no real need.
     */
    private AbstractPacket wakeCLI() {
        AbstractPacket out = requestQueue.poll();
        requestQueue.notifyAll();
        return out;
    }

    /**
     * Get the next request from the queue without removing it.
     * @return the next request from the queue.
     */
    public AbstractPacket getRequest() {
        return requestQueue.peek();
    }
}
