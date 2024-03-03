package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.EncodeDecodeHelper;
import bgu.spl.net.impl.tftp.GlobalConstants;
import bgu.spl.net.impl.tftp.Operation;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.IllegalFormatConversionException;
import java.util.InputMismatchException;

/**
 * This class represents a Broadcast Packet.
 * Opcode designated by: {@link Operation#BCAST}
 * A Broadcast packet is used to notify all logged-in clients that a file was deleted/added. This means users who are
 * connected but have not completed a successful login should not receive this message. This is a Server to client message
 * only.
 * This packet is sent from the server to the client.
 */
public class BroadcastPacket extends AbstractPacket {
    /**
     * Whether the file was added or deleted, true if added, false if deleted.
     */
    private final boolean add;
    /**
     * The file added/deleted.
     */
    private final String fileName;

    /**
     * Constructor for creating a BroadcastPacket.
     * @param add whether the file was added or deleted, true if added, false if deleted.
     * @param fileName the file added/deleted.
     * @exception IllegalArgumentException if the file name is empty or contains null character.
     */
    public BroadcastPacket(boolean add, String fileName) throws IllegalArgumentException {
        super(Operation.BCAST.OP_CODE);
        this.add = add;
        if (fileName.length() == 0 || fileName.contains("\0")) {
            throw new IllegalArgumentException("File name cannot contain null character or be empty");
        }
        this.fileName = fileName;
    }

    /**
     * Constructor for decoding a BroadcastPacket from a byte array.
     * @param packet the byte array to decode (includes opcode).
     * @exception IllegalArgumentException if the add/delete flag is not 0 or 1 or the packet is too short.
     */
    public BroadcastPacket(byte[] packet) throws IllegalArgumentException {
        super(Operation.BCAST.OP_CODE);
        // Make sure the add/delete flag is 0 or 1.
        if (!(packet[2] == 0 || packet[2] == 1)) {
            throw new IllegalArgumentException("The add/delete flag must be 0 or 1.");
        }
        this.add = packet[2] == 1; // 1 if added, 0 if deleted
        // Make sure the packet is long enough to contain the file name (Must be at least 5, 2 for op code, 1 for add flag
        // and 1 for file name and 1 for null terminated symbol at the end).
        if (packet.length < 5) {
            throw new IllegalArgumentException("The packet is too short.");
        }
        // Create a byte array to contain only the file name bytes.
        byte[] bytesToDecode = new byte[packet.length - 4];
        // Copy the file name bytes from the packet to the new byte array.
        // (excluding the opcode and the add/delete flag and the null terminated symbol at the end).
        System.arraycopy(packet, 3, bytesToDecode, 0, packet.length - 4);
        this.fileName = new String(bytesToDecode, GlobalConstants.ENCODING_FORMAT);
    }

    /**
     * Get the add/delete flag from the packet
     * @return true if added, false if deleted
     */
    public boolean isAdd() {
        return add;
    }

    /**
     * Gets the file name from the packet
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    @Override
    public byte[] getBytes() {
        byte[] opcode = EncodeDecodeHelper.shortToByte(this.opCode);
        byte[] add = new byte[]{(byte) (this.add ? 1 : 0)}; // 1 if added, 0 if deleted
        byte[] fileName = this.fileName.getBytes(GlobalConstants.ENCODING_FORMAT);
        return concatArrays(Arrays.asList(opcode, add, fileName), true);
    }
}
