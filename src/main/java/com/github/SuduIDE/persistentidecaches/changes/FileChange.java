package com.github.SuduIDE.persistentidecaches.changes;

import com.github.SuduIDE.persistentidecaches.records.FilePointer;

public abstract sealed class FileChange extends Change permits AddChange, DeleteChange {

    private final FilePointer place;

    public FileChange(final long timestamp, final FilePointer place) {
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
