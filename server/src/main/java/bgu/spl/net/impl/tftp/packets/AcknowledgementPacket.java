package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.EncodeDecodeHelper;
import bgu.spl.net.impl.tftp.Operation;

import java.util.Arrays;

/**
 * This class represents an Acknowledgement Packet.
 * Opcode designated by: {@link Operation#ACK}
 * Acknowledgement packets are used to acknowledge different packets.
 * The block number is used when acknowledging a
 * {@link DataPacket}. Once a {@link DataPacket} is acknowledged, The next block can be sent. Other packets:
 * {@link LoginRequestPacket}, {@link WriteRequestPacket}, {@link DeleteRequestPacket} or {@link DisconnectPacket}
 * should be acknowledged with block = 0 if successful.
 * This packet is sent from the server to the client.
 */
public class AcknowledgementPacket extends AbstractPacket{

    /**
     * The block number to acknowledge.
     * The block number is used when acknowledging a {@link DataPacket}. Once a {@link DataPacket} is acknowledged,
     * The next block can be sent. Other packets: {@link LoginRequestPacket}, {@link WriteRequestPacket},
     * {@link DeleteRequestPacket} or {@link DisconnectPacket} should be acknowledged with block = 0 if successful.
     * @see DataPacket
     */
    private final short blockNumber;

    /**
     * Constructor for creating an AcknowledgementPacket.
     * @param blockNumber the block number to acknowledge.
     * @exception IndexOutOfBoundsException if the block number is negative.
     */
    public AcknowledgementPacket(short blockNumber) throws IndexOutOfBoundsException {
        super(Operation.ACK.OP_CODE);
        if (blockNumber < 0) {
            throw new IndexOutOfBoundsException("Block number cannot be negative.");
        }
        this.blockNumber = blockNumber;
    }

    /**
     * Constructor for decoding an AcknowledgementPacket from a byte array.
     * @param packet the byte array to decode (includes opcode).
     * @exception IllegalArgumentException if the packet is too short.
     * @exception IndexOutOfBoundsException if the block number is negative.
     */
    public AcknowledgementPacket(byte[] packet) throws IllegalArgumentException, IndexOutOfBoundsException{
        super(Operation.ACK.OP_CODE);
        if (packet.length < 4) {
            throw new IllegalArgumentException("The packet is too short.");
        }
        // Create byte array containing the block number bytes.
        byte[] blockNumberBytes = new byte[]{packet[2], packet[3]};
        short blockNumber = EncodeDecodeHelper.byteToShort(blockNumberBytes);
        if (blockNumber < 0) {
            throw new IndexOutOfBoundsException("Block number cannot be negative.");
        }
        this.blockNumber = blockNumber;
    }

    /**
     * Get the block number from the packet
     * @return the block number
     */
    public short getBlockNumber() {
        return blockNumber;
    }

    @Override
    public byte[] getBytes() {
        byte[] opcode = EncodeDecodeHelper.shortToByte(this.opCode);
        byte[] blockNumber = EncodeDecodeHelper.shortToByte(this.blockNumber);
        return concatArrays(Arrays.asList(opcode, blockNumber), false);
    }
}
