package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.ClientCoordinator;
import bgu.spl.net.impl.tftp.EncodeDecodeHelper;
import bgu.spl.net.impl.tftp.GlobalConstants;
import bgu.spl.net.impl.tftp.Operation;

import java.util.Arrays;

/**
 * A class representing a ReadRequestPacket.
 * Opcode designated by: {@link Operation#RRQ}
 * It is used to request a file read from the server.
 * If the file exists, the server will send a {@link DataPacket} with the file data.
 * Otherwise, the server will send an {@link ErrorPacket}.
 * This packet is sent from the client to the server.
 */
public class ReadRequestPacket extends AbstractPacket{
    /**
     * The file name to read. Must not be empty or contain null character.
     */
    private final String fileName;

    /**
     * Constructor for creating a ReadRequestPacket.
     * @param fileName the file name to read. Must not be empty or contain null character.
     * @exception IllegalArgumentException if the file name is empty or contains null character.
     */
    public ReadRequestPacket(String fileName) throws IllegalArgumentException{
        super(Operation.RRQ.OP_CODE);
        if (fileName.length() == 0 || fileName.contains("\0")) {
            throw new IllegalArgumentException("File name cannot contain null character or be empty");
        }
        this.fileName = fileName;
    }

    /**
     * Constructor for decoding a ReadRequestPacket from a byte array.
     * @param packet the received array from the client/server. (includes opcode).
     * @exception IllegalArgumentException if the packet is too short.
     */
    public ReadRequestPacket(byte[] packet) throws IllegalArgumentException{
        super(Operation.RRQ.OP_CODE);
        // Make sure the packet is long enough to contain the file name
        // (Must be at least 4, 2 for op code, 1 for file name and 1 for null terminated symbol at the end).
        if (packet.length < 4) {
            throw new IllegalArgumentException("The packet is too short.");
        }
        // Create a byte array to contain only the file name bytes.
        // (excluding the opcode and the null terminated symbol at the end).
        this.fileName = new String(packet, 2, packet.length - 3);
    }

    /**
     * Get the file name from the packet
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    @Override
    public byte[] getBytes() {
        byte[] opcode = EncodeDecodeHelper.shortToByte(this.opCode);
        byte[] fileName = this.fileName.getBytes(GlobalConstants.ENCODING_FORMAT);
        return concatArrays(Arrays.asList(opcode, fileName), true);
    }

    @Override
    public boolean addSelf(ClientCoordinator coordinator) {
        return coordinator.addRequest(this);
    }
}
