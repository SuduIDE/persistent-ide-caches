package caches;

import caches.changes.Change;

import java.util.List;

public interface ChangeProcessor {

    void prepare(List<? extends Change> changes);


    void processChanges(List<? extends Change> changes);
}
