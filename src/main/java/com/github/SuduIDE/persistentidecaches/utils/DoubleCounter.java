package com.github.SuduIDE.persistentidecaches.utils;

import java.util.HashMap;
import java.util.Map;

public class DoubleCounter<Key1, Key2> {

    private final Map<Key1, Counter<Key2>> counter;

    public DoubleCounter(final Map<Key1, Counter<Key2>> counter) {
        this.counter = counter;
    }

    public DoubleCounter() {
        counter = new HashMap<>();
    }

    public void add(final Key1 key1, final Key2 key2, final int value) {
        counter.computeIfAbsent(key1, ignore -> new Counter<>()).add(key2, value);
    }

    public void add(final DoubleCounter<Key1, Key2> other) {
        other.forEach(this::add);
    }

    public void add(final Key1 key1, final Counter<Key2> other) {
        counter.computeIfAbsent(key1, ignore -> new Counter<>()).add(other);
    }

    public void decrease(final Key1 key1, final Key2 key2, final int value) {
        add(key1, key2, -value);
    }

    public void decrease(final DoubleCounter<Key1, Key2> other) {
        other.forEach(this::decrease);
    }

    public void decrease(final Key1 key1, final Counter<Key2> other) {
        counter.computeIfAbsent(key1, ignore -> new Counter<>()).decrease(other);
    }

    public Map<Key1, Counter<Key2>> getAsMap() {
        return counter;
    }

    public void forEach(final TriConsumer<Key1, Key2, Integer> function) {
        counter.forEach(((key1, key2IntegerMap) ->
                key2IntegerMap.forEach((key2, value) -> function.accept(key1, key2, value)
                )));
    }

    public int get(final Key1 key1, final Key2 key2) {
        return counter.getOrDefault(key1, Counter.emptyCounter()).get(key2);
    }

    public DoubleCounter<Key1, Key2> copy() {
        final DoubleCounter<Key1, Key2> result = new DoubleCounter<>();
        counter.forEach(result::add);
        return result;
    }
}
