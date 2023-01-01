package caches.trigram;

import caches.GlobalVariables;
import caches.records.Trigram;
import caches.utils.ReadUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public record TrigramDataFileCluster(List<TrigramFileDelta> deltas) {

    private static final int HEADER_BYTE_SIZE = Integer.BYTES;

    public static TrigramDataFileCluster readTrigramDataFileCluster(InputStream is) {
        try {
            var size = ReadUtils.readInt(is);
            List<TrigramFileDelta> deltas = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                deltas.add(TrigramFileDelta.read(is));
            }
            return new TrigramDataFileCluster(deltas);
        } catch (IOException e) {
            throw new RuntimeException("Error on reading node", e);
        }
    }

    byte[] toBytes() {
        int size = HEADER_BYTE_SIZE;
        for (var it : deltas) {
            size += it.byteSize();
        }
        var bytes = ByteBuffer.allocate(size)
                .putInt(deltas.size());
        deltas.forEach(it -> it.putInBuffer(bytes));
        return bytes.array();
    }

    public record TrigramFileDelta(Trigram trigram, File file, int delta) {

        public int byteSize() {
            return trigram.trigram().getBytes().length + Integer.BYTES + Integer.BYTES;
        }

        private void putInBuffer(ByteBuffer byteBuffer) {
            byteBuffer.put(trigram.trigram().getBytes());
            byteBuffer.putInt(GlobalVariables.reverseFilesInProject.get(file));
            byteBuffer.putInt(delta);
        }

        private static TrigramFileDelta read(InputStream is) throws IOException {
            var trigram = new Trigram(
                    String.valueOf(new char[]{ReadUtils.readOneUTF8(is),
                            ReadUtils.readOneUTF8(is),
                            ReadUtils.readOneUTF8(is)}));
            var fileInt = ReadUtils.readInt(is);
            var file = GlobalVariables.filesInProject.get(fileInt);
            var delta = ReadUtils.readInt(is);
            return new TrigramFileDelta(trigram, file, delta);
        }
    }
}
