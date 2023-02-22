package caches;

import caches.lmdb.LmdbInt2Int;
import caches.records.Revision;

public class Revisions {
    public static final String REVISIONS_COUNT = "revisions_count";
    public static final String CURRENT_REVISION = "current_revision";
    private final LmdbInt2Int parents = new LmdbInt2Int(GlobalVariables.env, "revisions");
    private Revision currentRevision = new Revision(GlobalVariables.variables.get(CURRENT_REVISION));
    private int revisionsCount = GlobalVariables.variables.get(REVISIONS_COUNT);

    public Revisions() {
        if (revisionsCount == -1) {
            GlobalVariables.variables.put(REVISIONS_COUNT, 0);
        }
        if (currentRevision.revision() == -1) {
            GlobalVariables.variables.put(CURRENT_REVISION, 0);
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
        GlobalVariables.variables.put(CURRENT_REVISION, currentRevision.revision());
    }

    private void updateRevisionsCount() {
        GlobalVariables.variables.put(REVISIONS_COUNT, revisionsCount);
    }
}
