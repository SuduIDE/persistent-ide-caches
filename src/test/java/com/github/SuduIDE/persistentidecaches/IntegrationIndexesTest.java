package com.github.SuduIDE.persistentidecaches;

import com.github.SuduIDE.persistentidecaches.changes.AddChange;
import com.github.SuduIDE.persistentidecaches.records.FilePointer;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class IntegrationIndexesTest {

    public static final Path EMPTY_PATH = Path.of("");
    public static final List<Path> FILES = List.of(
            EMPTY_PATH.resolve("file1.java"),
            EMPTY_PATH.resolve("file2.java"),
            EMPTY_PATH.resolve("file3.java"),
            EMPTY_PATH.resolve("dir1").resolve("dir2").resolve("file121.java"),
            EMPTY_PATH.resolve("dir1").resolve("dir2").resolve("file122.java"),
            EMPTY_PATH.resolve("dir1").resolve("dir3").resolve("file132.java"),
            EMPTY_PATH.resolve("dir1").resolve("file13.java"),
            EMPTY_PATH.resolve("😊😊😊😊😊😊.java")
    );
    @TempDir
    Path tmpDataDir;
    IndexesManager indexesManager;

    @BeforeEach
    public void prepare() {
        indexesManager = new IndexesManager(true, tmpDataDir);
    }

    @Test
    public void testOneChange() {
        addFiles(FILES);
        var trigramIndex = indexesManager.addTrigramIndex();
        var addChanges = FILES.stream().map(it -> createAddChange(it, it.toString())).toList();
        trigramIndex.processChanges(addChanges);
        FILES.forEach(file ->
                Assertions.assertEquals(List.of(file),
                        trigramIndex.getTrigramIndexUtils().filesForString(file.toString()))
        );
    }

    @SuppressWarnings("SameParameterValue")
    private void addFiles(List<Path> paths) {
        paths.forEach(it -> indexesManager.getFileCache().tryRegisterNewFile(it));
    }

    private AddChange createAddChange(Path path, String text) {
        return new AddChange(System.currentTimeMillis(), new FilePointer(path, 0), text);
    }


    @AfterEach
    public void close() {
        indexesManager.close();
    }
}