package bgu.spl.net.impl.tftp;

import bgu.spl.net.impl.tftp.packets.AbstractPacket;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.SynchronousQueue;

public class TftpClient {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("you must supply two arguments: host, port");
            System.exit(1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        TftpEncoderDecoder encdec = new TftpEncoderDecoder();
        TftpProtocol protocol = new TftpProtocol();
        CliInterface inter = new CliInterface(protocol.getCoordinator());
        Thread interfaceThread = new Thread(inter, "Interface-Thread");

        try (Socket sock = new Socket(host, port)) {

            interfaceThread.start();

            while (!Thread.currentThread().isInterrupted()) {
                // Check if the CLI made a request:
                AbstractPacket send = protocol.getCoordinator().getRequest();
                if(send != null) {
                    sock.getOutputStream().write(encdec.encode(send.getBytes()));
                }

                // If the server has sent us a message:
                byte[] msg = encdec.decodeNextByte((byte) sock.getInputStream().read());
                if(msg != null) {
                    // Process the server's message:
                    byte[] response = protocol.process(msg);
                    // If the server waits for our response:
                    if(response != null) {
                        sock.getOutputStream().write(encdec.encode(response));
                    }
                }
            }
            interfaceThread.interrupt();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }
}
