package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.util.LinkedList;

import static bgu.spl.net.impl.tftp.GlobalConstants.TERMINATOR;
import static bgu.spl.net.impl.tftp.Operation.*;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    private LinkedList<Byte> message;
    private Operation operation;
    private boolean isData;
    private boolean isWeird;
    private int weirdness;
    private int packetSize;

    public TftpEncoderDecoder() {
        message = new LinkedList<>();
        operation = NO_OP;
        packetSize = -1;
        isData = false;
        isWeird = false;
        weirdness = 0;
    }

    @Override
    public byte[] decodeNextByte(byte nextByte) {
        message.add(nextByte);
        // Determine op code:
        if(message.size() == 2) {
            operation = Operation.OPS[EncodeDecodeHelper.byteToShort(new byte[]{message.getFirst(), message.getLast()})];
            // determine length to read if not terminated by delimiter.
            if(operation == DIRQ || operation == DISC) {
                // those are ending after 2 bytes.
                packetSize = 2;
            } else if(operation == DATA) {
                // if that's a data packet, wait until the 4'th byte was read.
                isData = true;
            } else if (operation == ACK) {
                packetSize = 4;
            } else if(operation == ERROR || operation == BCAST) {
                isWeird = true;
                weirdness = operation == ERROR ? 6 : 5;
            }
        }
        if(isData && message.size() == 4) {
            packetSize = EncodeDecodeHelper.byteToShort(new byte[]{message.get(2), message.getLast()}) + 6;
        }
        if(messageComplete()) {
            byte[] result = new byte[message.size()];
            int i = 0;
            for(byte b : message) {
                result[i] = b;
                i++;
            }
            // reset for next message
            message = new LinkedList<>();
            operation = NO_OP;
            packetSize = -1;
            isData = false;
            isWeird = false;
            return result;
        }
        return null;
    }

    public boolean messageComplete() {
        return (!isWeird) && (operation.TERMINATED && message.getLast().equals(TERMINATOR))
                || (message.size() == packetSize)
                || (isWeird && message.size() > weirdness && message.getLast().equals(TERMINATOR));
    }

    @Override
    public byte[] encode(byte[] message) {
        return message;
    }
}