package caches.changes;

import caches.records.FilePointer;

public final class AddChange extends Change {
    private final String addedString;

    public AddChange(FilePointer place, long timestamp, String addedString) {
        super(place, timestamp);
        this.addedString = addedString;
    }

    public String getAddedString() {
        return addedString;
    }
}
