package bgu.spl.net;

import bgu.spl.net.impl.tftp.TftpConnections;
import bgu.spl.net.impl.tftp.TftpServer;
import bgu.spl.net.srv.BaseServer;
import bgu.spl.net.srv.Server;

import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;

public class TftpStartup {

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
}
