package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.EncodeDecodeHelper;
import bgu.spl.net.impl.tftp.Operation;

/**
 * This class represents a Disconnect Packet.
 * Opcode designated by: {@link Operation#DISC}
 * It is sent from the client to the server to disconnect the connection.
 * The server will respond with an acknowledgment and then close the connection.
 * The client will close the connection after receiving the acknowledgment.
 * This packet is sent from the client to the server.
 */
public class DisconnectPacket extends AbstractPacket{

    /**
     * Default constructor to build a disconnect packet.
     */
    public DisconnectPacket() {
        super(Operation.DISC.OP_CODE);
    }

    // This constructor has no reason to exist, but adding it anyway just in case.
    /**
     * Reconstructs a disconnect packet when receiving a byte array.
     * @param received the received array from the client/server. (includes opcode).
     */
    public DisconnectPacket(byte[] received) {
        super(EncodeDecodeHelper.byteToShort(new byte[]{received[0], received[1]}));
    }

    @Override
    public byte[] getBytes() {
        return EncodeDecodeHelper.shortToByte(opCode);
    }
}
