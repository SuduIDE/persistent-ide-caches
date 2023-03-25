package caches.changes;

import java.nio.file.Path;
import java.util.function.Supplier;

public final class CopyChange extends FileHolderChange {
    public CopyChange(long timestamp, Supplier<String> oldFileGetter, Supplier<String> newFileGetter, Path oldFile, Path newFile) {
        super(timestamp, oldFileGetter, newFileGetter, oldFile, newFile);
    }

    @Override
    public String toString() {
        return "CopyChange{} " + super.toString();
    }
}
