package bgu.spl.net.impl.tftp.services;

import bgu.spl.net.impl.tftp.packets.AbstractPacket;
import bgu.spl.net.impl.tftp.packets.LoginRequestPacket;

/**
 * This interface represents the TFTP service.
 * The TFTP service is used to handle all the requests from the client after the request has been decoded.
 * The methods will implement the business logic of the TFTP protocol.
 * The service will return the appropriate data to the handler to be used to encode the response.
 * @apiNote The usernames are case-sensitive.
 */
public interface ITftpService {

    /**
     * User requesting to log in with specified username.
     * If the username is already logged in, the user will be rejected. (error code 7)
     * Otherwise, the user will be logged in.
     * @apiNote The usernames are case-sensitive.
     * @param username username user is trying to log in with.
     * @return {@link bgu.spl.net.impl.tftp.packets.AcknowledgementPacket} with block number 0 if successful
     * and {@link bgu.spl.net.impl.tftp.packets.ErrorPacket} otherwise. (with the correct error code and message)
     */
    public AbstractPacket login(String username);

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
    public AbstractPacket deleteFile(String filename);

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
    public AbstractPacket writeRequest(String filename);

    //TODO update exception type if needed
    /**
     * Data to write to file
     * Make sure to broadcast to all users the file was added after the complete write was successful.
     * If the write was incomplete, the file should be deleted without broadcasting to all the users using
     * {@link #deleteFileQuietly(String)}.
     * @param data data to write
     * @param fileName file to write to
     * @exception Exception if some sort of error occurred while writing the data. Otherwise, assume write was successful.
     */
    public void writeData(byte[] data, String fileName) throws Exception;

    /**
     * To be used in case an error occurred while writing the data.
     * The file should be deleted without broadcasting to all the users.
     * @param filename file to delete
     * @exception IllegalArgumentException if the file does not exist.
     * @exception Exception if some sort of error occurred while deleting the file.
     */
    public void deleteFileQuietly(String filename) throws IllegalArgumentException, Exception;

    /**
     * Request to list all the files in the server
     * @return byte array of all the files in the server
     * @exception Exception if some sort of error occurred while listing the files.
     */
    public byte[] directoryRequest() throws Exception;

    /**
     * Request to disconnect from the server
     * @return {@link bgu.spl.net.impl.tftp.packets.AcknowledgementPacket} if successful
     * and {@link bgu.spl.net.impl.tftp.packets.ErrorPacket} otherwise. (with the correct error code and message)
     */
    public AbstractPacket disconnect();
}
