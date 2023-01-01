package caches;

import caches.records.Revision;

import java.util.HashMap;
import java.util.Map;

public class Revisions {
    private final Map<Revision, Revision> parents = new HashMap<>();
    private Revision currentRevision;
    private int revisions = 0;

    public Revision getParent(Revision revision) {
        return parents.get(revision);
    }

    public Revision addRevision(Revision parent) {
        Revision rev = new Revision(revisions++);
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

    public void setCurrentRevision(Revision revision) {
        currentRevision = revision;
    }
}
