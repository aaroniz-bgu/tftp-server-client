package bgu.spl.net.impl.tftp.services;

import bgu.spl.net.impl.tftp.EncodeDecodeHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EncodeDecodeHelperTests {

    @Test
    void test() {
        for(byte i = 0; i < Byte.MAX_VALUE; i++)
            for(byte j = 0; j < Byte.MAX_VALUE; j++) {
                short actual = EncodeDecodeHelper.byteToShort(new byte[]{j,i});
                assertEquals(((short) j) * 128 + ((short) i), actual);
            }
    }
}
