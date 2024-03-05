package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessagingProtocol;

public class TftpProtocol<T> implements MessagingProtocol<T> {
    @Override
    public T process(T msg) {
        return null;
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }
}
