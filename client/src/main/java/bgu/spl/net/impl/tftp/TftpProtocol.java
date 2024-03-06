package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.tftp.packets.AcknowledgementPacket;
import bgu.spl.net.impl.tftp.packets.BroadcastPacket;
import bgu.spl.net.impl.tftp.packets.DataPacket;
import bgu.spl.net.impl.tftp.packets.ErrorPacket;

public class TftpProtocol implements MessagingProtocol<byte[]> {
    /**
     * True if the protocol should terminate, false otherwise.
     */
    private volatile boolean terminate = false;
    private final ClientCoordinator coordinator;

    public TftpProtocol() {
        this.coordinator = new ClientCoordinator(this);
    }

    public ClientCoordinator getCoordinator() {
        return coordinator;
    }

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
        if (msg.length < 2) {
            throw new RuntimeException("Message received without operation code");
        }
        // Retrieve op code:
        short opCode = EncodeDecodeHelper.byteToShort(new byte[]{msg[0], msg[1]});
        Operation op = Operation.OPS[opCode];
        return processHelper(msg, op);
    }

    /**
     * Creates the response to the server based on the operation code.
     * Creates the relevant packet and sends it to the clientCoordinator to handle.
     * @param msg the received message from the server
     * @param op the operation code of the received message
     * @return the response to send back to the server or null if no response is expected by the client
     * @exception RuntimeException if the message received without operation code or unknown operation code
     */
    private byte[] processHelper(byte[] msg, Operation op) throws RuntimeException{
        switch (op) {
            case BCAST:
                return coordinator.handle(new BroadcastPacket(msg));
            case DATA:
                return coordinator.handle(new DataPacket(msg));
            case ERROR:
                return coordinator.handle(new ErrorPacket(msg));
            case ACK:
                return coordinator.handle(new AcknowledgementPacket(msg));
            default:
                throw new RuntimeException("Unknown operation code");
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
     * Perhaps should be called by the CLI after the user requested to disconnect from the server and
     * received an ACK packet from the server.
     * If received an error packet from the server, the protocol should not be terminated.
     */
    public void terminate() {
        terminate = true;
    }
}
