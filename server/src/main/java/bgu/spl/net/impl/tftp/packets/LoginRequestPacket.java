package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.EncodeDecodeHelper;
import bgu.spl.net.impl.tftp.GlobalConstants;
import bgu.spl.net.impl.tftp.Operation;

import java.util.Arrays;

/**
 * This class represents a Login Request Packet.
 * Opcode designated by: {@link Operation#LOGRQ}
 * It is used to request a login from the client to the server.
 * The username is a string that represents the username of the client.
 * If the username is already logged in, the server will send an {@link ErrorPacket}.
 * Otherwise, the server will send an {@link AcknowledgementPacket}
 * with block number 0 to acknowledge the login request.
 * This packet is sent from the client to the server.
 */
public class LoginRequestPacket extends AbstractPacket{
    /**
     * The username. Must not be null or empty and cannot contain null character.
     */
    private final String userName;

    /**
     * Constructor for creating a LoginRequestPacket.
     * @param userName the username. Must not be null or empty and cannot contain null character.
     * @exception IllegalArgumentException if the username is null or empty or contains null character.
     */
    public LoginRequestPacket(String userName) throws IllegalArgumentException{
        super(Operation.LOGRQ.OP_CODE);
        if (userName == null || userName.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }
        if (userName.contains("\0")) {
            throw new IllegalArgumentException("Username cannot contain null character.");
        }
        this.userName = userName;
    }

    /**
     * Constructor for decoding a LoginRequestPacket from a byte array.
     * @param packet the received array from the client/server. (includes opcode).
     * @exception IllegalArgumentException if the packet is too short.
     */
    public LoginRequestPacket(byte[] packet) throws IllegalArgumentException {
        super(Operation.LOGRQ.OP_CODE);
        // Make sure the packet is long enough to contain the username
        // (Must be at least 4, 2 for op code, 1 for username and 1 for null terminated symbol at the end).
        if (packet.length < 4) {
            throw new IllegalArgumentException("The packet is too short.");
        }
        // Create a byte array to contain only the username bytes.
        // (excluding the opcode and the null terminated symbol at the end).
        this.userName = new String(packet, 2, packet.length - 3);
    }

    /**
     * Get the username from the packet
     * @return the username
     */
    public String getUserName() {
        return this.userName;
    }

    @Override
    public byte[] getBytes() {
        byte[] opcode = EncodeDecodeHelper.shortToByte(this.opCode);
        byte[] userName = this.userName.getBytes(GlobalConstants.ENCODING_FORMAT);
        return concatArrays(Arrays.asList(opcode, userName), true);
    }
}
