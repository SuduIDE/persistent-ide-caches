package com.github.SuduIDE.persistentidecaches.utils;

import com.github.SuduIDE.persistentidecaches.Revisions;
import com.github.SuduIDE.persistentidecaches.records.Revision;
import java.util.HashMap;
import java.util.Map;

public class DummyRevisions implements Revisions {

    private final Map<Revision, Revision> parents = new HashMap<>();
    private Revision currentRevision;
    private int revisions = 0;

    public Revision getParent(final Revision revision) {
        return parents.get(revision);
    }

    public Revision addRevision(final Revision parent) {
        final Revision rev = new Revision(revisions++);
        parents.put(rev, parent);
        return rev;
    }

    public Revision addLastRevision() {
        currentRevision = addRevision(currentRevision);
        return currentRevision;
    }

    public Revision getCurrentRevision() {
        return currentRevision;
    }

    public void setCurrentRevision(final Revision revision) {
        currentRevision = revision;
    }
}


