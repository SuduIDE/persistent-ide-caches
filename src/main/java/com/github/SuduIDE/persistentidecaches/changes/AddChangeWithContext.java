package com.github.SuduIDE.persistentidecaches.changes;

import com.github.SuduIDE.persistentidecaches.records.FilePointer;

/**
 * Saves context of added string. Needs two or more chars of context from both sides of added string.
 */
public final class AddChangeWithContext extends AddChange {

    /**
     * Points on first char of added string.
     */
    private final int startIndex;
    /**
     * Points on char next to last char of added string.
     */
    private final int endIndex;

    public AddChangeWithContext(final long timestamp, final FilePointer place,
            final String addedString, final int startIndex, final int endIndex) {
        super(timestamp, place, addedString);
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    @Override
    public String toString() {
        return "AddChangeWithContext{" +
                "startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                "} " + super.toString();
    }
}
