package caches.records;

import java.nio.file.Path;

public record TrigramFile(Trigram trigram, Path file) {
}
