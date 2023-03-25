package caches.records;

import java.nio.file.Path;

public record FilePointer(Path file, int offset) {
}
