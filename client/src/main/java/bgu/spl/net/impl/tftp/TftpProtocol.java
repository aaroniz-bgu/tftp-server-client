package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessagingProtocol;

public class TftpProtocol implements MessagingProtocol<byte[]> {
    @Override
    public byte[] process(byte[] msg) {
        return null;
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }
}

