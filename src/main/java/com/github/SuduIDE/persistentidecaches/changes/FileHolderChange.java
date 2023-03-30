package com.github.SuduIDE.persistentidecaches.changes;

import java.nio.file.Path;
import java.util.function.Supplier;

public abstract sealed class FileHolderChange extends Change permits ModifyChange, RenameChange, CopyChange {

    private final Supplier<String> oldFileGetter;
    private final Supplier<String> newFileGetter;
    private final Path oldFileName;
    private final Path newFileName;


    public FileHolderChange(long timestamp, Supplier<String> oldFileGetter, Supplier<String> newFileGetter, Path oldFileName, Path newFileName) {
        super(timestamp);
        this.oldFileGetter = oldFileGetter;
        this.newFileGetter = newFileGetter;
        this.oldFileName = oldFileName;
        this.newFileName = newFileName;
    }

    public Supplier<String> getOldFileGetter() {
        return oldFileGetter;
    }

    public Supplier<String> getNewFileGetter() {
        return newFileGetter;
    }

    public Path getOldFileName() {
        return oldFileName;
    }

    public Path getNewFileName() {
        return newFileName;
    }

    public String getOldFileContent() {
        return oldFileGetter.get();
    }

    public String getNewFileContent() {
        return newFileGetter.get();
    }

    @Override
    public String toString() {
        return "FileHolderChange{" +
                "oldFileName=" + oldFileName +
                ", newFileName=" + newFileName +
                "} " + super.toString();
    }
}
