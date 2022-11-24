package caches.changes;

import caches.records.FilePointer;

public abstract sealed class Change permits AddChange, DeleteChange {
    private final FilePointer place;
    private final long timestamp;

    public Change(FilePointer place, long timestamp) {
        this.place = place;
        this.timestamp = timestamp;
    }

    public FilePointer getPlace() {
        return place;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
