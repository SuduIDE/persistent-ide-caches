package com.github.SuduIDE.persistentidecaches.lmdb;

import com.github.SuduIDE.persistentidecaches.lmdb.maps.LmdbInt2Obj;
import com.github.SuduIDE.persistentidecaches.lmdb.maps.LmdbString2Int;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class LmdbIntCountingCache<V> {

    private final String objectsStringName;
    private final LmdbInt2Obj<V> objInProject;
    private final Map<V, Integer> reverseObjInProject = new HashMap<>();
    private final LmdbString2Int variables;

    public LmdbIntCountingCache(final String objectsStringName,
            final LmdbInt2Obj<V> objectsInProject,
            final LmdbString2Int variables) {
        this.objectsStringName = objectsStringName;
        this.objInProject = objectsInProject;
        this.variables = variables;
    }

    public int getNumber(final V obj) {
        return reverseObjInProject.get(obj);
    }

    public V getObject(final int objNum) {
        return objInProject.get(objNum);
    }

    public void tryRegisterNewObj(final V obj) {
        if (reverseObjInProject.get(obj) == null) {
            final var fileNum = variables.get(objectsStringName);
            objInProject.put(fileNum, obj);
            reverseObjInProject.put(obj, fileNum);
            variables.put(objectsStringName, fileNum + 1);
        }
    }

    public void restoreObjectsFromDB() {
        objInProject.forEach((integer, file) -> reverseObjInProject.put(file, integer));
    }

    public void init() {
        if (variables.get(objectsStringName) == -1) {
            variables.put(objectsStringName, 0);
        }
    }

    public void forEach(final BiConsumer<V, Number> consumer) {
        reverseObjInProject.forEach(consumer);
    }

}
