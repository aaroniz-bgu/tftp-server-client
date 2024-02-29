package bgu.spl.net.impl.tftp.packets;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Global variables that the contracts use.
 */
public class GlobalConstants {
    /**
     * Max size of data in bytes the data-packet is allowed to transmit per packet.
     */
    protected static final short MAX_DATA_PACKET_SIZE = 512;
    /**
     * The default encoding format to use within this project.
     */
    protected  static final Charset ENCODING_FORMAT = StandardCharsets.UTF_8;
    /**
     * Asta-la-vista baby!
     * Termination character for non-constant length packets.
     */
    protected static final byte  TERMINATOR = 0;
}
