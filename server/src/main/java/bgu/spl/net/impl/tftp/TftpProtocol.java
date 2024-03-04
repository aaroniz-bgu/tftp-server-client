package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.impl.tftp.controllers.TftpApi;
import bgu.spl.net.impl.tftp.packets.AbstractPacket;
import bgu.spl.net.impl.tftp.packets.AcknowledgementPacket;
import bgu.spl.net.impl.tftp.packets.ErrorPacket;
import bgu.spl.net.impl.tftp.packets.LoginRequestPacket;
import bgu.spl.net.srv.Connections;

import java.util.NoSuchElementException;

import static bgu.spl.net.impl.tftp.GlobalConstants.DEFAULT_ACK;
import static bgu.spl.net.impl.tftp.TftpErrorCodes.*;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private TftpApi controller;

    private int connectionId;
    private Connections<byte[]> connections;

    private boolean terminate = false;
    private boolean isLogged  = false;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.connectionId = connectionId;
        this.connections  = connections;

        // High coupling here buddy:
        controller = new TftpApi();
        // Check if anything else should be done here.
    }

    @Override
    public void process(byte[] message) {
        if(message.length < 2) {
            throw new RuntimeException("Message received without operation code");
        }

        // Retrieve op code:
        short opCode = EncodeDecodeHelper.byteToShort(new byte[]{message[0], message[1]});
        Operation op = Operation.OPS[opCode]; // todo if out of bounds send illegal op.

        // Check if we're logged before giving any service:
        if(!isLogged) {
            if(op == Operation.LOGRQ) {
                String username = new LoginRequestPacket(message).getUserName();
                login(username);
            } else {
                connections.send(connectionId, new ErrorPacket(USER_NOT_LOGGED.ERROR_CODE,
                                "Unauthenticated user cannot perform actions, please login")
                                .getBytes());
            }
            return;
            // Check whether the request is to disconnect.
        } else if(op == Operation.DISC) {
            disconnect();
        }

        // Call the API and get response:
        AbstractPacket controllerResponse = mapMessageToApi(controller, op, message);

        // Handle response:
        if(controllerResponse != null) {
            // Check if file was added:
            AbstractPacket broadcast = controllerResponse.getBroadcastPacket();
            if(broadcast != null) {
                byte[] broadcastBytes = broadcast.getBytes();
                connections.broadcast(broadcastBytes);
            }

            // Respond to the user.
            connections.send(connectionId, controllerResponse.getBytes());
        } else {
            connections.send(connectionId, new ErrorPacket(ILLEGAL_OPERATION.ERROR_CODE,
                    "Operation " + opCode + " isn't supported.")
                    .getBytes());
        }
    }

    /**
     * Disconnects the connection.
     */
    private void disconnect() {
        try {
            connections.disconnect(connectionId);
            terminate = true;
            isLogged  = false;
            connections.send(connectionId, new AcknowledgementPacket(DEFAULT_ACK).getBytes());
        } catch (RuntimeException e) {
            connections.send(connectionId, new ErrorPacket(NOT_DEF.ERROR_CODE, e.getMessage()).getBytes());
        }
    }

    /**
     * Logs in connection so it could use the server.
     * @param username The username the connections wants to use.
     */
    private void login(String username) {
        try {
            connections.subscribe(connectionId, username);
            connections.send(connectionId, new AcknowledgementPacket(DEFAULT_ACK).getBytes());
            isLogged = true;
        } catch (SecurityException e) {
            //we need a logger.
            connections.send(connectionId, new ErrorPacket(USER_ALREADY_LOGGED.ERROR_CODE, e.getMessage()).getBytes());
            isLogged = false;
        } catch (NoSuchElementException e) {
            //log that this connection doesn't exist.
        }
    }

    /**
     * Maps requests to the appropriate end-point.
     * @param controller The controller that this user uses.
     * @param opCode The operation/service user is requesting.
     * @param request The request contents.
     * @return The APIs response.
     */
    private static AbstractPacket mapMessageToApi(TftpApi controller, Operation opCode, byte[] request) {
        switch(opCode) {
            case RRQ:
                return controller.readRequest(request);
            case WRQ:
                return controller.writeRequest(request);
            case DATA:
                return controller.writeData(request);
            case DIRQ:
                return controller.listDirectoryRequest();
            case DELRQ:
                return controller.deleteRequest(request);
            default:
                return null;
        }
    }

    @Override
    public boolean shouldTerminate() {
        return terminate;
    }
}
