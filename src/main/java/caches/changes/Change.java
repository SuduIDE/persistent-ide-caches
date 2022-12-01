package caches.changes;

import caches.records.FilePointer;

public abstract sealed class Change permits FileChange, FileHolderChange {
    private final long timestamp;

    public Change(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Change{" +
                "timestamp=" + timestamp +
                '}';
    }
}
