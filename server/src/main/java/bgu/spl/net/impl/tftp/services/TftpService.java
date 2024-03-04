package bgu.spl.net.impl.tftp.services;


import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.util.ConcurrentModificationException;

import static bgu.spl.net.impl.tftp.GlobalConstants.MAX_DATA_PACKET_SIZE;
import static bgu.spl.net.impl.tftp.services.ServicesConstants.WORK_DIR;

/**
 * Implements the services in such manner where reading/writing operations will be called sequentially.
 * If reading/writing of a file would be interfered by another request may produce undefined behaviour.
 */
public class TftpService implements ITftpService {

    private String currentFileName;

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
    public void deleteFile(String filename) throws Exception {
        if(isIllegalFileName(filename)) {
            throw new IllegalArgumentException("Illegal file name!");
        }
        try {
            ConcurrencyHelper.getInstance().delete(filename);
            if (!new File(WORK_DIR + filename).delete()) {
                throw new RuntimeException("Failed to delete file.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ConcurrencyHelper.getInstance().deletionCompleted(filename);
        }
    }

    /**
     * Used to make continuous file reading easier.
     * Reads a file block and returns it, might return an array smaller than a block size or equals to 0.
     * @param block The current block of file being read.
     * @return A byte array containing the file's contents in the specified block. Might return a smaller
     * array then data-packet size if the block is the last of the file, if no bytes we're read due to the
     * request of a block which isn't found in the file or if there was an undetected IO error within the OS.
     * @throws ConcurrentModificationException If the file is currently being deleted.
     * @throws FileNotFoundException If the OS could not locate the file.
     * @throws IOException If there was some kind of IO faulty while reading the file.     * @throws Exception
     */
    private byte[] readFileHelper(short block) throws Exception{
        try {
            ConcurrencyHelper.getInstance().read(currentFileName);

            InputStream stream = new FileInputStream(new File(WORK_DIR + currentFileName));
            long skipBytes = block * MAX_DATA_PACKET_SIZE;
            stream.skip(skipBytes);

            byte[] output = new byte[MAX_DATA_PACKET_SIZE];
            int read = stream.read(output);
            stream.close();

            if(read == -1) {
                return new byte[0];
            } else if (read < MAX_DATA_PACKET_SIZE) {
                ConcurrencyHelper.getInstance().free(currentFileName);
                currentFileName = null;

                byte[] trimmed = new byte[read];
                System.arraycopy(output, 0, trimmed, 0, read);
                return trimmed;
            }
            return output;
            // We're not calling free in finally because the file may be still in reading progress.
        } catch (ConcurrentModificationException e) {
            ConcurrencyHelper.getInstance().free(currentFileName);
            currentFileName = null;
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            ConcurrencyHelper.getInstance().free(currentFileName);
            currentFileName = null;
            throw new RuntimeException(e);
        } catch (IOException e) {
            ConcurrencyHelper.getInstance().free(currentFileName);
            currentFileName = null;
            throw new RuntimeException(e);
        }
    }

    /**
     * Used in the first time we want to read a file.
     * Reads a file block and returns it, might return an array smaller than a block size or equals to 0.
     * @param filename The file's name in the server's working directory.
     * @return A byte array containing the file's contents in the specified block. Might return a smaller
     * array then data-packet size if the block is the last of the file, if no bytes we're read due to the
     * request of a block which isn't found in the file or if there was an undetected IO error within the OS.
     * @throws ConcurrentModificationException If the file is currently being deleted.
     * @throws IllegalArgumentException If the file's name is not allowed.
     * @throws FileNotFoundException If the OS could not locate the file.
     * @throws IOException If there was some kind of IO faulty while reading the file.
     */
    @Override
    public byte[] readFile(String filename) throws Exception {
        if(isIllegalFileName(filename)) {
            throw new IllegalArgumentException("Illegal file name!");
        }
        currentFileName = filename;
        return readFileHelper((short) 0);
    }

    /**
     * Used for continuous reading of a file after we already read the first block.
     * Note that if readFile(String, short) wasn't called before this may produce undefined behaviour.
     * @param block The current block of file being read.
     * @return A byte array containing the file's contents in the specified block. Might return a smaller
     * array then data-packet size if the block is the last of the file, if no bytes we're read due to the
     * request of a block which isn't found in the file or if there was an undetected IO error within the OS.
     * @throws ConcurrentModificationException If the file is currently being deleted.
     * @throws FileNotFoundException If the OS could not locate the file.
     * @throws IOException If there was some kind of IO faulty while reading the file.
     */
    public byte[] readFile(short block) throws Exception {
        return readFileHelper(block);
    }

    /**
     * Returns whether a file can be written or not to the server.
     * Will mark the file as being written if the file doesn't exist in the ConcurrencyHelper.
     *
     * @param filename file to write
     * @return True if no such already file exists.
     * @throws IOException Read createNewFile in {@link java.io.File}
     * @throws SecurityException Read createNewFile in {@link java.io.File}
     */
    @Override
    public boolean writeRequest(String filename) throws Exception {
        if (isIllegalFileName(filename)) {
            throw new IllegalArgumentException("Illegal file name!");
        }
        // Mark the file as being written before creating it
        ConcurrencyHelper.getInstance().write(filename);
        if (new File(WORK_DIR + filename).createNewFile()) {
            currentFileName = filename;
            return true;
        } else {
            // If file creation fails, mark the write operation as completed to avoid locking the file.
            // This shouldn't happen, but just in case.
            ConcurrencyHelper.getInstance().writeCompleted(filename);
            return false;
        }
    }


    /**
     * Writes/appends the data to the currently being written file.
     * Will release the file from being written if the data is less than the maximum data packet size
     * or an error occurs in the ConcurrencyHelper.
     *
     * @param data data to write
     * @return The file's name when writing is done.
     * @throws IllegalStateException If no file is currently being written, which means the service is in illegal
     * state.
     * @throws IOException Read write in {@link java.io.OutputStream}.
     */
    @Override
    public String writeData(byte[] data) throws Exception {
        if (currentFileName == null) {
            throw new IllegalStateException("No file is being written currently.");
        }
        File file = new File(WORK_DIR + currentFileName);
        OutputStream stream = new FileOutputStream(file, true);
        try {
            stream.write(data);
            stream.close();
            // Check if this is the last block of the file
            if (data.length < MAX_DATA_PACKET_SIZE) {
                // Mark the write operation as completed
                ConcurrencyHelper.getInstance().writeCompleted(currentFileName);
                String output = currentFileName;
                currentFileName = null; // Reset currentFileName as the write operation is completed
                return output;
            }
        } catch (IOException e) {
            // Handle IOException
            ConcurrencyHelper.getInstance().writeCompleted(currentFileName);
            currentFileName = null;
            stream.close();
            throw e;
        }
        return null;
    }

    /**
     * Lists all the files in the server's directory.
     * Will list them in the following format:
     * (file name 1)\n
     * (file name 2)\n
     * ⋯
     * (file name n)\n
     * Will exclude files that are still being created.
     * @return All the files in the server that are not still being created.
     * @throws IOException If some sort of error occurred while listing the files.
     */
    @Override
    public String directoryRequest() throws Exception {
        File directory = new File(WORK_DIR);
        // Initialize empty list of files
        StringBuilder fileList = new StringBuilder();
        // List all files in the directory
        String[] files = directory.list();
        if (files == null) {
            throw new IOException("Directory does not exist or an I/O error occurred");
        }
        ConcurrencyHelper helper = ConcurrencyHelper.getInstance();
        for (String file : files) {
            if (!helper.isBeingWritten(file)) {
                fileList.append(file).append("\n");
            }
        }
        return fileList.toString();
    }
}
