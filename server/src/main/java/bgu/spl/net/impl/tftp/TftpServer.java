package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.srv.BaseServer;
import bgu.spl.net.srv.BlockingConnectionHandler;

import java.util.function.Supplier;

/**
 * Uses the thread-per-client model as specified in the SRS.
 * This server handles messages by serializing packets into byte streams.
 */
public class TftpServer extends BaseServer<byte[]> {
    public TftpServer(int port) {
        super(port, TftpProtocol::new, TftpEncoderDecoder::new);
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
