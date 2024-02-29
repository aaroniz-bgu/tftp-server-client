package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.EncodeDecodeHelper;
import bgu.spl.net.impl.tftp.GlobalConstants;

import java.util.Arrays;

/**
 *
 */
public class DataPacket extends AbstractPacket {
    private static final short OP_CODE = 3;
    public final short packetSize;
    public final short blockNumber;
    public final byte[] data;

    /**
     * Default constructor to build an internal contract for data-packets.
     * @param packetSize The size of data that would be transmitted.
     * @param blockNumber The block number denotes the specific location in a file where the data is intended to be stored.
     * @param data The data itself, stored in UTF-8 protocol.
     */
    public DataPacket(short packetSize, short blockNumber, byte[] data) {
        super(OP_CODE);
        if(data.length > GlobalConstants.MAX_DATA_PACKET_SIZE) {
            throw new IllegalArgumentException("Not allowed to transmit over 512 bytes of data per packet.");
        }
        this.packetSize  = packetSize;
        this.blockNumber = blockNumber;
        this.data = data;
    }

    /**
     * Reconstructs a packet when receiving a byte array.
     * @param received the received array from the client/server.
     */
    public DataPacket(byte[] received) {
        super(EncodeDecodeHelper.byteToShort(new byte[]{received[0], received[1]}));
        this.packetSize  = EncodeDecodeHelper.byteToShort(new byte[]{received[2], received[3]});
        this.blockNumber = EncodeDecodeHelper.byteToShort(new byte[]{received[4], received[5]});
        this.data = new byte[received.length - 6];
        System.arraycopy(received, 6, this.data, 0, this.data.length);
    }

    @Override
    public byte[] getBytes() {
        return concatArrays(Arrays.asList(
                EncodeDecodeHelper.shortToByte(opCode),
                EncodeDecodeHelper.shortToByte(packetSize),
                EncodeDecodeHelper.shortToByte(blockNumber),
                data), false);
    }
}
