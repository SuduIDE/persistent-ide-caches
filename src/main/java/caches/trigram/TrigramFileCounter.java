package caches.trigram;

import caches.records.Trigram;
import caches.records.TrigramFileInteger;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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
                .compute(file, (k, v) -> v == null ? value : v + value);
    }

    public void add(TrigramFileInteger it) {
        add(it.trigram(), it.file(), it.value());
    }

    public void add(File file, TrigramCounter other) {
        other.forEach((trigram, value) -> add(trigram, file, value));
    }

    public void add(TrigramFileCounter other) {
        other.forEach(this::add);
    }

    public void decrease(Trigram trigram, File file, int value) {
        add(trigram, file, -value);
    }

    public void decrease(TrigramFileInteger it) {
        decrease(it.trigram(), it.file(), it.value());
    }

    public void decrease(File file, TrigramCounter other) {
        other.getAsMap().forEach((trigram, value) -> decrease(trigram, file, value));
    }

    public void decrease(TrigramFileCounter other) {
        other.forEach(this::decrease);
    }

    public Map<Trigram, Map<File, Integer>> getAsMap() {
        return counter;
    }

    public void forEach(Consumer<TrigramFileInteger> function) {
        counter.forEach(((trigram, fileIntegerMap) ->
                fileIntegerMap.forEach((file, value) -> function.accept(new TrigramFileInteger(trigram, file, value))
                )));
    }

    public int get(Trigram trigram, File file) {
        return counter.getOrDefault(trigram, Collections.emptyMap()).getOrDefault(file, 0);
    }

    public TrigramFileCounter copy() {
        var result = new TrigramFileCounter();
        forEach(it -> result.add(it.trigram(), it.file(), it.value()));
        return result;
    }
}
