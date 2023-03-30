package com.github.SuduIDE.persistentidecaches.records;

import java.nio.file.Path;

public record FilePointer(Path file, int offset) {
}
