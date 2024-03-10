package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.EncodeDecodeHelper;
import bgu.spl.net.impl.tftp.GlobalConstants;
import bgu.spl.net.impl.tftp.Operation;

import java.util.Arrays;

/**
 * This class represents a Data Packet.
 * Opcode designated by: {@link Operation#DATA}
 * A Data packet is used to transfer data from the client to the server or vice versa.
 * The data field is a sequence of 0 to {@link bgu.spl.net.impl.tftp.GlobalConstants#MAX_DATA_PACKET_SIZE} bytes of data.
 * If it is {@link bgu.spl.net.impl.tftp.GlobalConstants#MAX_DATA_PACKET_SIZE} bytes, the block is not the last block of data;
 * if it is less than {@link bgu.spl.net.impl.tftp.GlobalConstants#MAX_DATA_PACKET_SIZE} bytes, it is the last block of data.
 * This packet is sent from the client to the server and vice versa.
 */
public class DataPacket extends AbstractPacket {
    private final short packetSize;
    private final short blockNumber;
    private final byte[] data;

    /**
     * Default constructor to build an internal contract for data-packets.
     * @param packetSize The size of data that would be transmitted.
     * @param blockNumber The block number denotes the specific location in a file where the data is intended to be stored.
     * @param data The data itself, stored in UTF-8 protocol.
     * @exception IllegalArgumentException if the data is too long.
     */
    public DataPacket(short packetSize, short blockNumber, byte[] data) throws IllegalArgumentException {
        super(Operation.DATA.OP_CODE);
        if(data.length > GlobalConstants.MAX_DATA_PACKET_SIZE) {
            throw new IllegalArgumentException("Not allowed to transmit over 512 bytes of data per packet.");
        }
        this.packetSize  = packetSize;
        this.blockNumber = blockNumber;
        this.data = data;
    }

    /**
     * Reconstructs a packet when receiving a byte array.
     * @param received the received array from the client/server. (includes opcode).
     * @exception IllegalArgumentException if the packet is too short.
     */
    public DataPacket(byte[] received) throws IllegalArgumentException{
        super(EncodeDecodeHelper.byteToShort(new byte[]{received[0], received[1]}));
        // Data can be empty if message is divisible by MAX_DATA_PACKET_SIZE
        if (received.length < 6) {
            throw new IllegalArgumentException("The packet is too short.");
        }
        this.packetSize  = EncodeDecodeHelper.byteToShort(new byte[]{received[2], received[3]});
        this.blockNumber = EncodeDecodeHelper.byteToShort(new byte[]{received[4], received[5]});
        this.data = new byte[received.length - 6];
        System.arraycopy(received, 6, this.data, 0, this.data.length);
    }

    /**
     * Get the block number from the packet
     * @return the block number
     */
    public short getBlockNumber() {
        return blockNumber;
    }

    /**
     * Get the data from the packet
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Get the packet size from the packet
     * @return the packet size
     */
    public short getPacketSize() {
        return packetSize;
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
