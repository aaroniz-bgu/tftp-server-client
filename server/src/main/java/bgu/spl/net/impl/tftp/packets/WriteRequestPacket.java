package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.EncodeDecodeHelper;
import bgu.spl.net.impl.tftp.GlobalConstants;
import bgu.spl.net.impl.tftp.Operation;

import java.util.Arrays;

public class WriteRequestPacket extends AbstractPacket{
    /**
     * The file name to write. Must not be empty or contain null character.
     */
    private final String fileName;

    /**
     * Constructor for creating a WriteRequestPacket.
     * @param fileName the file name to write. Must not be empty or contain null character.
     * @throws IllegalArgumentException if the file name is empty or contains null character.
     */
    public WriteRequestPacket(String fileName) throws IllegalArgumentException{
        super(Operation.WRQ.OP_CODE);
        if (fileName.length() == 0 || fileName.contains("\0")) {
            throw new IllegalArgumentException("File name cannot contain null character or be empty");
        }
        this.fileName = fileName;
    }

    /**
     * Constructor for decoding a WriteRequestPacket from a byte array.
     * @param packet the received array from the client/server. (includes opcode).
     * @throws IllegalArgumentException if the packet is too short.
     */
    public WriteRequestPacket(byte[] packet) throws IllegalArgumentException{
        super(Operation.WRQ.OP_CODE);
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
}
