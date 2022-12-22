package caches;

import caches.changes.Change;

import java.util.List;

public interface ChangeProcessor {

    void prepare(List<Change> changes);


    void processChanges(List<Change> changes);
}
