package bgu.spl.net.impl.tftp;

public enum TftpErrorCodes {
    NOT_DEF((short) 0), FILE_NOT_FOUND((short) 1),
    ACCESS_VIOLATION((short) 2), FULL_DISK((short) 3),
    ILLEGAL_OPERATION((short) 4), FILE_ALREADY_EXISTS((short) 5),
    USER_NOT_LOGGED((short) 6), USER_ALREADY_LOGGED((short) 7);

    public final short ERROR_CODE;
    TftpErrorCodes(short errCode) {
        ERROR_CODE = errCode;
    }

}
