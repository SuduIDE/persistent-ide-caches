package com.github.SuduIDE.persistentidecaches;

import java.util.function.BiConsumer;

public interface CountingCache<V> {

    int getNumber(V obj);

    V getObject(int objNum);

    void tryRegisterNewObj(V obj);

    void restoreObjectsFromDB();

    void init();

    void forEach(BiConsumer<V, Number> consumer);
}
