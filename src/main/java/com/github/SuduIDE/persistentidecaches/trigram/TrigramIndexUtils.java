package com.github.SuduIDE.persistentidecaches.trigram;

import com.github.SuduIDE.persistentidecaches.ccsearch.CamelCaseIndexUtils;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TrigramIndexUtils {

    private final TrigramIndex trigramIndex;

    public TrigramIndexUtils(final TrigramIndex trigramIndex) {
        this.trigramIndex = trigramIndex;
    }

    public List<Path> filesForString(final String str) {
        final var trigramSet = CamelCaseIndexUtils.getTrigramsSet(str);
        final Set<Path> fileSet = new TreeSet<>(trigramIndex.getCounter().getObjForTrigram(trigramSet.first()));
        trigramSet.pollFirst();
        trigramSet.forEach(it -> fileSet.retainAll(trigramIndex.getCounter().getObjForTrigram(it)));
        return new ArrayList<>(fileSet);
    }
}
