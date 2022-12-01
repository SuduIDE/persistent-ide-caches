package caches.changes;

import java.io.File;
import java.util.function.Supplier;

public final class RenameChange extends FileHolderChange {
    public RenameChange(long timestamp, Supplier<String> oldFileGetter, Supplier<String> newFileGetter, File oldFile, File newFile) {
        super(timestamp, oldFileGetter, newFileGetter, oldFile, newFile);
    }

    @Override
    public String toString() {
        return "RenameChange{} " + super.toString();
    }
}
