package caches.records;

import java.nio.file.Path;

public record TrigramFileInteger(Trigram trigram, Path file, int value) {
}
