package bgu.spl.net.impl.tftp.services;

import bgu.spl.net.impl.tftp.packets.AbstractPacket;
import bgu.spl.net.impl.tftp.packets.LoginRequestPacket;

import java.util.ConcurrentModificationException;

/**
 * This interface represents the TFTP service.
 * The TFTP service is used to handle all the requests from the client after the request has been decoded.
 * The methods will implement the business logic of the TFTP protocol.
 * The service will return the appropriate data to the handler to be used to encode the response.
 * @apiNote The usernames are case-sensitive.
 */
public interface ITftpService {

    /**
     * Request to delete file from server
     * If the file does not exist, the user will be rejected. (error code 1)
     * Otherwise, the file will be deleted and all the users currently logged in should be notified with a
     * {@link bgu.spl.net.impl.tftp.packets.BroadcastPacket}
     * The {@link bgu.spl.net.impl.tftp.packets.BroadcastPacket} should contain the file name and whether it was added or deleted.
     * And will be contained in the {@link bgu.spl.net.impl.tftp.packets.AcknowledgementPacket} as an attached broadcast packet.
     * @param filename file to delete
     * @return {@link bgu.spl.net.impl.tftp.packets.AcknowledgementPacket} if successful
     * and {@link bgu.spl.net.impl.tftp.packets.ErrorPacket} otherwise. (with the correct error code and message)
     */
    public void deleteFile(String filename) throws ConcurrentModificationException;

    /**
     * Request to read file from server
     * @param filename
     * @return byte array of the file
     * @throws IllegalArgumentException if the file does not exist.
     */
    public byte[] readRequest(String filename) throws IllegalArgumentException;

    /**
     * Request to write file to server
     * If the file already exists, the user will be rejected. (error code 5)
     * @param filename file to write
     * @return {@link bgu.spl.net.impl.tftp.packets.AcknowledgementPacket} if file doesn't exist and user can write to it.
     * and {@link bgu.spl.net.impl.tftp.packets.ErrorPacket} otherwise. (with the correct error code and message)
     */
    public boolean writeRequest(String filename);

    //TODO update exception type if needed
    /**
     * Data to write to file
     * Make sure to broadcast to all users the file was added after the complete write was successful.
     * Assumes the service already knows which user is writing the file and the file name.
     * If the write was incomplete, the file should be deleted without broadcasting to all the users.
     * @param data data to write
     * @return the block number of the data that was written.
     * @exception Exception if some sort of error occurred while writing the data. Otherwise, assume write was successful.
     */
    public short writeData(byte[] data) throws Exception;

    /**
     * Request to list all the files in the server
     * @return byte array of all the files in the server
     * @exception Exception if some sort of error occurred while listing the files.
     */
    public byte[] directoryRequest() throws Exception;
}
