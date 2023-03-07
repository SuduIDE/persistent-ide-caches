package caches.trigram;


import caches.records.Trigram;
import caches.records.TrigramFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TrigramFileCounterTest {

    private TrigramFileCounter counter;
    private static final List<Trigram> trigrams = List.of(
            new Trigram(new byte[]{1, 2, 3}),
            new Trigram(new byte[]{1, 4, 5}),
            new Trigram(new byte[]{2, 2, 2}),
            new Trigram(new byte[]{3, 3, 3}),
            new Trigram(new byte[]{4, 5, 1})
    );

    private static final List<File> files = List.of(
            new File("1"),
            new File("2"),
            new File("3"),
            new File("4"),
            new File("5")
    );

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
        TrigramCounter trigramCounter = new TrigramCounter();
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
        TrigramCounter trigramCounter = new TrigramCounter();
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
        var map = Map.of(
                new TrigramFile(trigrams.get(0), files.get(2)), 2,
                new TrigramFile(trigrams.get(1), files.get(1)), 25,
                new TrigramFile(trigrams.get(2), files.get(3)), -3,
                new TrigramFile(trigrams.get(4), files.get(4)), -15
        );
        AtomicInteger count = new AtomicInteger();
        counter.forEach(((trigram, file, integer) -> {
            count.addAndGet(1);
            assertEquals(map.get(new TrigramFile(trigram, file)), integer);
        }));
        assertEquals(count.get(), map.size());
    }
}
