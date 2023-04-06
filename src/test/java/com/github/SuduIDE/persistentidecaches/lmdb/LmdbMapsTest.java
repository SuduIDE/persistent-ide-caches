package com.github.SuduIDE.persistentidecaches.lmdb;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lmdbjava.Env;

public class LmdbMapsTest {

    public static final String GET = "get";
    public static final String PUT = "put";
    @TempDir
    Path directory;
    Env<ByteBuffer> env;

    @BeforeEach
    void prepare() {
        this.env = Env.create()
                .setMapSize(10_485_760)
                .setMaxDbs(1)
                .setMaxReaders(1)
                .open(directory.toFile());
    }

    @Test
    void testInt2Int() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        final var map = new LmdbInt2Int(env, "a");
        final var list = List.of(
                Pair.of(1, 2),
                Pair.of(3, 4),
                Pair.of(5, 6),
                Pair.of(10000, 2)
        );
        testMap(map, list, List.of(100, 4, 0), -1, Integer.TYPE, Integer.TYPE);
    }

    @Test
    void testInt2Long() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        final var map = new LmdbInt2Long(env, "a");
        final var list = List.of(
                Pair.of(1, 2L),
                Pair.of(3, 4L),
                Pair.of(5, 6L),
                Pair.of(10000, 2L)
        );
        testMap(map, list, List.of(100, 4, 0), -1L, Integer.TYPE, Long.TYPE);
    }

    @Test
    void testLong2Int() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        final var map = new LmdbLong2Int(env, "a");
        final List<Pair<Long, Integer>> list = List.of(
                Pair.of(1L, 2),
                Pair.of(3L, 4),
                Pair.of(5L, 6),
                Pair.of(10000L, 2),
                Pair.of(100_000_000_000_000L, 2)
        );
        testMap(map, list, List.of(100L, 4L, 0L, 100_000_000_000_001L), -1, Long.TYPE, Integer.TYPE);
    }

    @Test
    void testInt2File() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        final var map = new LmdbInt2Path(env, "a");
        final var list = List.of(
                Pair.of(1, Path.of("1")),
                Pair.of(3, Path.of("3")),
                Pair.of(5, Path.of("5")),
                Pair.of(10000, Path.of("1"))
        );
        testMap(map, list, List.of(100, 4, 0), null, Integer.TYPE, Path.class);
    }

    @Test
    void testString2Int() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        final var map = new LmdbString2Int(env, "a");
        final var list = List.of(
                Pair.of("1", 1),
                Pair.of("3", 3),
                Pair.of("5", 5),
                Pair.of("1000", 1)
        );
        testMap(map, list, List.of("100", "4", "0"), -1, String.class, Integer.TYPE);
    }

    @Test
    void testInt2String() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        final var map = new LmdbInt2String(env, "a");
        final var list = List.of(
                Pair.of(1, "1"),
                Pair.of(3, "3"),
                Pair.of(5, "5"),
                Pair.of(10000, "1")
        );
        testMap(map, list, List.of(100, 4, 0), null, Integer.TYPE, String.class);
    }

    @Test
    void testSha12String() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        final var map = new LmdbInt2String(env, "a");
        final var list = List.of(
                Pair.of(1, "1"),
                Pair.of(3, "3"),
                Pair.of(5, "5"),
                Pair.of(10000, "1")
        );
        testMap(map, list, List.of(100, 4, 0), null, Integer.TYPE, String.class);
    }


    <T, V> void testMap(final LmdbMap map, final List<Pair<T, V>> list, final List<T> missingKeys, final V defaultValue,
            final Class<?> keyToken, final Class<?> valueToken)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final var putMethod = map.getClass().getMethod(PUT, keyToken, valueToken);
        final var getMethod = map.getClass().getMethod(GET, keyToken);
        for (final var pair : list) {
            for (final var key : missingKeys) {
                Assertions.assertNotEquals(pair.getLeft(), pair.getRight());
            }
        }
        for (final var pair : list) {
            putMethod.invoke(map, pair.getLeft(), pair.getRight());
        }
        for (final var pair : list) {
            Assertions.assertEquals(getMethod.invoke(map, pair.getLeft()), pair.getRight());
        }
        for (final var key : missingKeys) {
            Assertions.assertEquals(getMethod.invoke(map, key), defaultValue);

        }
    }

}
