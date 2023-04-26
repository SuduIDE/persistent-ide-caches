package com.github.SuduIDE.persistentidecaches.utils;

import com.github.SuduIDE.persistentidecaches.CountingCache;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class DummyCountingCache<V> implements CountingCache<V> {
    public final AtomicInteger fileCounter = new AtomicInteger();
    public final Map<Integer, V> filesInProject = new HashMap<>();
    public final Map<V, Integer> reverseFilesInProject = new HashMap<>();

    @Override
    public int getNumber(final V obj) {
        return reverseFilesInProject.get(obj);
    }

    @Override
    public V getObject(final int objNum) {
        return filesInProject.get(objNum);
    }

    @Override
    public void tryRegisterNewObj(final V obj) {
        if (reverseFilesInProject.get(obj) == null) {
            filesInProject.put(fileCounter.get(), obj);
            reverseFilesInProject.put(obj, fileCounter.getAndIncrement());
        }
    }

    @Override
    public void restoreObjectsFromDB() {

    }

    @Override
    public void init() {

    }

    @Override
    public void forEach(final BiConsumer<V, Number> consumer) {
        reverseFilesInProject.forEach(consumer);
    }
}
