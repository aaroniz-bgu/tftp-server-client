package bgu.spl.net.impl.tftp.services;

public class TftpService implements ITftpService {



    @Override
    public void deleteFile(String filename) {
        return null;
    }

    @Override
    public byte[] readRequest(String filename) throws IllegalArgumentException {
        return new byte[0];
    }

    @Override
    public boolean writeRequest(String filename) {
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
