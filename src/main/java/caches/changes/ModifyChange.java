package caches.changes;

import java.io.File;
import java.util.function.Supplier;

public final class ModifyChange extends FileHolderChange {
    public ModifyChange(long timestamp, Supplier<String> oldFileGetter, Supplier<String> newFileGetter, File oldFile, File newFile) {
        super(timestamp, oldFileGetter, newFileGetter, oldFile, newFile);
    }

    @Override
    public String toString() {
        return "ModifyChange{" +
                "} " + super.toString();
    }
}
