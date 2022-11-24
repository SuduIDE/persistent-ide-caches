package caches.trigram;

import caches.Index;
import caches.changes.AddChange;
import caches.changes.Change;
import caches.changes.DeleteChange;
import caches.records.Revision;
import caches.records.Trigram;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TrigramHistoryIndex implements Index<String, List<Revision>> {

    public static final String DIRECTORY = ".trigrams/";
    private final Map<Trigram, File> trigramsFiles = new HashMap<>();

    private Map<Trigram, Integer> getTrigramsCount(String str) {
        Map<Trigram, Integer> result = new TreeMap<>();
        for (int i = 3; i <= str.length(); i++) {
            Trigram trigram = new Trigram(str.substring(i - 3, i));
            result.compute(trigram, (key, value) -> value == null ? 1 : value + 1);
        }
        return result;
    }

    private Map<Trigram, Integer> getTrigramsCount(File file, int pos, int length) {
        Map<Trigram, Integer> result = new TreeMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            if (reader.skip(pos) != pos) {
                return result;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                int read = reader.read();
                if (read == -1) {
                    return result;
                }
                sb.append((char) read);
            }
            int index = 2;
            int read = reader.read();
            while (index < length && read != -1) {
                Trigram trigram = new Trigram(sb.toString());
                result.compute(trigram, (key, value) -> value == null ? 1 : value + 1);
                sb.deleteCharAt(0).append((char) read);
                index++;
                read = reader.read();
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Error on reading file " + file.getName(), e);
        }
    }

    private Map<Trigram, Integer> trigramDiff(Map<Trigram, Integer> minuend, Map<Trigram, Integer> subtrahend) {
        minuend.replaceAll((trigram, value) -> value - subtrahend.get(trigram));
        return minuend;
    }

    private Map<Trigram, Integer> getTrigramsCount(File file) {
        return getTrigramsCount(file, 0, Integer.MAX_VALUE);
    }

    @Override
    public void processChange(Change change) {
        switch (change) {
            case AddChange event:
                var trigrams = trigramDiff(getTrigramsCount(event.getAddedString()),
                        getTrigramsCount(event.getPlace().file()));
                trigrams.entrySet().stream()
                        .filter((entry) -> entry.getValue() == 0)
                        .forEach((entry) -> {
                            try {
                                if (!trigramsFiles.containsKey(entry.getKey())) {
                                    var file = new File(DIRECTORY + entry.getKey().trigram());
                                    try {
                                        file.createNewFile();
                                    } catch (IOException e) {
                                        throw new RuntimeException("Can't create file" + file.getAbsolutePath(), e);
                                    }
                                    trigramsFiles.put(entry.getKey(), file);
                                }
                                TrigramCache.pushNode(trigramsFiles.get(entry.getKey()), event.getPlace().file(),
                                        event.getTimestamp(), TrigramNode.Action.ADD);
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException("Not found file" + trigramsFiles.get(entry.getKey()), e);
                            }
                        });
                break;
            case DeleteChange event:
//                getTrigramsCounts(event.getDeletedString());
                break;
        }
    }

    @Override
    public List<Revision> getValue(String s, Revision revision) {
        return null;
    }

    @Override
    public Revision getCurrentRevision() {
        return null;
    }

    @Override
    public List<Revision> getAllRevisions() {
        return null;
    }

    @Override
    public void checkout(Revision revision) {

    }
}
