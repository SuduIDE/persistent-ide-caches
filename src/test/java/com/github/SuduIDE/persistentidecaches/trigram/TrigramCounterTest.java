package com.github.SuduIDE.persistentidecaches.trigram;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.SuduIDE.persistentidecaches.records.Trigram;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TrigramCounterTest {

    private static final List<Trigram> trigrams = List.of(
            new Trigram(new byte[]{1, 2, 3}),
            new Trigram(new byte[]{1, 4, 5}),
            new Trigram(new byte[]{2, 2, 2}),
            new Trigram(new byte[]{3, 3, 3}),
            new Trigram(new byte[]{4, 5, 1})
    );
    private TrigramCounter counter;

    @BeforeEach
    public void resetCounter() {
        counter = new TrigramCounter();
    }

    @Test
    public void testAdd() {
        assertEquals(0, counter.get(trigrams.get(0)));
        counter.add(trigrams.get(0), 2);
        assertEquals(2, counter.get(trigrams.get(0)));
        counter.add(trigrams.get(1), 10);
        assertEquals(10, counter.get(trigrams.get(1)));
        counter.add(trigrams.get(1), 10);
        assertEquals(2, counter.get(trigrams.get(0)));
        assertEquals(20, counter.get(trigrams.get(1)));
    }

    @Test
    public void testDecrease() {
        assertEquals(0, counter.get(trigrams.get(0)));
        counter.decrease(trigrams.get(0), 2);
        assertEquals(-2, counter.get(trigrams.get(0)));
        counter.decrease(trigrams.get(1), 10);
        assertEquals(-10, counter.get(trigrams.get(1)));
        counter.decrease(trigrams.get(1), 10);
        assertEquals(-2, counter.get(trigrams.get(0)));
        assertEquals(-20, counter.get(trigrams.get(1)));
    }

    @Test
    public void testForEach() {
        counter.add(trigrams.get(0), 2);
        counter.add(trigrams.get(1), 10);
        counter.add(trigrams.get(1), 15);
        counter.add(trigrams.get(2), -3);
        counter.add(trigrams.get(4), -5);
        counter.decrease(trigrams.get(4), 10);
        final var map = Map.of(
                trigrams.get(0), 2,
                trigrams.get(1), 25,
                trigrams.get(2), -3,
                trigrams.get(4), -15
        );
        final AtomicInteger count = new AtomicInteger();
        counter.forEach(((trigram, integer) -> {
            count.addAndGet(1);
            assertEquals(map.get(trigram), integer);
        }));
        assertEquals(count.get(), map.size());
    }
}
