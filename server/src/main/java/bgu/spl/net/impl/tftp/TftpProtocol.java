package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.impl.tftp.controllers.TftpApi;
import bgu.spl.net.impl.tftp.packets.AbstractPacket;
import bgu.spl.net.srv.Connections;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private TftpApi controller;

    private int connectionId;
    private Connections<byte[]> connections;

    private boolean terminate = false;

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
        short opCode = EncodeDecodeHelper.byteToShort(new byte[]{message[0], message[1]});
        Operation op = Operation.OPS[opCode];

        AbstractPacket controllerResponse = mapMessageToApi(controller, op, message);

        if(controllerResponse != null) {
            AbstractPacket broadcast = controllerResponse.getBroadcastPacket();
            if(broadcast != null) {
                // TODO.
            }
        }

        if(op == Operation.DISC) {
            disconnect();
        } else {

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
            case LOGRQ:
                return controller.logRequest(request);
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
