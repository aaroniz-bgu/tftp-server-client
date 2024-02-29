package bgu.spl.net.impl.tftp.packets;

import java.util.Collection;

public abstract class AbstractPacket {
    public final short opCode;
    public AbstractPacket(short opCode) {
        this.opCode = opCode;
    }

    /**
     * Encodes the packet into byte array using the default charset predefined in the {@link GlobalConstants}.
     * @return a byte array representing the current packet.
     */
    abstract public byte[] getBytes();

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