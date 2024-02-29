package bgu.spl.net.impl.tftp;

public enum Operation {
    NO_OP((short) 0,false),
    RRQ((short) 1, true),
    WRQ((short) 2, true),
    DATA((short) 3, false),
    ACK((short) 4, false),
    ERROR((short) 5, false),
    DIRQ((short) 6, true),
    LOGRQ((short) 7, false),
    DELRQ((short) 8, true),
    BCAST((short) 9, true),
    DISC((short) 10, false);

    public static final Operation[] OPS = {NO_OP, RRQ, WRQ, DATA, ACK, ERROR, DIRQ, LOGRQ, DELRQ, BCAST, DISC};

    public final boolean TERMINATED;
    public final short OP_CODE;
    Operation(short opCode, boolean terminated) {
        TERMINATED = terminated;
        OP_CODE = opCode;
    }
}
