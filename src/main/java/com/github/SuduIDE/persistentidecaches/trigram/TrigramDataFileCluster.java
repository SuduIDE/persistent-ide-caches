package com.github.SuduIDE.persistentidecaches.trigram;

import com.github.SuduIDE.persistentidecaches.lmdb.CountingCacheImpl;
import com.github.SuduIDE.persistentidecaches.records.Trigram;
import com.github.SuduIDE.persistentidecaches.utils.ByteArrIntIntConsumer;
import com.github.SuduIDE.persistentidecaches.utils.ReadUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record TrigramDataFileCluster(TrigramFileCounter deltas, CountingCacheImpl<Path> pathCache) {

    private static final int HEADER_BYTE_SIZE = Integer.BYTES;

    public static void readTrigramDataFileCluster(final InputStream is,
            final ByteArrIntIntConsumer consumer) {
        try {
            final var size = ReadUtils.readInt(is);
            final TrigramFileCounter deltas = new TrigramFileCounter();
            for (int i = 0; i < size; i++) {
                TrigramCounterNode.read(is, consumer);
//                deltas.add(it.file(), it.trigramCounter());
            }
        } catch (final IOException e) {
            throw new RuntimeException("Error on reading node", e);
        }
    }

    byte[] toBytes() {
        int size = HEADER_BYTE_SIZE;
        final Map<Path, List<TrigramInteger>> groupedDelta = new HashMap<>();
        deltas.forEach(((trigram, file, integer) -> groupedDelta.computeIfAbsent(file, (ignore) -> new ArrayList<>())));
        deltas.forEach(((trigram, file, integer) -> groupedDelta.get(file).add(new TrigramInteger(trigram, integer))));
        for (final var it : groupedDelta.entrySet()) {
            size += TrigramCounterNode.byteSize(it.getValue());
        }
        final var bytes = ByteBuffer.wrap(new byte[size])
                .putInt(groupedDelta.size());
        for (final var it : groupedDelta.entrySet()) {
            TrigramCounterNode.putInBuffer(bytes, pathCache.getNumber(it.getKey()), it.getValue());
        }
        return bytes.array();
    }

    public record TrigramFileDelta(Trigram trigram, File file, int delta) {

        public int byteSize() {
            return trigram.trigram().length + Integer.BYTES + Integer.BYTES;
        }

        private void putInBuffer(final ByteBuffer byteBuffer) {
            byteBuffer.put(trigram.trigram());
        }
    }

    private record TrigramCounterNode(File file, List<TrigramInteger> trigramCounter) {

        public static int byteSize(final List<TrigramInteger> trigramCounter) {
            return Integer.BYTES + Integer.BYTES +
                    trigramCounter.stream()
                            .mapToInt(TrigramInteger::sizeOf)
                            .sum();
        }

        private static void putInBuffer(final ByteBuffer byteBuffer, final int fileInt,
                final List<TrigramInteger> trigramCounter) {
            byteBuffer.putInt(fileInt);
            byteBuffer.putInt(trigramCounter.size());
            trigramCounter.forEach(((it) -> it.putInBuffer(byteBuffer)));
        }

        private static void read(final InputStream is, final ByteArrIntIntConsumer consumer) throws IOException {
            final var fileInt = ReadUtils.readInt(is);
            final var size = ReadUtils.readInt(is);
            for (int i = 0; i < size; i++) {
                TrigramInteger.read(is, consumer, fileInt);
            }
        }
    }

    private record TrigramInteger(Trigram trigram, int value) {

        private static void read(final InputStream is, final ByteArrIntIntConsumer consumer, final int fileInt)
                throws IOException {
            final var trigram = ReadUtils.readBytes(is, 3);
            final var delta = ReadUtils.readInt(is);
            consumer.accept(trigram, fileInt, delta);
        }

        private int sizeOf() {
            return trigram.trigram().length + Integer.BYTES;
        }

        private void putInBuffer(final ByteBuffer byteBuffer) {
            byteBuffer.put(trigram.trigram());
            byteBuffer.putInt(value);
        }
    }
}
