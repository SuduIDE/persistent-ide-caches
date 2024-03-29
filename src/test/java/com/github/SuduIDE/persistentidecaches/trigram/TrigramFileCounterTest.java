package com.github.SuduIDE.persistentidecaches.trigram;


import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.SuduIDE.persistentidecaches.records.Trigram;
import com.github.SuduIDE.persistentidecaches.records.TrigramFile;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TrigramFileCounterTest {

    private static final List<Trigram> trigrams = List.of(
            new Trigram(new byte[]{1, 2, 3}),
            new Trigram(new byte[]{1, 4, 5}),
            new Trigram(new byte[]{2, 2, 2}),
            new Trigram(new byte[]{3, 3, 3}),
            new Trigram(new byte[]{4, 5, 1})
    );
    private static final List<Path> files = List.of(
            Path.of("1"),
            Path.of("2"),
            Path.of("3"),
            Path.of("4"),
            Path.of("5")
    );
    private TrigramFileCounter counter;

    @BeforeEach
    public void resetCounter() {
        counter = new TrigramFileCounter();
    }

    @Test
    public void testAdd() {
        assertEquals(0, counter.get(trigrams.get(0), files.get(2)));
        counter.add(trigrams.get(0), files.get(2), 2);
        assertEquals(2, counter.get(trigrams.get(0), files.get(2)));
        counter.add(trigrams.get(1), files.get(1), 10);
        assertEquals(10, counter.get(trigrams.get(1), files.get(1)));
        counter.add(trigrams.get(1), files.get(1), 10);
        assertEquals(2, counter.get(trigrams.get(0), files.get(2)));
        assertEquals(20, counter.get(trigrams.get(1), files.get(1)));
    }

    @Test
    public void testDecrease() {
        assertEquals(0, counter.get(trigrams.get(0), files.get(2)));
        counter.decrease(trigrams.get(0), files.get(2), 2);
        assertEquals(-2, counter.get(trigrams.get(0), files.get(2)));
        counter.decrease(trigrams.get(1), files.get(1), 10);
        assertEquals(-10, counter.get(trigrams.get(1), files.get(1)));
        counter.decrease(trigrams.get(1), files.get(1), 10);
        assertEquals(-2, counter.get(trigrams.get(0), files.get(2)));
        assertEquals(-20, counter.get(trigrams.get(1), files.get(1)));
    }

    @Test
    public void testAdd2() {
        assertEquals(0, counter.get(trigrams.get(0), files.get(2)));
        final TrigramCounter trigramCounter = new TrigramCounter();
        trigramCounter.add(trigrams.get(0), 2);
        counter.add(files.get(2), trigramCounter);
        assertEquals(2, counter.get(trigrams.get(0), files.get(2)));
        assertEquals(0, counter.get(trigrams.get(1), files.get(1)));
        trigramCounter.add(trigrams.get(1), 15);
        counter.add(files.get(1), trigramCounter);
        counter.add(trigrams.get(1), files.get(1), 10);
        assertEquals(2, counter.get(trigrams.get(0), files.get(1)));
        assertEquals(25, counter.get(trigrams.get(1), files.get(1)));
        counter.add(files.get(1), trigramCounter);
        assertEquals(40, counter.get(trigrams.get(1), files.get(1)));
    }

    @Test
    public void testDecrease2() {
        assertEquals(0, counter.get(trigrams.get(0), files.get(2)));
        final TrigramCounter trigramCounter = new TrigramCounter();
        trigramCounter.add(trigrams.get(0), 2);
        counter.decrease(files.get(2), trigramCounter);
        assertEquals(-2, counter.get(trigrams.get(0), files.get(2)));
        assertEquals(0, counter.get(trigrams.get(1), files.get(1)));
        trigramCounter.add(trigrams.get(1), 15);
        counter.decrease(files.get(1), trigramCounter);
        counter.add(trigrams.get(1), files.get(1), 10);
        assertEquals(-2, counter.get(trigrams.get(0), files.get(1)));
        assertEquals(-5, counter.get(trigrams.get(1), files.get(1)));
        counter.decrease(files.get(1), trigramCounter);
        assertEquals(-4, counter.get(trigrams.get(0), files.get(1)));
        assertEquals(-20, counter.get(trigrams.get(1), files.get(1)));
    }

    @Test
    public void testForEach() {
        counter.add(trigrams.get(0), files.get(2), 2);
        counter.add(trigrams.get(1), files.get(1), 10);
        counter.add(trigrams.get(1), files.get(1), 15);
        counter.add(trigrams.get(2), files.get(3), -3);
        counter.add(trigrams.get(4), files.get(4), -5);
        counter.decrease(trigrams.get(4), files.get(4), 10);
        final var map = Map.of(
                new TrigramFile(trigrams.get(0), files.get(2)), 2,
                new TrigramFile(trigrams.get(1), files.get(1)), 25,
                new TrigramFile(trigrams.get(2), files.get(3)), -3,
                new TrigramFile(trigrams.get(4), files.get(4)), -15
        );
        final AtomicInteger count = new AtomicInteger();
        counter.forEach(((trigram, file, integer) -> {
            count.addAndGet(1);
            assertEquals(map.get(new TrigramFile(trigram, file)), integer);
        }));
        assertEquals(count.get(), map.size());
    }
}
