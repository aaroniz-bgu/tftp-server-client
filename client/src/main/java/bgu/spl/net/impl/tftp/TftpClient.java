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

        SynchronousQueue<String> queue = new SynchronousQueue<>();
        CliInterface inter = new CliInterface(queue);
        Thread interfaceThread = new Thread(inter, "Interface-Thread");

        Thread clientThread = new Thread(() -> {
            try (Socket sock = new Socket(host, port)) {
                TftpEncoderDecoder encdec = new TftpEncoderDecoder();
                TftpProtocol protocol = new TftpProtocol();

                interfaceThread.start();

                while (!Thread.currentThread().isInterrupted()) {
                    if(!queue.isEmpty()) {
                        AbstractPacket packet = null; //PacketFactory.createPacket(queue.poll()); TODO
                        if(packet != null) {
                            sock.getOutputStream().write(encdec.encode(packet.getBytes()));
                        }
                    }

                    byte[] msg = encdec.decodeNextByte((byte) sock.getInputStream().read());
                    if(msg != null) {
                        protocol.process(msg);
                    }
                }
                interfaceThread.interrupt();
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println(ex.getMessage());
            }
        }, "Listener-Thread");
    }
}
