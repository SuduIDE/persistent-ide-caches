package caches.trigram;

import caches.records.Trigram;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TrigramFileCounter {

    private final Map<Trigram, Map<File, Integer>> counter;

    public TrigramFileCounter(Map<Trigram, Map<File, Integer>> counter) {
        this.counter = counter;
    }

    public TrigramFileCounter() {
        counter = new HashMap<>();
    }

    public void add(Trigram trigram, File file, int value) {
        counter.computeIfAbsent(trigram, ignore -> new HashMap<>())
                .compute(file, (k, v) -> v == null ? value : Math.max(v + value, 0));
    }

    public void decrease(Trigram trigram, File file, int value) {
        add(trigram, file, -value);
    }

    public void add(File file, TrigramCounter other) {
        other.getAsMap().forEach((trigram, value) -> add(trigram, file, value));
    }

    public void decrease(File file, TrigramCounter other) {
        other.getAsMap().forEach((trigram, value) -> decrease(trigram, file, value));
    }

    public Map<Trigram, Map<File, Integer>> getAsMap() {
        return counter;
    }

    public int get(Trigram trigram, File file) {
        return counter.getOrDefault(trigram, Collections.emptyMap()).getOrDefault(file, 0);
    }
}
