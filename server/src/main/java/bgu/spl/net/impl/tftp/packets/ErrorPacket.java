package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.EncodeDecodeHelper;
import bgu.spl.net.impl.tftp.GlobalConstants;
import bgu.spl.net.impl.tftp.Operation;

import java.util.Arrays;

/**
 * This class represents an Error Packet.
 * Opcode designated by: {@link Operation#ERROR}
 * It is used to send an error message from the server to the client.
 * The error message is a string that describes the error.
 * The error code is a 16-bit field that defines the error code.
 * This packet is sent from the server to the client.
 * */
public class ErrorPacket extends AbstractPacket{
    /**
     * The error code.
     */
    private final short errorCode;
    /**
     * The error message.
     */
    private final String errorMessage;

    /**
     * Constructor for creating an ErrorPacket.
     * @param errorCode the error number. Must be a valid error number (0-7).
     * @param errorMessage the error message. Must not be null, empty or contain null character.
     * @exception IllegalArgumentException if the error number is not valid
     * or the error message is null, empty or contains null character.
     */
    public ErrorPacket(short errorCode, String errorMessage) throws IllegalArgumentException{
        super(Operation.ERROR.OP_CODE);
        // TODO Check if error number is valid (0-7)
        // TODO Maybe make enum for error numbers?
        // TODO Remove magic numbers
        if (errorCode < 0 || errorCode > 7) {
            throw new IllegalArgumentException("Error number must be between 0 and 7.");
        }
        this.errorCode = errorCode;
        if (errorMessage == null) {
            throw new IllegalArgumentException("Error message cannot be null.");
        }
        if (errorMessage.contains("\0")) {
            throw new IllegalArgumentException("Error message cannot contain null character.");
        }
        this.errorMessage = errorMessage;
    }

    /**
     * Constructor for decoding an ErrorPacket from a byte array.
     * @param received the received array from the client/server. (includes opcode).
     */
    public ErrorPacket(byte[] received) throws IllegalArgumentException {
        super(Operation.ERROR.OP_CODE);
        // Make sure the packet is long enough to contain the error code and the error message
        // (Must be at least 6, 2 for op code, 2 for error code, 1 for error message and 1 for null terminated symbol at the end).
        if (received.length < 6) {
            throw new IllegalArgumentException("The packet is too short.");
        }
        this.errorCode  = EncodeDecodeHelper.byteToShort(new byte[]{received[2], received[3]});
        // Create a byte array to contain only the error message bytes.
        // (excluding the opcode, the error code and the null terminated symbol at the end).
        this.errorMessage = new String(received, 4, received.length - 5, GlobalConstants.ENCODING_FORMAT);
    }

    /**
     * Get the error code from the packet
     * @return the error code
     */
    public short getErrorCode() {
        return errorCode;
    }

    /**
     * Get the error message from the packet
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public byte[] getBytes() {
        byte[] opcode = EncodeDecodeHelper.shortToByte(this.opCode);
        byte[] errorCode = EncodeDecodeHelper.shortToByte(this.errorCode);
        byte[] errorMessage = this.errorMessage.getBytes(GlobalConstants.ENCODING_FORMAT);
        return concatArrays(Arrays.asList(opcode, errorCode, errorMessage), true);
    }
}
