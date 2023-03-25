package caches.changes;

import java.nio.file.Path;
import java.util.function.Supplier;

public final class RenameChange extends FileHolderChange {
    public RenameChange(long timestamp, Supplier<String> oldFileGetter, Supplier<String> newFileGetter, Path oldFile, Path newFile) {
        super(timestamp, oldFileGetter, newFileGetter, oldFile, newFile);
    }

    @Override
    public String toString() {
        return "RenameChange{} " + super.toString();
    }
}
