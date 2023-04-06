package com.github.SuduIDE.persistentidecaches;

import com.github.SuduIDE.persistentidecaches.lmdb.LmdbInt2Int;
import com.github.SuduIDE.persistentidecaches.lmdb.LmdbString2Int;
import com.github.SuduIDE.persistentidecaches.records.Revision;

public class RevisionsImpl implements Revisions {

    public static final String REVISIONS_COUNT = "revisions_count";
    public static final String CURRENT_REVISION = "current_revision";
    private final LmdbString2Int variables;
    private final LmdbInt2Int parents;
    private Revision currentRevision;
    private int revisionsCount;

    public RevisionsImpl(final LmdbString2Int variables, final LmdbInt2Int parents) {
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

    @Override
    public Revision getParent(final Revision revision) {
        return new Revision(parents.get(revision.revision()));
    }

    @Override
    public Revision addRevision(final Revision parent) {
        final Revision rev = new Revision(revisionsCount++);
        parents.put(rev.revision(), parent.revision());
        updateRevisionsCount();
        return rev;
    }

    @Override
    public Revision addLastRevision() {
        currentRevision = addRevision(currentRevision);
        updateCurrentRevision();
        return currentRevision;
    }

    @Override
    public Revision getCurrentRevision() {
        return currentRevision;
    }

    @Override
    public void setCurrentRevision(final Revision revision) {
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
