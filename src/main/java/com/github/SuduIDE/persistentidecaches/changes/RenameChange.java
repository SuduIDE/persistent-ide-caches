package com.github.SuduIDE.persistentidecaches.changes;

import java.nio.file.Path;
import java.util.function.Supplier;

public final class RenameChange extends FileHolderChange {

    public RenameChange(final long timestamp, final Supplier<String> oldFileGetter, final Supplier<String> newFileGetter, final Path oldFile,
            final Path newFile) {
        super(timestamp, oldFileGetter, newFileGetter, oldFile, newFile);
    }

    @Override
    public String toString() {
        return "RenameChange{} " + super.toString();
    }
}
