package caches.changes;

import java.io.File;
import java.util.function.Supplier;

public abstract sealed class FileHolderChange extends Change permits ModifyChange, RenameChange, CopyChange {

    private final Supplier<String> oldFileGetter;
    private final Supplier<String> newFileGetter;
    private final File oldFileName;
    private final File newFileName;


    public FileHolderChange(long timestamp, Supplier<String> oldFileGetter, Supplier<String> newFileGetter, File oldFileName, File newFileName) {
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

    public File getOldFileName() {
        return oldFileName;
    }

    public File getNewFileName() {
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
