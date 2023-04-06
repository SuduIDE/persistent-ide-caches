package com.github.SuduIDE.persistentidecaches.changes;

import java.nio.file.Path;
import java.util.function.Supplier;

public final class ModifyChange extends FileHolderChange {

    public ModifyChange(final long timestamp, final Supplier<String> oldFileGetter, final Supplier<String> newFileGetter, final Path oldFile,
            final Path newFile) {
        super(timestamp, oldFileGetter, newFileGetter, oldFile, newFile);
    }

    @Override
    public String toString() {
        return "ModifyChange{" +
                "} " + super.toString();
    }
}
