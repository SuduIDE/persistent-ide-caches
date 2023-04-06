package com.github.SuduIDE.persistentidecaches.trigram;

import com.github.SuduIDE.persistentidecaches.records.Trigram;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

public class TrigramIndexUtils {

    private final TrigramIndex trigramIndex;

    public TrigramIndexUtils(final TrigramIndex trigramIndex) {
        this.trigramIndex = trigramIndex;
    }

    public List<Path> filesForString(final String str) {
        final NavigableSet<Trigram> trigramSet = new TreeSet<>();
        final byte[] bytes = str.getBytes();
        for (int i = 2; i < bytes.length; i++) {
            final Trigram trigram = new Trigram(new byte[]{bytes[i - 2], bytes[i - 1], bytes[i]});
            trigramSet.add(trigram);
        }
        final Set<Path> fileSet = new TreeSet<>(trigramIndex.getCounter().getFilesForTrigram(trigramSet.first()));
        trigramSet.pollFirst();
        trigramSet.forEach(it -> fileSet.retainAll(trigramIndex.getCounter().getFilesForTrigram(it)));
        return new ArrayList<>(fileSet);
    }
}
