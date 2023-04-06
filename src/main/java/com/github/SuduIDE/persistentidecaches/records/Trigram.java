package com.github.SuduIDE.persistentidecaches.records;

import java.util.Arrays;
import java.util.Objects;

public record Trigram(byte[] trigram) implements Comparable<Trigram> {

    public Trigram(final int i) {
        this((long) i);
    }

    public Trigram(final long l) {
        this(new byte[]{(byte) (l >> 16), (byte) (l >> 8), (byte) l});

    }

    public static long toLong(final byte[] bytes) {
        return toInt(bytes);
    }

    public static int toInt(final byte[] bytes) {
        return (((((Math.abs(bytes[0])) << 8) + Math.abs(bytes[1]))) << 8) + Math.abs(bytes[2]);
    }

    public int toInt() {
        return toInt(trigram);
    }

    public long toLong() {
        return toLong(trigram);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Trigram trigram1 = (Trigram) o;
        return Arrays.equals(trigram, trigram1.trigram);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trigram[0], trigram[1], trigram[2]);
    }

    @Override
    public int compareTo(final Trigram o) {
        return Integer.compare(toInt(trigram), toInt(o.trigram));
    }

    @Override
    public String toString() {
        return "Trigram" + Arrays.toString(trigram);
    }
}
