package bgu.spl.net.impl.tftp.services;

public class TftpService implements ITftpService {



    @Override
    public void deleteFile(String filename) {
    }

    @Override
    public byte[] readRequest(String filename) throws IllegalArgumentException {
        return new byte[0];
    }

    @Override
    public boolean writeRequest(String filename) {
        return false;
    }

    @Override
    public short writeData(byte[] data) throws Exception {
        return 0;
    }

    @Override
    public String directoryRequest() throws Exception {
        return null;
    }
}
