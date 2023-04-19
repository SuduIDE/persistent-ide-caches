package com.github.SuduIDE.persistentidecaches;

import com.github.SuduIDE.persistentidecaches.lmdb.LmdbIntCountingCache;
import com.github.SuduIDE.persistentidecaches.lmdb.maps.LmdbInt2Obj;
import com.github.SuduIDE.persistentidecaches.lmdb.maps.LmdbString2Int;
import java.nio.file.Path;

public class PathCache extends LmdbIntCountingCache<Path> {


    public PathCache(final LmdbInt2Obj<Path> objectsInProject,
            final LmdbString2Int variables) {
        super("files", objectsInProject, variables);
    }
}
