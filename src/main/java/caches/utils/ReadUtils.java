package caches.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

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
}
