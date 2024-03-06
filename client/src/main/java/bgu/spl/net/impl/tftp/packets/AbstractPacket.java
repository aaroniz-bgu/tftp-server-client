package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.ClientCoordinator;
import bgu.spl.net.impl.tftp.GlobalConstants;

import java.util.Collection;

public abstract class AbstractPacket {
    /**
     * The opcode of the packet.
     * @see bgu.spl.net.impl.tftp.Operation
     */
    public final short opCode;
    /**
     * If not null, the packet will be broadcast to all the connections.
     */
    public BroadcastPacket broadcast;

    /**
     * Default Constructor for creating a packet. All packets must have an opcode.
     * broadcast is set to null by default. If needed, it can be set using {@link #setBroadcastPacket(BroadcastPacket)}.
     * @param opCode the opcode of the packet.
     */
    public AbstractPacket(short opCode) {
        this.opCode = opCode;
        this.broadcast = null;
    }

    /**
     * Encodes the packet into byte array using the default charset predefined in the {@link GlobalConstants}.
     * @return a byte array representing the current packet.
     */
    abstract public byte[] getBytes();

    /**
     * @return BroadcastPacket if this operation should broadcast itself to all the connections.
     */
    public AbstractPacket getBroadcastPacket() {
        return broadcast;
    }

    /**
     * Being set by business layer, if needed!
     * Sets a new {@link BroadcastPacket} if needed.
     * @apiNote If not set, broadcast won't be sent.
     * @implNote Cannot be set again once been set.
     * @param broadcastPacket The {@link BroadcastPacket} to be broadcast.
     */
    public void setBroadcastPacket(BroadcastPacket broadcastPacket) {
        if(broadcast == null) {
            this.broadcast = broadcastPacket;
        }
    }

    /**
     * Get the opcode of the packet.
     * @return the opcode of the packet.
     * @see bgu.spl.net.impl.tftp.Operation
     */
    public short getOpCode() {
        return opCode;
    }

    /**
     * Utility function that concats arrays together.
     * @param arrays Collection of byte arrays to combine.
     * @param terminate Determines whether the packet should be terminated by the defined terminator or not.
     * @return byte array which is the concatenation of all the given arrays.
     */
    protected byte[] concatArrays(Collection<byte[]> arrays, boolean terminate) {
        int length = arrays.stream().mapToInt(a -> a.length).sum();
        length += terminate ? 1 : 0;
        byte[] output = new byte[length];
        int pos = 0;
        for(byte[] arr : arrays) {
            System.arraycopy(arr, 0, output, pos, arr.length);
            pos += arr.length;
        }
        if(terminate) { output[length-1] = GlobalConstants.TERMINATOR; }
        return output;
    }

    /**
     * Visitor pattern for the client coordinator.
     * Only for client use.
     * @param coordinator The client coordinator to visit.
     * @return true if the packet was added successfully to the queue, false otherwise.
     */
    public abstract boolean addSelf(ClientCoordinator coordinator) throws InterruptedException;
}
