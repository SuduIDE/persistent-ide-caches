package caches;

import caches.changes.Change;
import caches.records.FilePointer;
import caches.records.Revision;

import java.util.List;

public interface Index<Key, Value> {

    void processChange(Change change);
    Value getValue(Key key, Revision revision);

    Revision getCurrentRevision();

    List<Revision> getAllRevisions();

    void checkout(Revision revision);
}
