package caches.utils;

import java.util.HashMap;
import java.util.Map;

public class DoubleCounter<Key1, Key2> {
    private final Map<Key1, Counter<Key2>> counter;

    public DoubleCounter(Map<Key1, Counter<Key2>> counter) {
        this.counter = counter;
    }

    public DoubleCounter() {
        counter = new HashMap<>();
    }

    public void add(Key1 key1, Key2 key2, int value) {
        counter.computeIfAbsent(key1, ignore -> new Counter<>()).add(key2, value);
    }

    public void add(DoubleCounter<Key1, Key2> other) {
        other.forEach(this::add);
    }

    public void add(Key1 key1, Counter<Key2> other) {
        counter.computeIfAbsent(key1, ignore -> new Counter<>()).add(other);
    }

    public void decrease(Key1 key1, Key2 key2, int value) {
        add(key1, key2, -value);
    }

    public void decrease(DoubleCounter<Key1, Key2> other) {
        other.forEach(this::decrease);
    }

    public void decrease(Key1 key1, Counter<Key2> other) {
        counter.computeIfAbsent(key1, ignore -> new Counter<>()).decrease(other);
    }
    public Map<Key1, Counter<Key2>> getAsMap() {
        return counter;
    }

    public void forEach(TriConsumer<Key1, Key2, Integer> function) {
        counter.forEach(((key1, key2IntegerMap) ->
                key2IntegerMap.forEach((key2, value) -> function.accept(key1, key2, value)
                )));
    }

    public int get(Key1 key1, Key2 key2) {
        return counter.getOrDefault(key1, Counter.emptyCounter()).get(key2);
    }

    public DoubleCounter<Key1, Key2> copy() {
        DoubleCounter<Key1, Key2> result = new DoubleCounter<>();
        counter.forEach(result::add);
        return result;
    }
}
