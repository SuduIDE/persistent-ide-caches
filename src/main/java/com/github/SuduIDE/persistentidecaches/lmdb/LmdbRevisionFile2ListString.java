package com.github.SuduIDE.persistentidecaches.lmdb;

import com.github.SuduIDE.persistentidecaches.lmdb.maps.LmdbLong2String;
import com.github.SuduIDE.persistentidecaches.lmdb.maps.LmdbMap;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import org.lmdbjava.Env;

public class LmdbRevisionFile2ListString implements LmdbMap {

    private final LmdbLong2String long2String;
    public LmdbRevisionFile2ListString(final Env<ByteBuffer> env, final String name) {
        long2String = new LmdbLong2String(env, name);
    }

    private static long getKey(final long revision, final long file) {
        return (revision << 32) + file;
    }

    public void put(final int revision, final int file, final List<String> s) {
        long2String.put(getKey(revision, file), String.join(" ", s));
    }

    public List<String> get(final int revision, final int file) {
        final String ans = long2String.get(getKey(revision, file));
        return ans == null ? null : Arrays.stream(ans.split(" ")).filter(it -> !it.isEmpty()).toList();
    }

}
