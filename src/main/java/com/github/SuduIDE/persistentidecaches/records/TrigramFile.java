package com.github.SuduIDE.persistentidecaches.records;

import java.nio.file.Path;

public record TrigramFile(Trigram trigram, Path file) {
    @Override
    public int hashCode() {
        return trigram.toInt() * 31 + file.hashCode();
    }
}
