package caches.records;

import java.util.Arrays;
import java.util.Objects;

public record Trigram(byte[] trigram) {

    public static long toLong(byte[] bytes) {
        return (bytes[0] << 8 + bytes[1]) << 8 + bytes[2];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trigram trigram1 = (Trigram) o;
        return Arrays.equals(trigram, trigram1.trigram);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trigram[0], trigram[1], trigram[2]);
    }
}
