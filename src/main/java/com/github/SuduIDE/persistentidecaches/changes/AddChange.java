package com.github.SuduIDE.persistentidecaches.changes;

import com.github.SuduIDE.persistentidecaches.records.FilePointer;

public final class AddChange extends FileChange {
    private final String addedString;

    public AddChange(long timestamp, FilePointer place, String addedString) {
        super(timestamp, place);
        this.addedString = addedString;
    }

    public String getAddedString() {
        return addedString;
    }

    @Override
    public String toString() {
        return "AddChange{" +
                "addedString='" + addedString + '\'' +
                "} " + super.toString();
    }
}
