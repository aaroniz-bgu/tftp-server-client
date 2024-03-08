package bgu.spl.net.impl.tftp.services;

import java.util.*;

/**
 * Maps files to number of reading connections.
 */
public class ConcurrencyHelper {

    /**
     * Concurrent singleton. <br/>
     * Usually it's better to create one thread that accesses files in the system and handles them.
     * However, this will do in the current scenario. For more information read:
     * <a href="https://stackoverflow.com/a/55031179/19275130">this.</a>
     */
    private static class ConcurrencyHelperHolder {
        private static final ConcurrencyHelper instance = new ConcurrencyHelper();
    }

    /**
     * Files being currently read, cannot be deleted;
     */
    private final Map<String, Integer> filesToReaders;

    /**
     * Files currently being deleted, cannot be read.
     */
    private final List<String> beingDeleted;
    private final List<String> beingWritten;

    private ConcurrencyHelper() {
        filesToReaders = new HashMap<>();
        beingDeleted = new Vector<>();
        beingWritten = new Vector<>();
    }

    public static ConcurrencyHelper getInstance() {
        return ConcurrencyHelperHolder.instance;
    }

    /**
     * Flags the file as being read.
     * @param file The file's name as it is found in the server's directory.
     * @throws ConcurrentModificationException If the file currently being deleted.
     */
    public synchronized void read(String file) throws ConcurrentModificationException {
        if(beingDeleted.contains(file) || beingWritten.contains(file)) {
            throw new ConcurrentModificationException("File cannot be read.");
        }

        Integer readers = filesToReaders.putIfAbsent(file, 1);
        if(readers != null) {
            filesToReaders.replace(file, readers + 1);
        }
    }

    /**
     * Frees the file for modification if no one else is currently reading this file as well.
     * @param file The file's name as it is found in the server's directory.
     */
    public synchronized void free(String file) {
        Integer readers = filesToReaders.computeIfPresent(file, (k, v) -> v - 1);
        if(readers != null && readers == 0) {
            filesToReaders.remove(file);
        }
    }

    /**
     * Flags the file as being deleted.
     * @param file The file's name as it is found in the server's directory.
     * @throws ConcurrentModificationException If the file currently being read.
     */
    public synchronized void delete(String file) throws ConcurrentModificationException {
        if(filesToReaders.containsKey(file) || beingWritten.contains(file) || beingDeleted.contains(file)) {
            throw new ConcurrentModificationException("File cannot be deleted.");
        }

        beingDeleted.add(file);
    }

    /**
     * Flags the file as being written.
     * @param file The file's name as it is found in the server's directory.
     * @throws ConcurrentModificationException If the file currently being written.
     */
    public synchronized void write(String file) throws ConcurrentModificationException {
        if(filesToReaders.containsKey(file) || beingDeleted.contains(file) || beingWritten.contains(file)) {
            throw new ConcurrentModificationException("File cannot be written.");
        }
        beingWritten.add(file);
    }

    /**
     * Deletion was completed.
     * @param file File
     */
    public void deletionCompleted(String file) {
        if(!beingDeleted.contains(file)) {
            throw new NoSuchElementException();
        }

        beingDeleted.remove(file);
    }

    /**
     * Writing was completed.
     * @param file File
     */
    public void writeCompleted(String file) {
        if(!beingWritten.contains(file)) {
            throw new NoSuchElementException();
        }
        beingWritten.remove(file);
    }

    /**
     * Checks if the file is currently being written.
     * @param file File
     * @return True if the file is currently being written, false otherwise.
     */
    public boolean isBeingWritten(String file) {
        return beingWritten.contains(file);
    }
}
