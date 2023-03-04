package caches;

import caches.lmdb.LmdbInt2Int;
import caches.lmdb.LmdbString2Int;
import caches.records.Revision;

public class Revisions {
    public static final String REVISIONS_COUNT = "revisions_count";
    public static final String CURRENT_REVISION = "current_revision";
    private Revision currentRevision;
    private int revisionsCount;
    private final LmdbString2Int variables;
    private final LmdbInt2Int parents;

    public Revisions(LmdbString2Int variables, LmdbInt2Int parents) {
        this.variables = variables;
        this.parents = parents;
        revisionsCount = variables.get(REVISIONS_COUNT);
        if (revisionsCount == -1) {
            variables.put(REVISIONS_COUNT, 0);
        }
        currentRevision = new Revision(variables.get(CURRENT_REVISION));
        if (currentRevision.revision() == -1) {
            variables.put(CURRENT_REVISION, 0);
        }
    }

    public Revision getParent(Revision revision) {
        return new Revision(parents.get(revision.revision()));
    }

    public Revision addRevision(Revision parent) {
        Revision rev = new Revision(revisionsCount++);
        parents.put(rev.revision(), parent.revision());
        updateRevisionsCount();
        return rev;
    }

    public Revision addLastRevision() {
        currentRevision = addRevision(currentRevision);
        updateCurrentRevision();
        return currentRevision;
    }

    public Revision getCurrentRevision() {
        return currentRevision;
    }

    public void setCurrentRevision(Revision revision) {
        currentRevision = revision;
        updateRevisionsCount();
    }

    private void updateCurrentRevision() {
        variables.put(CURRENT_REVISION, currentRevision.revision());
    }

    private void updateRevisionsCount() {
        variables.put(REVISIONS_COUNT, revisionsCount);
    }
}
