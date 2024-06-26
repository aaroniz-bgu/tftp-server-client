package bgu.spl.net.srv;

import java.io.IOException;

public interface Connections<T> {

    void connect(int connectionId, ConnectionHandler<T> handler);

    boolean send(int connectionId, T msg);

    void disconnect(int connectionId);

    // Added observer design pattern for broadcasting

    /**
     * Subscribes to the listeners.
     * @param connectionId The connection id to be subscribed.
     */
    void subscribe(int connectionId, String username) throws SecurityException; // this might be the wrong type of ex.

    /**
     * Usually called notify(), to broadcast messages easier.
     * @param broadcastMessage Message to send.
     */
    void broadcast(T broadcastMessage); // TODO MAYBE ADD CONNECTION HANDLER TO NOT BROADCAST TO SELF
}
