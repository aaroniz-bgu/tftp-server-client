package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.srv.BaseServer;
import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.Connections;

import javax.swing.*;
import java.util.MissingFormatArgumentException;
import java.util.function.Supplier;

/**
 * Uses the thread-per-client model as specified in the SRS.
 * This server handles messages by serializing packets into byte streams.
 */
public class TftpServer extends BaseServer<byte[]> {

    public static void main(String[] args) {
        if(args.length == 0) {
            throw new MissingFormatArgumentException("Missing port.");
        }
        int port = Integer.parseInt(args[0]);
        try {
            System.out.println("Starting server...");
            BaseServer<byte[]> server = new TftpServer(port);
            server.serve();
            server.close();
        } catch(Exception e) {
            System.out.println("Error occurred while running the server:\n");
            e.printStackTrace();
        }
        System.out.println("Server was forced to shut down.");
    }

    public TftpServer(int port) {
        super(port, TftpProtocol::new, TftpEncoderDecoder::new, new TftpConnections());
    }

    /**
     * Using the thread-per-client model.
     * Change in the future when you'll use this in the portfolio.
     * @param handler the connection handler created by {@link BaseServer}.serve().
     */
    @Override
    protected void execute(BlockingConnectionHandler<byte[]> handler) {
        new Thread(handler).start();
    }
}
