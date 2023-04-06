package com.github.SuduIDE.persistentidecaches.changes;

import com.github.SuduIDE.persistentidecaches.records.FilePointer;

public final class DeleteChange extends FileChange {

    private final String deletedString;

    public DeleteChange(final long timestamp, final FilePointer place, final String deletedString) {
        super(timestamp, place);
        this.deletedString = deletedString;
    }

    public String getDeletedString() {
        return deletedString;
    }

    @Override
    public String toString() {
        return "DeleteChange{" +
                "deletedString='" + deletedString + '\'' +
                "} " + super.toString();
    }
}
