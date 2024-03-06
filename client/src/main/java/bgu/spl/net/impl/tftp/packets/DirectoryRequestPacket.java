package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.ClientCoordinator;
import bgu.spl.net.impl.tftp.EncodeDecodeHelper;
import bgu.spl.net.impl.tftp.Operation;

/**
 * This class represents a Directory Request Packet.
 * Opcode designated by: {@link Operation#DIRQ}
 * It is used to request a directory listing from the server.
 * The directory listing will be sent back as {@link DataPacket}s.
 * This packet is sent from the client to the server.
 */
public class DirectoryRequestPacket extends AbstractPacket{

    /**
     * Constructor for a Directory Request Packet.
     */
    public DirectoryRequestPacket() {
        super(Operation.DIRQ.OP_CODE);
    }

    // This constructor has no reason to exist, but adding it anyway just in case.
    /**
     * Reconstructs a directory request packet when receiving a byte array.
     * @param received the received array from the client/server. (includes opcode).
     */
    public DirectoryRequestPacket(byte[] received) {
        super(Operation.DIRQ.OP_CODE);
    }

    @Override
    public byte[] getBytes() {
        return EncodeDecodeHelper.shortToByte(this.opCode);
    }

    @Override
    public boolean addSelf(ClientCoordinator coordinator) throws InterruptedException {
        return coordinator.addRequest(this);
    }
}
