package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class TftpConnections implements Connections<byte[]> {
    private final Map<Integer, ConnectionHandler<byte[]>> connections;

    public TftpConnections() {
        connections = new HashMap<>();
    }

    @Override
    public void connect(int connectionId, ConnectionHandler<byte[]> handler) {
        if(connections.containsKey(connectionId)) {
            throw new KeyAlreadyExistsException("Connection ID " + connectionId + " is already in use.");
        }
        connections.put(connectionId, handler);
        // return true;
        // FIXME https://moodle.bgu.ac.il/moodle/mod/forum/discuss.php?d=703060#p1061585
    }

    @Override
    public boolean send(int connectionId, byte[] msg) {
        ConnectionHandler<byte[]> connection = connections.get(connectionId);
        if(connection == null) {
            throw new NoSuchElementException("No connection with the ID " + connectionId + " exists");
        }
        connection.send(msg);
        return true;
    }

    @Override
    public void disconnect(int connectionId) {
        try {
            connections.get(connectionId).close();
            connections.remove(connectionId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
