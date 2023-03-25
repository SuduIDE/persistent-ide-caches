package caches.trigram;

import caches.records.Trigram;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

public class TrigramIndexUtils {
    private final TrigramIndex trigramIndex;

    public TrigramIndexUtils(TrigramIndex trigramIndex) {
        this.trigramIndex = trigramIndex;
    }

    public List<Path> filesForString(final String str) {
        NavigableSet<Trigram> trigramSet = new TreeSet<>();
        byte[] bytes = str.getBytes();
        for (int i = 2; i < bytes.length; i++) {
            Trigram trigram = new Trigram(new byte[]{bytes[i - 2], bytes[i - 1], bytes[i]});
            trigramSet.add(trigram);
        }
        Set<Path> fileSet = new TreeSet<>(trigramIndex.getCounter().getFilesForTrigram(trigramSet.first()));
        trigramSet.pollFirst();
        trigramSet.forEach(it -> fileSet.retainAll(trigramIndex.getCounter().getFilesForTrigram(it)));
        return new ArrayList<>(fileSet);
    }
}
