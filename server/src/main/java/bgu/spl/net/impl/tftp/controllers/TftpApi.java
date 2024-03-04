package bgu.spl.net.impl.tftp.controllers;

import bgu.spl.net.impl.tftp.packets.*;
import bgu.spl.net.impl.tftp.services.ITftpService;

import java.io.FileNotFoundException;
import java.util.ConcurrentModificationException;

import static bgu.spl.net.impl.tftp.GlobalConstants.DEFAULT_ACK;
import static bgu.spl.net.impl.tftp.GlobalConstants.ENCODING_FORMAT;
import static bgu.spl.net.impl.tftp.TftpErrorCodes.*;

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
            AcknowledgementPacket response = new AcknowledgementPacket(DEFAULT_ACK);
            response.setBroadcastPacket(new BroadcastPacket(false ,requestPacket.getFileName()));
            return response;
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
        try {
            ReadRequestPacket requestPacket = new ReadRequestPacket(request);
            byte[] firstBlock = service.readFile(requestPacket.getFileName());
            return new DataPacket((short) firstBlock.length, (short) 0, firstBlock);
        } catch (FileNotFoundException e) {
            return new ErrorPacket(FILE_NOT_FOUND.ERROR_CODE, e.getMessage());
        } catch (IllegalArgumentException | ConcurrentModificationException e) {
            return new ErrorPacket(ACCESS_VIOLATION.ERROR_CODE, e.getMessage());
        } catch (Exception e) {
            return new ErrorPacket(NOT_DEF.ERROR_CODE, e.getMessage());
        }
    }

    /**
     * Used to support continuous reading of a file.
     * @param request User's request.
     * @return {@link bgu.spl.net.impl.tftp.packets.DataPacket}'s containing file data or
     * {@link bgu.spl.net.impl.tftp.packets.ErrorPacket}.
     */
    public AbstractPacket readRequestContinue(byte[] request) {
        try {
            AcknowledgementPacket requestPacket = new AcknowledgementPacket(request);
            byte[] nextBlock = service.readFile((short) (requestPacket.getBlockNumber() + 1));
            return new DataPacket((short) nextBlock.length, (short) (requestPacket.getBlockNumber() + 1), nextBlock);
        } catch (FileNotFoundException e) {
            return new ErrorPacket(FILE_NOT_FOUND.ERROR_CODE, e.getMessage());
        } catch (IllegalArgumentException | ConcurrentModificationException e) {
            return new ErrorPacket(ACCESS_VIOLATION.ERROR_CODE, e.getMessage());
        } catch (Exception e) {
            return new ErrorPacket(NOT_DEF.ERROR_CODE, e.getMessage());
        }
    }

    /**
     * Upload file request, just a request to upload file not the writing yet.
     * @param request User's request.
     * @return {@link bgu.spl.net.impl.tftp.packets.AcknowledgementPacket} if possible to upload
     * and {@link bgu.spl.net.impl.tftp.packets.ErrorPacket} otherwise.
     */
    public AbstractPacket writeRequest(byte[] request) {
        try {
            WriteRequestPacket requestPacket = new WriteRequestPacket(request);
            if(service.writeRequest(requestPacket.getFileName())) {
                return new AcknowledgementPacket(DEFAULT_ACK);
            } else {
                return new ErrorPacket(FILE_ALREADY_EXISTS.ERROR_CODE, "File name exists.");
            }
        } catch (Exception e) {
            return new ErrorPacket(NOT_DEF.ERROR_CODE, e.getMessage());
        }
    }

    /**
     * Writes a file to the server, accepting data.
     * @param request User's request.
     * @return returns AcknowledgementPacket(+block) if succeeded saving and
     * {@link bgu.spl.net.impl.tftp.packets.ErrorPacket} if something went wrong.
     */
    public AbstractPacket writeData(byte[] request) {
        try {
            DataPacket requestPacket = new DataPacket(request);
            AcknowledgementPacket response = new AcknowledgementPacket(requestPacket.getBlockNumber());
            String filename = service.writeData(requestPacket.getData());
            if(filename != null) {
                response.setBroadcastPacket(new BroadcastPacket(true, filename));
            }
            return response;
        } catch (Exception e) {
            return new ErrorPacket(NOT_DEF.ERROR_CODE, e.getMessage());
        }
    }

    /**
     * Lists files in the server's directory.
     * @return {@link bgu.spl.net.impl.tftp.packets.DataPacket} containing the server's files and
     * {@link bgu.spl.net.impl.tftp.packets.ErrorPacket} if something went wrong.
     */
    public AbstractPacket listDirectoryRequest() {
        try {
            byte[] response = service.directoryRequest().getBytes(ENCODING_FORMAT);
            return new DataPacket((short) response.length, (short) 0, response);
        } catch (Exception e) {
            return new ErrorPacket(NOT_DEF.ERROR_CODE, e.getMessage());
        }
    }
}
