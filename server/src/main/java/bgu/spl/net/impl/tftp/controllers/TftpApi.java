package bgu.spl.net.impl.tftp.controllers;

import bgu.spl.net.impl.tftp.TftpConnections;
import bgu.spl.net.impl.tftp.packets.AbstractPacket;

/**
 * Serializes data from the server and deserializes data from client.
 */
public class TftpApi {

    /**
     * Deletes a file from the server.
     * Then sends to all users that are logged in a broadcast packet that the file was deleted.
     * @param request User's request.
     * @return {@link bgu.spl.net.impl.tftp.packets.AcknowledgementPacket}(block=0) if successful
     * and {@link bgu.spl.net.impl.tftp.packets.ErrorPacket} otherwise.
     */
    public AbstractPacket deleteRequest(byte[] request) {
        // TODO remember in the service check for dumb trials of user to get out of the folder.
        // Check for '/' or '\\' chars in the file name.
        throw new UnsupportedOperationException("Yet to be implemented");
    }

    /**
     * Download a file from the server.
     * @param request User's request.
     * @return {@link bgu.spl.net.impl.tftp.packets.DataPacket}'s containing file data or
     * {@link bgu.spl.net.impl.tftp.packets.ErrorPacket}.
     */
    public AbstractPacket readRequest(byte[] request) {
        throw new UnsupportedOperationException("Yet to be implemented");
    }

    /**
     * Upload file request, just a request to upload file not the writing yet.
     * @param request User's request.
     * @return {@link bgu.spl.net.impl.tftp.packets.AcknowledgementPacket} if possible to upload
     * and {@link bgu.spl.net.impl.tftp.packets.ErrorPacket} otherwise.
     */
    public AbstractPacket writeRequest(byte[] request) {
        throw new UnsupportedOperationException("Yet to be implemented");
    }

    /**
     * Writes a file to the server, accepting data.
     * @param request User's request.
     * @return returns AcknowledgementPacket(+block) if succeeded saving and @link bgu.spl.net.impl.tftp.packets.ErrorPacket} if something went wrong.
     */
    public AbstractPacket writeData(byte[] request) {
        throw new UnsupportedOperationException("Yet to be implemented");
    }

    /**
     * Lists files in the server's directory.
     * @return {@link bgu.spl.net.impl.tftp.packets.DataPacket} containing the server's files and
     * {@link bgu.spl.net.impl.tftp.packets.ErrorPacket} if something went wrong.
     */
    public AbstractPacket listDirectoryRequest() {
        throw new UnsupportedOperationException("Yet to be implemented");
    }
  
}
