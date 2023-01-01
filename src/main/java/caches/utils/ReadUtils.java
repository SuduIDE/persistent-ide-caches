package caches.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ReadUtils {

    public static int readInt(InputStream is) throws IOException {
        return ByteBuffer.wrap(is.readNBytes(Integer.BYTES)).getInt();
    }

    public static short readShort(InputStream is) throws IOException {
        return ByteBuffer.wrap(is.readNBytes(Short.BYTES)).getShort();
    }

    public static String readUTF(InputStream is) throws IOException {
        var bytes = is.readNBytes(readShort(is));
        return new String(bytes);
    }

    public static char readOneUTF8(InputStream is) throws IOException {
        int first = is.read();
        byte[] bytes = new byte[4];
        bytes[0] = (byte) first;
        int len = 1;
        if (first == -1) {
            throw new RuntimeException("Expected UTF8 char, actual: EOF");
        } else if ((first & (1 << 7)) != 0) {
            if ((first & (1 << 6)) != 0) {
                if ((first & (1 << 5)) != 0) {
                    len = 4;
                } else {
                    len = 3;
                }
            } else {
                len = 2;
            }
        }
        if (len != 1) {
            int read = is.read(bytes, 1, len - 1);
            if (read != len - 1) {
                throw new RuntimeException("Expected UTF8 char, actual: EOF");
            }
        }
        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes, 0, len)).get();

    }
}
