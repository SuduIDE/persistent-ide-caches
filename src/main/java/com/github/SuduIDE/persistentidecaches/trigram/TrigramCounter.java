package com.github.SuduIDE.persistentidecaches.trigram;

import com.github.SuduIDE.persistentidecaches.records.Trigram;
import com.github.SuduIDE.persistentidecaches.utils.Counter;
import java.util.Map;

public class TrigramCounter extends Counter<Trigram> {
    public TrigramCounter() {
        super();
    }

    public TrigramCounter(Map<Trigram, Integer> counter) {
        super(counter);
    }
}
