package caches.records;

import java.util.Arrays;
import java.util.Objects;

public record Trigram(byte[] trigram) {

    public int toInt() {
        return (trigram[0] << 8 + trigram[1]) << 8 + trigram[2];
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
