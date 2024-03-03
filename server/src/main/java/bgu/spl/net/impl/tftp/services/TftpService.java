package bgu.spl.net.impl.tftp.services;

import bgu.spl.net.impl.tftp.packets.AbstractPacket;

import java.io.File;
import java.util.ConcurrentModificationException;

import static bgu.spl.net.impl.tftp.services.ServicesConstants.WORK_DIR;

public class TftpService implements ITftpService {

    /**
     * Checks whether the user is trying any funny business.
     * @param filename The file's name.
     * @return true if the file's name is illegal, false otherwise.
     */
    private static boolean isIllegalFileName(String filename) {
        return filename == null || filename.contains("\\") || filename.contains("/");
    }

    /**
     * Attempts to delete a file from the server.
     * @param filename File to delete.
     * @throws ConcurrentModificationException If the file is currently being read.
     * @throws IllegalArgumentException If the file name contains illegal characters.
     * @throws RuntimeException If the file deletion was not successful.
     */
    @Override
    public void deleteFile(String filename) throws
            ConcurrentModificationException,
            IllegalArgumentException,
            RuntimeException {
        if(isIllegalFileName(filename)) {
            throw new IllegalArgumentException("Illegal file name!");
        }
        ConcurrencyHelper.getInstance().delete(filename);
        if(!new File(WORK_DIR + filename).delete()) {
            throw new RuntimeException("Failed to delete file.");
        } else {
            ConcurrencyHelper.getInstance().deletionCompleted(filename);
        }
    }


    @Override
    public byte[] readRequest(String filename) throws IllegalArgumentException {
        if(isIllegalFileName(filename)) {
            throw new IllegalArgumentException("Illegal file name!");
        }

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
