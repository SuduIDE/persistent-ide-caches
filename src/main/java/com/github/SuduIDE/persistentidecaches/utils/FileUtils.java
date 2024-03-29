package com.github.SuduIDE.persistentidecaches.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class FileUtils {

    private FileUtils() {

    }

    public static void createParentDirectories(final Path... paths) {
        Arrays.stream(paths).forEach(FileUtils::createParentDirectories);
    }

    public static void createParentDirectories(final Path path) {
        try {
            Files.createDirectories(path);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
