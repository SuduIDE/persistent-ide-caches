package caches.trigram;

import caches.records.Trigram;

import java.io.File;
import java.util.*;

public class TrigramIndexManager {
    private final TrigramIndex trigramIndex;
    public TrigramIndexManager(TrigramIndex trigramIndex) {
        this.trigramIndex = trigramIndex;
    }

    public List<File> filesForString(final String str) {
        NavigableSet<Trigram> trigramSet = new TreeSet<>();
        byte[] bytes = str.getBytes();
        for (int i = 2; i < bytes.length; i++) {
            Trigram trigram = new Trigram(new byte[]{bytes[i - 2], bytes[i - 1], bytes[i]});
            trigramSet.add(trigram);
        }
        Set<File> fileSet = new TreeSet<>(trigramIndex.getCounter().getFilesForTrigram(trigramSet.first()));
        trigramSet.pollFirst();
        trigramSet.forEach(it -> fileSet.retainAll(trigramIndex.getCounter().getFilesForTrigram(it)));
        return new ArrayList<>(fileSet);
    }
}
