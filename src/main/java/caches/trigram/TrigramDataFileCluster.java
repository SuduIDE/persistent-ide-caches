package caches.trigram;

import caches.GlobalVariables;
import caches.records.Trigram;
import caches.utils.Counter;
import caches.utils.ReadUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
public record TrigramDataFileCluster(TrigramFileCounter deltas) {

    private static final int HEADER_BYTE_SIZE = Integer.BYTES;

    public static TrigramDataFileCluster readTrigramDataFileCluster(InputStream is) {
        try {
            var size = ReadUtils.readInt(is);
            TrigramFileCounter deltas = new TrigramFileCounter();
            for (int i = 0; i < size; i++) {
                var it = TrigramCounterNode.read(is);
                deltas.add(it.file(), it.trigramCounter());
            }
            return new TrigramDataFileCluster(deltas);
        } catch (IOException e) {
            throw new RuntimeException("Error on reading node", e);
        }
    }

    byte[] toBytes() {
        int size = HEADER_BYTE_SIZE;
        for (var it : deltas.getAsMap().entrySet()) {
            size += TrigramCounterNode.byteSize(it.getValue());
        }
        var bytes = ByteBuffer.allocate(size)
                .putInt(deltas.getAsMap().size());
        for (var it : deltas.getAsMap().entrySet()) {
            TrigramCounterNode.putInBuffer(bytes, it.getKey(), it.getValue());
        }
        return bytes.array();
    }

    public record TrigramFileDelta(Trigram trigram, File file, int delta) {

        public int byteSize() {
            return trigram.trigram().getBytes(StandardCharsets.UTF_8).length + Integer.BYTES + Integer.BYTES;
        }

        private void putInBuffer(ByteBuffer byteBuffer) {
            byteBuffer.put(trigram.trigram().getBytes(StandardCharsets.UTF_8));
        }
    }
    private record TrigramCounterNode(File file, Counter<Trigram> trigramCounter) {
        public static int byteSize(Counter<Trigram> trigramCounter) {
            return Integer.BYTES + Integer.BYTES +
                    trigramCounter.getAsMap().keySet().stream()
                            .mapToInt(TrigramInteger::sizeOf)
                            .sum();
        }

        private static void putInBuffer(ByteBuffer byteBuffer, File file, Counter<Trigram> trigramCounter) {
            byteBuffer.putInt(GlobalVariables.reverseFilesInProject.get(file));
            byteBuffer.putInt(trigramCounter.getAsMap().size());
            trigramCounter.forEach(((trigram, integer) -> TrigramInteger.putInBuffer(byteBuffer, trigram, integer)));
        }
        private static TrigramCounterNode read(InputStream is) throws IOException {
            var fileInt = ReadUtils.readInt(is);
            var file = GlobalVariables.filesInProject.get(fileInt);
            var size = ReadUtils.readInt(is);
            var counter = new TrigramCounter();
            for (int i = 0; i < size; i++) {
                var it = TrigramInteger.read(is);
                counter.add(it.trigram, it.value);
            }
            return new TrigramCounterNode(file, counter);
        }
    }

    private record TrigramInteger(Trigram trigram, int value) {
        private static int sizeOf(Trigram trigram) {
            return trigram.trigram().getBytes().length + Integer.BYTES;
        }

        private static void putInBuffer(ByteBuffer byteBuffer, Trigram trigram, int value) {
            byteBuffer.put(trigram.trigram().getBytes());
            byteBuffer.putInt(value);
        }

        private static TrigramInteger read(InputStream is) throws IOException {
            var trigram = ReadUtils.readTrigram(is);
            var delta = ReadUtils.readInt(is);
            return new TrigramInteger(trigram, delta);
        }
    }
}
