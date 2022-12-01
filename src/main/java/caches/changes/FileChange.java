package caches.changes;

import caches.records.FilePointer;

public abstract sealed class FileChange extends Change permits AddChange, DeleteChange {
    private final FilePointer place;

    public FileChange(long timestamp, FilePointer place) {
        super(timestamp);
        this.place = place;
    }

    public FilePointer getPlace() {
        return place;
    }

    @Override
    public String toString() {
        return "FileChange{" +
                "place=" + place +
                "} " + super.toString();
    }
}
