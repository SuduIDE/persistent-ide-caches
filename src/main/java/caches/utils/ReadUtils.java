package caches.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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

    public static String readNSymbols(InputStream is, int n) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            try {
                stringBuilder.append(readOneUTF8(is));
            } catch (RuntimeException e) {
                throw new RuntimeException("InputStream hasn't " + n + " symbols", e);
            }
        }
        return stringBuilder.toString();
    }

    public static CharBuffer readOneUTF8(InputStream is) throws IOException {
        int first = is.read();
        byte[] bytes = new byte[4];
        bytes[0] = (byte) first;
        int len = 1;
        if (first == -1) {
            throw new RuntimeException("Expected UTF8 char, actual: EOF");
        }
        if ((first & (1 << 7)) != 0) {
            if ((first & (1 << 5)) != 0) {
                if ((first & (1 << 4)) != 0) {
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
        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes, 0, len));

    }
}
