package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.Operation;

public class BroadcastPacket extends AbstractPacket {
    public BroadcastPacket() {
        super(Operation.BCAST.OP_CODE);
    }

    public BroadcastPacket(byte[] packet) {
        super(Operation.BCAST.OP_CODE);
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }
}
