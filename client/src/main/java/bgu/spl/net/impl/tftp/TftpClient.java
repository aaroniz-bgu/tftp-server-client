package bgu.spl.net.impl.tftp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TftpClient {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("you must supply two arguments: host, port");
            System.exit(1);
        }

        ConcurrentLinkedQueue<String> requestQueue = new ConcurrentLinkedQueue<>();

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        // TODO wait somewhere
        Thread inputManager = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
                while (!Thread.currentThread().isInterrupted()) {
                    String line = in.readLine();
                    if (line != null) {
                        requestQueue.add(line);
                    }
                }
            } catch (IOException ex) {
            }
        });
    }
}
