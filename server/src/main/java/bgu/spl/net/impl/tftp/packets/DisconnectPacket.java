package bgu.spl.net.impl.tftp.packets;

import bgu.spl.net.impl.tftp.EncodeDecodeHelper;

public class DisconnectPacket extends AbstractPacket{
    private static final short OP_CODE = 10;

    public DisconnectPacket() {
        super(OP_CODE);
    }

    @Override
    public byte[] getBytes() {
        return EncodeDecodeHelper.shortToByte(opCode);
    }
}
