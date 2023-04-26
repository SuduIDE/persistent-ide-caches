package com.github.SuduIDE.persistentidecaches.lmdb;

import com.github.SuduIDE.persistentidecaches.CountingCache;
import com.github.SuduIDE.persistentidecaches.lmdb.maps.LmdbInt2Obj;
import com.github.SuduIDE.persistentidecaches.lmdb.maps.LmdbString2Int;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class CountingCacheImpl<V> implements CountingCache<V> {

    private final String objectsStringName;
    private final LmdbInt2Obj<V> objInProject;
    private final Map<V, Integer> reverseObjInProject = new HashMap<>();
    private final LmdbString2Int variables;

    public CountingCacheImpl(final String objectsStringName,
            final LmdbInt2Obj<V> objectsInProject,
            final LmdbString2Int variables) {
        this.objectsStringName = objectsStringName;
        this.objInProject = objectsInProject;
        this.variables = variables;
    }

    @Override
    public int getNumber(final V obj) {
        return reverseObjInProject.get(obj);
    }

    @Override
    public V getObject(final int objNum) {
        return objInProject.get(objNum);
    }

    @Override
    public void tryRegisterNewObj(final V obj) {
        if (reverseObjInProject.get(obj) == null) {
            final var fileNum = variables.get(objectsStringName);
            objInProject.put(fileNum, obj);
            reverseObjInProject.put(obj, fileNum);
            variables.put(objectsStringName, fileNum + 1);
        }
    }

    @Override
    public void restoreObjectsFromDB() {
        objInProject.forEach((integer, file) -> reverseObjInProject.put(file, integer));
    }

    @Override
    public void init() {
        if (variables.get(objectsStringName) == -1) {
            variables.put(objectsStringName, 0);
        }
    }

    @Override
    public void forEach(final BiConsumer<V, Number> consumer) {
        reverseObjInProject.forEach(consumer);
    }

}
