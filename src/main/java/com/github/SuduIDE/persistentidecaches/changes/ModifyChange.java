package com.github.SuduIDE.persistentidecaches.changes;

import java.nio.file.Path;
import java.util.function.Supplier;

public final class ModifyChange extends FileHolderChange {
    public ModifyChange(long timestamp, Supplier<String> oldFileGetter, Supplier<String> newFileGetter, Path oldFile, Path newFile) {
        super(timestamp, oldFileGetter, newFileGetter, oldFile, newFile);
    }

    @Override
    public String toString() {
        return "ModifyChange{" +
                "} " + super.toString();
    }
}
