package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.ClientCoordinator;
import bgu.spl.net.impl.tftp.EncodeDecodeHelper;
import bgu.spl.net.impl.tftp.GlobalConstants;
import bgu.spl.net.impl.tftp.Operation;

import java.util.Arrays;

/**
 * This class represents a Delete Request Packet.
 * Opcode designated by: {@link Operation#DELRQ}
 * A delete request packet is sent from the client to the server to request the deletion of a file.
 * The packet should be acknowledged with a {@link AcknowledgementPacket} with block = 0 if successful.
 * If the file does not exist, the server will respond with an {@link ErrorPacket}.
 * This packet is sent from the client to the server.
 */
public class DeleteRequestPacket extends AbstractPacket{
    /**
     * The file name to delete
     */
    private final String fileName;

    /**
     * Delete request packet constructor
     * @param fileName the name of the file to delete (must not be empty and cannot contain null character).
     * @exception IllegalArgumentException if the file name is empty or contains null character.
     */
    public DeleteRequestPacket(String fileName) throws IllegalArgumentException{
        super(Operation.DELRQ.OP_CODE);
        if (fileName.length() == 0 || fileName.contains("\0")) {
            throw new IllegalArgumentException("File name cannot contain null character or be empty");
        }
        this.fileName = fileName;
    }

    /**
     * Delete request packet constructor
     * @param packet the byte array to decode (includes opcode).
     * @exception IllegalArgumentException if the packet is too short.
     */
    public DeleteRequestPacket(byte[] packet) throws IllegalArgumentException{
        super(Operation.DELRQ.OP_CODE);
        // Make sure the packet is long enough to contain the file name
        // (Must be at least 4, 2 for op code, 1 for file name and 1 for null terminated symbol at the end).
        if (packet.length < 4) {
            throw new IllegalArgumentException("The packet is too short.");
        }
        // Create a byte array to contain only the file name bytes.
        // (excluding the opcode and the null terminated symbol at the end).
        this.fileName = new String(packet, 2, packet.length - 3, GlobalConstants.ENCODING_FORMAT);
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
