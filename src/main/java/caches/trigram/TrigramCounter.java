package caches.trigram;

import caches.records.Trigram;
import caches.utils.Counter;

import java.util.Map;

public class TrigramCounter extends Counter<Trigram> {
    public TrigramCounter() {
        super();
    }

    public TrigramCounter(Map<Trigram, Integer> counter) {
        super(counter);
    }
}
