package bgu.spl.net.impl.tftp;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Global variables that the contracts use.
 */
public class GlobalConstants {
    /**
     * Max size of data in bytes the data-packet is allowed to transmit per packet.
     */
    public static final short MAX_DATA_PACKET_SIZE = 512;
    /**
     * The default encoding format to use within this project.
     */
    public  static final Charset ENCODING_FORMAT = StandardCharsets.UTF_8;
    /**
     * Asta-la-vista baby!
     * Termination character for non-constant length packets.
     */
    public static final byte TERMINATOR = 0;

    /**
     * Saves data about operations that are terminated by a delimiter.
     */
    public static final short[] IS_OP_TERMINATED = {1,1,0,0,1,0,1,1,1,0};

    public static boolean isOpTerminated(short opcode) {
        return IS_OP_TERMINATED[opcode] == 1;
    }
}
