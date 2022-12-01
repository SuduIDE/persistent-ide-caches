package caches.changes;

import caches.records.FilePointer;

public final class DeleteChange extends FileChange {
    private final String deletedString;

    public DeleteChange(long timestamp, FilePointer place, String deletedString) {
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
