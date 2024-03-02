package bgu.spl.net.impl.tftp.controllers;

import bgu.spl.net.impl.tftp.TftpConnections;
import bgu.spl.net.impl.tftp.packets.AbstractPacket;

import java.sql.Connection;

/**
 * Serializes data from the server and deserializes data from client.
 */
public class TftpAPI {

    private int connectionId;
    private TftpConnections connections;
    // private TftpServices service; <- this is an interface.

    /**
     * Creates a new API.
     * @param connectionId
     * @param connections
     */
    public TftpAPI(int connectionId, TftpConnections connections/*, TftpServices service*/) {
        this.connectionId = connectionId;
        this.connections  = connections;
    }

    /**
     * Logins a new user whether it's not logged in yet.
     * @param request User's request.
     * @return AcknowledgementPacket(block=0) if successful and ErrorPacket otherwise.
     */
    public AbstractPacket logRequest(byte[] request) {
        throw new UnsupportedOperationException("Yet to be implemented");
    }

    /**
     * Deletes a file from the server.
     * @param request User's request.
     * @return AcknowledgementPacket(block=0) if successful and ErrorPacket otherwise.
     */
    public AbstractPacket deleteRequest(byte[] request) {
        // TODO remember in the service check for dumb trials of user to get out of the folder.
        // Check for '/' or '\\' chars in the file name.
        throw new UnsupportedOperationException("Yet to be implemented");
    }

    /**
     * Download a file from the server.
     * @param request User's request.
     * @return DataPacket-s containing file data or ErrorPacket.
     */
    public AbstractPacket readRequest(byte[] request) {
        throw new UnsupportedOperationException("Yet to be implemented");
    }

    /**
     * Upload file request, just a request to upload file not the writing yet.
     * @param request User's request.
     * @return AcknowledgementPacket if possible to upload and ErrorPacket otherwise.
     */
    public AbstractPacket writeRequest(byte[] request) {
        throw new UnsupportedOperationException("Yet to be implemented");
    }

    /**
     * Writes a file to the server, accepting data.
     * @param request User's request.
     */
    public void writeData(byte[] request) {
        throw new UnsupportedOperationException("Yet to be implemented");
    }

    /**
     * Lists files in the server's directory.
     * @return DataPacket containing the server's files and ErrorPacket if something went wrong.
     */
    public AbstractPacket listDirectoryRequest() {
        throw new UnsupportedOperationException("Yet to be implemented");
    }

    /**
     * Disconnects user from the server.
     * @return AcknowledgementPacket or ErrorPacket.
     */
    public AbstractPacket disconnectRequest() {
        try {
            connections.disconnect(connectionId);
            connections = null; // To calm down the gc.
            //service = null // To really calm down the gc.
            //create AcknowledgementPacket and send it.
            return null;
        } catch (RuntimeException e) {
            //create an ErrorPacket and send it
            return null;
        }
    }

}
