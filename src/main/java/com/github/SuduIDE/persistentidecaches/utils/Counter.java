package com.github.SuduIDE.persistentidecaches.utils;


import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Counter<Key> {

    protected final Map<Key, Integer> counter;

    // LIMIT=300
    // fast-util.Avl 210 64 1 9 9 64
    // fast-util.OpenHash 98 37 0 5 5 38
    // fast-util.RB 202 68 1 10 9 67
    // fast-util.LinkedOpenHash 96 34 0 4 4 33
    // HashMap 72 28 0 3 4 27

    public Counter() {
        counter = new HashMap<>();
    }

    public Counter(final Map<Key, Integer> counter) {
        this.counter = new HashMap<>(counter);
    }

    public static <Key> Counter<Key> emptyCounter() {
        return new Counter<>();
    }

    public void add(final Key key) {
        add(key, 1);
    }

    public void decrease(final Key key) {
        add(key, -1);
    }

    public void add(final Key key, final int value) {
        counter.merge(key, value, Integer::sum);
    }

    public void decrease(final Key key, final int value) {
        add(key, -value);
    }

    public void add(final Counter<Key> other) {
        other.counter.forEach(this::add);
    }

    public void decrease(final Counter<Key> other) {
        other.counter.forEach(this::decrease);
    }

    public Counter<Key> plus(final Counter<Key> other) {
        final var copy = copy();
        copy.add(other);
        return copy;
    }

    public Counter<Key> minus(final Counter<Key> other) {
        final var copy = copy();
        copy.decrease(other);
        return copy;
    }

    public int get(final Key key) {
        return counter.getOrDefault(key, 0);
    }

    public Map<Key, Integer> getAsMap() {
        return counter;
    }

    public Counter<Key> copy() {
        return new Counter<>(counter);
    }

    public void forEach(final BiConsumer<Key, Integer> function) {
        counter.forEach(function);
    }
}
