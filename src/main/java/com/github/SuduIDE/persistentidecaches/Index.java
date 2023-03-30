package com.github.SuduIDE.persistentidecaches;

import com.github.SuduIDE.persistentidecaches.records.Revision;

public interface Index<Key, Value> extends ChangeProcessor {


    Value getValue(Key key, Revision revision);

    void checkout(Revision revision);
}
