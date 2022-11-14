package caches.changes;

import caches.records.FilePointer;

public final class DeleteChange extends Change {
    private final int length;

    public DeleteChange(FilePointer place, long timestamp, int length) {
        super(place, timestamp);
        this.length = length;
    }


    public int getLength() {
        return length;
    }
}
