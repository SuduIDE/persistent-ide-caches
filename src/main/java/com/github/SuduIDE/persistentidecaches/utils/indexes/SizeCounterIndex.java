package com.github.SuduIDE.persistentidecaches.utils.indexes;

import com.github.SuduIDE.persistentidecaches.Index;
import com.github.SuduIDE.persistentidecaches.changes.AddChange;
import com.github.SuduIDE.persistentidecaches.changes.Change;
import com.github.SuduIDE.persistentidecaches.changes.CopyChange;
import com.github.SuduIDE.persistentidecaches.changes.ModifyChange;
import com.github.SuduIDE.persistentidecaches.records.Revision;
import java.util.List;

public class SizeCounterIndex implements Index<String, String> {

    private long summarySize = 0;

    public long getSummarySize() {
        return summarySize;
    }

    @Override
    public void prepare(final List<? extends Change> changes) {
        processChanges(changes);
    }

    @Override
    public void processChanges(final List<? extends Change> changes) {
        changes.forEach(this::processChange);
    }

    private void processChange(final Change change) {
        if (change instanceof final AddChange addChange) {
            summarySize += addChange.getAddedString().getBytes().length;
        } else if (change instanceof final ModifyChange modifyChange) {
            summarySize += modifyChange.getNewFileContent().getBytes().length;
        } else if (change instanceof final CopyChange copyChange) {
            summarySize += copyChange.getNewFileContent().getBytes().length;
        }
    }

    @Override
    public String getValue(final String s, final Revision revision) {
        return null;
    }

    @Override
    public void checkout(final Revision revision) {

    }
}
