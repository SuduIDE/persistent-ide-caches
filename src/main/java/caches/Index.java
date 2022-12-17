package caches;

import caches.records.Revision;

import java.util.List;

public interface Index<Key, Value> extends ChangeProcessor {


    Value getValue(Key key, Revision revision);

    Revision getCurrentRevision();

    List<Revision> getAllRevisions();

    void checkout(Revision revision);
}
