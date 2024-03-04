package bgu.spl.net.impl.tftp.controllers;

import bgu.spl.net.impl.tftp.packets.*;
import bgu.spl.net.impl.tftp.services.ITftpService;

import java.util.ConcurrentModificationException;

import static bgu.spl.net.impl.tftp.GlobalConstants.DEFAULT_ACK;
import static bgu.spl.net.impl.tftp.TftpErrorCodes.ACCESS_VIOLATION;
import static bgu.spl.net.impl.tftp.TftpErrorCodes.NOT_DEF;

/**
 * Serializes data from the server and deserializes data from client.
 */
public class TftpApi {

    private final ITftpService service;

    public TftpApi(ITftpService service) {
        this.service = service;
    }

    /**
     * Deletes a file from the server.
     * Then sends to all users that are logged in a broadcast packet that the file was deleted.
     * @param request User's request.
     * @return {@link bgu.spl.net.impl.tftp.packets.AcknowledgementPacket}(block=0) if successful
     * and {@link bgu.spl.net.impl.tftp.packets.ErrorPacket} otherwise.
     */
    public AbstractPacket deleteRequest(byte[] request) {
        try {
            DeleteRequestPacket requestPacket = new DeleteRequestPacket(request);
            service.deleteFile(requestPacket.getFileName());
            AcknowledgementPacket output = new AcknowledgementPacket(DEFAULT_ACK);
            output.setBroadcastPacket(new BroadcastPacket(false ,requestPacket.getFileName()));
            return output;
        } catch (ConcurrentModificationException e) {
            return new ErrorPacket(ACCESS_VIOLATION.ERROR_CODE, e.getMessage());
        } catch (Exception e) {
            return new ErrorPacket(NOT_DEF.ERROR_CODE, e.getMessage());
        }
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
