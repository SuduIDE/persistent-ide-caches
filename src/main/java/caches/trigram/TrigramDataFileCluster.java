package caches.trigram;

import caches.FileCache;
import caches.records.Trigram;
import caches.utils.ReadUtils;
import caches.utils.TriConsumer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record TrigramDataFileCluster(TrigramFileCounter deltas, FileCache fileCache) {

    private static final int HEADER_BYTE_SIZE = Integer.BYTES;

    public static void readTrigramDataFileCluster(InputStream is,
                                                  TriConsumer<byte[], Integer, Integer> consumer) {
        try {
            var size = ReadUtils.readInt(is);
            TrigramFileCounter deltas = new TrigramFileCounter();
            for (int i = 0; i < size; i++) {
                TrigramCounterNode.read(is, consumer);
//                deltas.add(it.file(), it.trigramCounter());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error on reading node", e);
        }
    }

    byte[] toBytes() {
        int size = HEADER_BYTE_SIZE;
        Map<File, List<TrigramInteger>> groupedDelta = new HashMap<>();
        deltas.forEach(((trigram, file, integer) -> groupedDelta.computeIfAbsent(file, (ignore) -> new ArrayList<>())));
        deltas.forEach(((trigram, file, integer) -> groupedDelta.get(file).add(new TrigramInteger(trigram, integer))));
        for (var it : groupedDelta.entrySet()) {
            size += TrigramCounterNode.byteSize(it.getValue());
        }
        var bytes = ByteBuffer.allocate(size)
                .putInt(groupedDelta.size());
        for (var it : groupedDelta.entrySet()) {
            TrigramCounterNode.putInBuffer(bytes, fileCache.getNumber(it.getKey()), it.getValue());
        }
        return bytes.array();
    }

    public record TrigramFileDelta(Trigram trigram, File file, int delta) {

        public int byteSize() {
            return trigram.trigram().length + Integer.BYTES + Integer.BYTES;
        }

        private void putInBuffer(ByteBuffer byteBuffer) {
            byteBuffer.put(trigram.trigram());
        }
    }

    private record TrigramCounterNode(File file, List<TrigramInteger> trigramCounter) {
        public static int byteSize(List<TrigramInteger> trigramCounter) {
            return Integer.BYTES + Integer.BYTES +
                    trigramCounter.stream()
                            .mapToInt(TrigramInteger::sizeOf)
                            .sum();
        }

        private static void putInBuffer(ByteBuffer byteBuffer, int fileInt, List<TrigramInteger> trigramCounter) {
            byteBuffer.putInt(fileInt);
            byteBuffer.putInt(trigramCounter.size());
            trigramCounter.forEach(((it) -> it.putInBuffer(byteBuffer)));
        }

        private static void read(InputStream is, TriConsumer<byte[], Integer, Integer> consumer) throws IOException {
            var fileInt = ReadUtils.readInt(is);
            var size = ReadUtils.readInt(is);
            for (int i = 0; i < size; i++) {
                TrigramInteger.read(is, consumer, fileInt);
            }
        }
    }

    private record TrigramInteger(Trigram trigram, int value) {
        private static void read(InputStream is, TriConsumer<byte[], Integer, Integer> consumer, int fileInt)
                throws IOException {
            var trigram = ReadUtils.readBytes(is, 3);
            var delta = ReadUtils.readInt(is);
            consumer.accept(trigram, fileInt, delta);
        }

        private int sizeOf() {
            return trigram.trigram().length + Integer.BYTES;
        }

        private void putInBuffer(ByteBuffer byteBuffer) {
            byteBuffer.put(trigram.trigram());
            byteBuffer.putInt(value);
        }
    }
}
