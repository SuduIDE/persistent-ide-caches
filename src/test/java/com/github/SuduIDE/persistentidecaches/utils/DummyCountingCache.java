package com.github.SuduIDE.persistentidecaches.utils;

import com.github.SuduIDE.persistentidecaches.CountingCache;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class DummyCountingCache<V> implements CountingCache<V> {
    public final AtomicInteger counter = new AtomicInteger();
    public final Map<Integer, V> numToObj = new HashMap<>();
    public final Map<V, Integer> objToNum = new HashMap<>();

    @Override
    public int getNumber(final V obj) {
        return objToNum.get(obj);
    }

    @Override
    public V getObject(final int objNum) {
        return numToObj.get(objNum);
    }

    @Override
    public void tryRegisterNewObj(final V obj) {
        if (objToNum.get(obj) == null) {
            numToObj.put(counter.get(), obj);
            objToNum.put(obj, counter.getAndIncrement());
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
        objToNum.forEach(consumer);
    }
}
