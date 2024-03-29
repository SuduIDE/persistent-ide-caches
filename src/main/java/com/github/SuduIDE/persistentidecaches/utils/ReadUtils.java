package com.github.SuduIDE.persistentidecaches.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

public class ReadUtils {

    public static int readInt(final InputStream is) throws IOException {
        return ByteBuffer.wrap(is.readNBytes(Integer.BYTES)).getInt();
    }

    public static short readShort(final InputStream is) throws IOException {
        return ByteBuffer.wrap(is.readNBytes(Short.BYTES)).getShort();
    }

    public static String readUTF(final InputStream is) throws IOException {
        final var bytes = is.readNBytes(readShort(is));
        return new String(bytes);
    }

    public static String readNSymbols(final InputStream is, final int n) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            try {
                stringBuilder.append(readOneUTF8(is));
            } catch (final RuntimeException e) {
                throw new RuntimeException("InputStream hasn't " + n + " symbols", e);
            }
        }
        return stringBuilder.toString();
    }

    public static CharBuffer readOneUTF8(final InputStream is) throws IOException {
        final int first = is.read();
        final byte[] bytes = new byte[4];
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
            final int read = is.read(bytes, 1, len - 1);
            if (read != len - 1) {
                throw new RuntimeException("Expected UTF8 char, actual: EOF");
            }
        }
        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes, 0, len));

    }

    public static byte[] readBytes(final InputStream is, final int size) throws IOException {
        final byte[] res = new byte[size];
        if (is.read(res) != size) {
            throw new RuntimeException("InputStream hasn't 3 bytes");
        }
        return res;
//        return new Trigram(readNSymbols(is, 3));
    }
}
