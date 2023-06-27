package com.github.SuduIDE.persistentidecaches.changes;

import com.github.SuduIDE.persistentidecaches.records.FilePointer;

public sealed class AddChange extends FileChange permits AddChangeWithContext {

    private final String addedString;

    public AddChange(final long timestamp, final FilePointer place, final String addedString) {
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
