package bgu.spl.net.impl.tftp;

import java.io.*;
import java.net.Socket;

import static bgu.spl.net.impl.tftp.DisplayMessage.print;

public class TftpClient {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("you must supply two arguments: host, port");
            System.exit(1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        print("Starting client...");
        print("Initiating resources...");

        TftpEncoderDecoder encdec = new TftpEncoderDecoder();
        TftpProtocol protocol = new TftpProtocol(encdec);
        CliInterface inter = new CliInterface(protocol.getCoordinator());
        Thread interfaceThread = new Thread(inter, "Interface-Thread");

        print("Attempting to connect to server");

        try (Socket sock = new Socket(host, port);
             InputStream in = sock.getInputStream();
             OutputStream out = sock.getOutputStream()
        ) {
            protocol.setOutputStream(out);

            print("Starting client...");

            interfaceThread.start();

            print("Client is ready!");
            while (!Thread.currentThread().isInterrupted() && !protocol.shouldTerminate()) {
                // If the server has sent us a message:
                byte[] msg = encdec.decodeNextByte((byte) in.read());
                if (msg != null) {
                    // Process the server's message:
                    byte[] response = protocol.process(msg);
                    // If the server waits for our response:
                    if (response != null) {
                        out.write(encdec.encode(response));
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
