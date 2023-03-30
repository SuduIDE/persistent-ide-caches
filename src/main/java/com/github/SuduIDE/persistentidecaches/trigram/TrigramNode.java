package com.github.SuduIDE.persistentidecaches.trigram;

import com.github.SuduIDE.persistentidecaches.records.Revision;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public record TrigramNode(Revision revision, Revision parent, long pointer) {

    private static final int BYTE_SIZE = Integer.BYTES + Integer.BYTES + Long.BYTES;

    public static TrigramNode readTrigramNode(RandomAccessFile raf) {
        try {
            var revision = new Revision(raf.readInt());
            var parent = new Revision(raf.readInt());
            var pointer = raf.readLong();
            return new TrigramNode(revision, parent, pointer);
        } catch (IOException e) {
            throw new RuntimeException("Error on reading node", e);
        }
    }

    byte[] toBytes() {
        var bytes = ByteBuffer.allocate(BYTE_SIZE)
                .putInt(revision.revision())
                .putInt(parent.revision())
                .putLong(pointer);
        return bytes.array();
    }

}
