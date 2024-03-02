package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.impl.tftp.controllers.TftpApi;
import bgu.spl.net.impl.tftp.packets.AbstractPacket;
import bgu.spl.net.srv.Connections;

import java.util.NoSuchElementException;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private TftpApi controller;

    private int connectionId;
    private Connections<byte[]> connections;

    private boolean terminate = false;
    private boolean isLogged = false;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        this.connectionId = connectionId;
        this.connections  = connections;

        // High coupling here buddy:
        controller = new TftpApi();
        // Check if anything else should be done here.
    }

    @Override
    public void process(byte[] message) { // TODO Subscribe to broadcast once logged.
        if(message.length < 2) {
            throw new RuntimeException("Message received without operation code");
        }

        // Retrieve op code:
        short opCode = EncodeDecodeHelper.byteToShort(new byte[]{message[0], message[1]});
        Operation op = Operation.OPS[opCode];

        // Check if we're logged before giving any service:
        if(!isLogged) {
            if(op == Operation.LOGRQ) {
                login("TODO"); // TODO Retrieve username from message.
            }
            return;
        }

        // Call the API and get response:
        AbstractPacket controllerResponse = mapMessageToApi(controller, op, message);

        // Handle response:
        if(controllerResponse != null) {
            AbstractPacket broadcast = controllerResponse.getBroadcastPacket();
            if(broadcast != null) {
                byte[] broadcastBytes = broadcast.getBytes();
                connections.broadcast(broadcastBytes);
            }
            connections.send(connectionId, controllerResponse.getBytes());
        }

        if(op == Operation.DISC) {
            disconnect();
        }
        // send message.
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }

    private void disconnect() {
        try {
            connections.disconnect(connectionId);
            terminate = true;
            //create AcknowledgementPacket and send it.
        } catch (RuntimeException e) {
            //create an ErrorPacket and send it
        }
    }

    /**
     * Logs in connection so it could use the server.
     * @param username The username the connections wants to use.
     */
    private void login(String username) {
        try {
            connections.subscribe(connectionId, username);
            connections.send(connectionId, null /*TODO new AcknowledgementPacket(0).getBytes()*/);
            isLogged = true;
        } catch (SecurityException e) {
            //we need a logger.
            connections.send(connectionId, null /*TODO new ErrorPacket(e.getMessage()).getBytes()*/);
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
                controller.writeData(request);
                return null;
            case DIRQ:
                return controller.listDirectoryRequest();
            /*
            Unnecessary, now using TftpProtocol.login() for this.
            case LOGRQ:
                return controller.logRequest(request);
            */
            case DELRQ:
                return controller.deleteRequest(request);
            case DISC:
                return controller.disconnectRequest();
            default:
                // TODO Replace with ErrorPacket
                throw new UnsupportedOperationException("Unsupported request operation code: " + opCode);
        }
    }

    @Override
    public boolean shouldTerminate() {
        return terminate;
    }
}
