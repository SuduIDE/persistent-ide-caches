package com.github.SuduIDE.persistentidecaches.changes;

import java.nio.file.Path;
import java.util.function.Supplier;

public final class CopyChange extends FileHolderChange {

    public CopyChange(final long timestamp, final Supplier<String> oldFileGetter, final Supplier<String> newFileGetter, final Path oldFile,
            final Path newFile) {
        super(timestamp, oldFileGetter, newFileGetter, oldFile, newFile);
    }

    @Override
    public String toString() {
        return "CopyChange{} " + super.toString();
    }
}
