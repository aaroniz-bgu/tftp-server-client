package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;


public class TftpConnections implements Connections<byte[]> {
    private final Map<Integer, ConnectionHandler<byte[]>> connections;
    private final ArrayList<Integer> listeners;
    public TftpConnections() {
        connections = new HashMap<>();
        listeners = new ArrayList<>();
    }

    @Override
    public synchronized void connect(int connectionId, ConnectionHandler<byte[]> handler) {
        if(connections.containsKey(connectionId)) {
            throw new KeyAlreadyExistsException("Connection ID " + connectionId + " is already in use.");
        }
        connections.put(connectionId, handler);
        // return true;
        // FIXME if https://moodle.bgu.ac.il/moodle/mod/forum/discuss.php?d=703060#p1061585
    }

    @Override
    public synchronized boolean send(int connectionId, byte[] msg) {
        ConnectionHandler<byte[]> connection = connections.get(connectionId);
        if(connection == null) {
            throw new NoSuchElementException("No connection with the ID " + connectionId + " exists");
        }
        connection.send(msg);
        return true;
    }

    @Override
    public synchronized void disconnect(int connectionId) {
        try {
            connections.get(connectionId).close();
            connections.remove(connectionId);
            listeners.remove(Integer.valueOf(connectionId));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void subscribe(int connectionId) {
        if(!connections.containsKey(connectionId)) {
            throw new NoSuchElementException("No connection exists with ID: " + connectionId);
        }
        listeners.add(connectionId);
    }

    @Override
    public synchronized void broadcast(byte[] broadcastMessage) {
        for(int listenerId : listeners) {
            connections.get(listenerId).send(broadcastMessage);
        }
    }
}
