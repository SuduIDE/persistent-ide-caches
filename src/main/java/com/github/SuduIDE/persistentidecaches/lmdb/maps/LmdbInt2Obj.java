package com.github.SuduIDE.persistentidecaches.lmdb.maps;

import java.util.function.BiConsumer;

public interface LmdbInt2Obj<V> {

    void put(final int key, final V value);
    /**
     * @return value for key or null
     */
    V get(final int key);

    void forEach(final BiConsumer<Integer, V> consumer);
}
