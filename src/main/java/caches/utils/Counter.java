package caches.utils;


import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Counter<Key> {
    private final Map<Key, Integer> counter;

    // LIMIT=300
    // fast-util.Avl 210 64 1 9 9 64
    // fast-util.OpenHash 98 37 0 5 5 38
    // fast-util.RB 202 68 1 10 9 67
    // fast-util.LinkedOpenHash 96 34 0 4 4 33
    // HashMap 72 28 0 3 4 27

    public Counter() {
        counter = new HashMap<>();
    }

    public Counter(Map<Key, Integer> counter) {
        this.counter = new HashMap<>(counter);
    }

    public static <Key> Counter<Key> emptyCounter() {
        return new Counter<>();
    }

    public void add(Key key) {
        add(key, 1);
    }

    public void decrease(Key key) {
        add(key, -1);
    }

    public void add(Key key, int value) {
        counter.compute(key, (k, v) -> v == null ? value : v + value);
    }

    public void decrease(Key key, int value) {
        add(key, -value);
    }

    public void add(Counter<Key> other) {
        other.counter.forEach(this::add);
    }

    public void decrease(Counter<Key> other) {
        other.counter.forEach(this::decrease);
    }

    public Counter<Key> plus(Counter<Key> other) {
        var copy = copy();
        copy.add(other);
        return copy;
    }

    public Counter<Key> minus(Counter<Key> other) {
        var copy = copy();
        copy.decrease(other);
        return copy;
    }

    public int get(Key key) {
        return counter.getOrDefault(key, 0);
    }

    public Map<Key, Integer> getAsMap() {
        return counter;
    }

    public Counter<Key> copy() {
        return new Counter<>(counter);
    }

    public void forEach(BiConsumer<Key, Integer> function) {
        counter.forEach(function);
    }
}
