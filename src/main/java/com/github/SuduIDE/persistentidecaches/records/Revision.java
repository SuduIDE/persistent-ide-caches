package com.github.SuduIDE.persistentidecaches.records;

public record Revision(int revision) {
    public static final Revision NULL = new Revision(-1);
}
