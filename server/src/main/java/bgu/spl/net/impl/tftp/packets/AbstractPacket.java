package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.GlobalConstants;

import java.util.Collection;

public abstract class AbstractPacket {
    public final short opCode;
    public BroadcastPacket broadcast;

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
}
