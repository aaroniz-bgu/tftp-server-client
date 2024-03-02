package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.srv.BlockingConnectionHandler;

import java.io.IOException;
import java.net.Socket;

public class TftpConnectionHandler extends BlockingConnectionHandler<byte[]> {
    public TftpConnectionHandler(
            Socket sock,
            MessageEncoderDecoder<byte[]> reader,
            MessagingProtocol<byte[]> protocol) {
        super(sock, reader, protocol);
    }

    @Override
    public void close() throws IOException {
        super.close();

    }
}
