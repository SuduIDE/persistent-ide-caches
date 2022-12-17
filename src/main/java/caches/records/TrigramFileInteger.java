package caches.records;

import java.io.File;

public record TrigramFileInteger(Trigram trigram, File file, int value) {
}
