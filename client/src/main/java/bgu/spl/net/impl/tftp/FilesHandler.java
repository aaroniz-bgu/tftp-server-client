package bgu.spl.net.impl.tftp;

import java.io.*;

import static bgu.spl.net.impl.tftp.GlobalConstants.MAX_DATA_PACKET_SIZE;
import static bgu.spl.net.impl.tftp.GlobalConstants.WORK_DIR;

public class FilesHandler {
    /**
     * The name of the file being read, written or deleted.
     * @Exception IllegalArgumentException If the file name is empty or contains a null character.
     */
    private String fileName;
    public FilesHandler(String fileName) throws IllegalArgumentException{
        if (fileName.length() == 0 || fileName.contains("\0")) {
            throw new IllegalArgumentException("File name cannot contain null character or be empty");
        }
        this.fileName = fileName;
    }
    public String getFileName() {
        return fileName;
    }

    /**
     * Reads a block of data from the client's file.
     * If the file is done reading, the file name will be set to null.
     * @param block The block to read.
     * @return The block of data. If the file is done reading, an empty array will be returned.
     * @throws RuntimeException If the file is not found or an I/O error occurred.
     */
    public byte[] ReadFile(int block) throws RuntimeException {
        try {
            InputStream stream = new FileInputStream(new File(WORK_DIR + fileName));
            long skipBytes = block * MAX_DATA_PACKET_SIZE;
            stream.skip(skipBytes);
            byte[] output = new byte[MAX_DATA_PACKET_SIZE];
            int read = stream.read(output);
            stream.close();
            if(read == -1) {
                return new byte[0];
            } else if (read < MAX_DATA_PACKET_SIZE) {
                fileName = null;
                byte[] trimmed = new byte[read];
                System.arraycopy(output, 0, trimmed, 0, read);
                return trimmed;
            }
            return output;
        } catch (FileNotFoundException e) {
            fileName = null;
            throw new RuntimeException(e);
        } catch (IOException e) {
            fileName = null;
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes a block of data to the client's file.
     * If the file is done writing, the file name will be set to null.
     * @param data The data to write.
     * @throws RuntimeException If the file is not found or an I/O error occurred.
     * @throws IllegalStateException If no file is currently being written to.
     */
    public void WriteData(byte[] data) throws RuntimeException {
        if (fileName == null) {
            throw new IllegalStateException("No file currently being written to.");
        }
        try {
            File file = new File(WORK_DIR + fileName);
            OutputStream stream = new FileOutputStream(file, true);
            stream.write(data, 0, data.length);
            stream.close();
        } catch (FileNotFoundException e) {
            fileName = null;
            throw new RuntimeException(e);
        } catch (IOException e) {
            fileName = null;
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes the client's file.
     * @throws RuntimeException If the file is not found or an I/O error occurred.
     */
    public void DeleteFile() throws RuntimeException {
        if (fileName == null) {
            throw new IllegalStateException("No file currently being deleted.");
        }
        try {
            if (!new File(WORK_DIR + fileName).delete()) {
                throw new RuntimeException("Failed to delete file.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            fileName = null;
        }
    }

    /**
     * Creates a new file with the file name.
     * @return True if the file was created, false otherwise.
     * @throws IOException an I/O error occurred.
     * @throws SecurityException a security error occurred.
     */
    public boolean createNewFile() throws IOException, SecurityException {
        return new File(WORK_DIR + fileName).createNewFile();
    }
}
