package bgu.spl.net.impl.tftp.services;

import bgu.spl.net.impl.tftp.packets.AbstractPacket;

public class TftpService implements ITftpService {
    @Override
    public AbstractPacket deleteFile(String filename) {
        return null;
    }

    @Override
    public byte[] readRequest(String filename) throws IllegalArgumentException {
        return new byte[0];
    }

    @Override
    public AbstractPacket writeRequest(String filename) {
        return null;
    }

    @Override
    public short writeData(byte[] data) throws Exception {
        return 0;
    }

    @Override
    public byte[] directoryRequest() throws Exception {
        return new byte[0];
    }
}
