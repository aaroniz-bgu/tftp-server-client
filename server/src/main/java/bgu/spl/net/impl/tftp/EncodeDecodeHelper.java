package bgu.spl.net.impl.tftp;

public class EncodeDecodeHelper {

    /**
     * Converts a short to a byte array.
     * @param convert a short to convert.
     * @return a byte array containing the short data.
     */
    public static byte[] shortToByte(short convert) {
        return new byte[]{(byte) ((convert >> 8) & 0xff), (byte) (convert & 0xff)};
    }

    /**
     * Converts a byte array to a short.
     * @param bytes 2 length byte array.
     * @return a short equal to the input.
     * @throws IllegalArgumentException if the byte array does not contain exactly 2 entries.
     */
    public static short byteToShort(byte[] bytes) {
        if(bytes.length != 2) {
            throw new IllegalArgumentException("Cannot convert into a short, contains more/less than 2 bytes.");
        }
        return (short) ((short) ((bytes[0] & 0xff) << 8) | (short) (bytes[1] & 0xff));
    }
}
