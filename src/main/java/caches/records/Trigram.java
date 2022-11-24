package caches.records;

import java.util.Objects;

public record Trigram(String trigram) implements Comparable<Trigram> {

    @Override
    public int compareTo(Trigram trigram) {
        return this.trigram.compareTo(trigram.trigram);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trigram trigram1 = (Trigram) o;
        return Objects.equals(trigram, trigram1.trigram);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trigram);
    }
}
