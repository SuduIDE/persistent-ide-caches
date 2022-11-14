package caches.changes;

import caches.records.FilePointer;

public final class DeleteChange extends Change {
    private final int deletedString;

    public DeleteChange(FilePointer place, long timestamp, int deletedString) {
        super(place, timestamp);
        this.deletedString = deletedString;
    }

    public int getDeletedString() {
        return deletedString;
    }
}
