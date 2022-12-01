package caches.trigram;

import caches.records.Trigram;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TrigramCounter {
    private final Map<Trigram, Integer> counter;

    public TrigramCounter() {
        counter = new HashMap<>();
    }

    public TrigramCounter(Map<Trigram, Integer> counter) {
        this.counter = counter;
    }

    public void add(Trigram key) {
        add(key, 1);
    }

    public void decrease(Trigram key) {
        add(key, -1);
    }

    public void add(Trigram key, int value) {
        counter.compute(key, (k, v) -> v == null ? value : Math.max(v + value, 0));
    }

    public void decrease(Trigram key, int value) {
        add(key, -value);
    }

    public void add(TrigramCounter other) {
        other.counter.forEach(this::add);
    }

    public void decrease(TrigramCounter other) {
        other.counter.forEach(this::decrease);
    }

    public TrigramCounter plus(TrigramCounter other) {
        var copy = new TrigramCounter(new HashMap<>(counter));
        copy.add(other);
        return copy;
    }

    public TrigramCounter minus(TrigramCounter other) {
        var copy = copy();
        copy.decrease(other);
        return copy;
    }

    public int get(Trigram key) {
        return counter.getOrDefault(key, 0);
    }

    public Map<Trigram, Integer> getAsMap() {
        return counter;
    }

    public TrigramCounter copy() {
        return new TrigramCounter(new HashMap<>(counter));
    }


}
