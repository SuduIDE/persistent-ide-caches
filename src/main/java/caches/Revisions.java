package caches;

import caches.changes.Change;
import caches.records.Revision;
import caches.trigram.TrigramIndex;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Revisions {
    private final Map<Revision, Revision> parents = new HashMap<>();
    private Revision currentRevision = Revision.NULL;
    private int revisions = 0;
    public final Map<Revision, List<Change>> changes = new HashMap<>();
    private final Map<Revision, Integer> depth = new HashMap<>();

    public Revision getParent(Revision revision) {
        return parents.get(revision);
    }

    public Revision addRevision(Revision parent) {
        Revision rev = new Revision(revisions++);
        parents.put(rev, parent);
        return rev;
    }

    public Revision addLastRevision() {
        currentRevision = addRevision(currentRevision);
        return currentRevision;
    }

    public Revision getCurrentRevision() {
        return currentRevision;
    }

    public void setCurrentRevision(Revision revision) {
        currentRevision = revision;
    }

    public MeasureResult measurePath(Revision re, int dist) {
        if (dist > depth.get(re)) {
            throw new RuntimeException("dist too big");
        }
        List<Change> changeList = new ArrayList<>();
        MeasureResult result = new MeasureResult();
        for (int iter = 0; iter < dist; iter++) {
            var ch = changes.get(re);
            changeList.addAll(ch);
            result.single += calcSize(ch);
            re = parents.get(re);
        }
        result.combined = calcSize(changeList);
        return result;
    }

    private static class MeasureResult {
        long single;
        long combined;
    }

    public void printInfo(String filename) {
        int K = 5;
        int n = parents.size();
        long[][] costSingle = new long[n][K];
        long[][] costCombined = new long[n][K];
        for (var arr : costSingle) {
            Arrays.fill(arr, -1);
        }
        for (var arr : costCombined) {
            Arrays.fill(arr, -1);
        }

        depth.put(Revision.NULL, 0);
        for (int i = 0; i < n; i++) {
            var re = new Revision(i);
            depth.put(re, depth.get(getParent(re)) + 1);
        }

        try (var writer = new PrintWriter(new FileWriter(filename), true)) {
            writer.println(n);
            int ITERS = 250;
            writer.println(ITERS);
            Random random = new Random(566);
            double log = 0;
            double STEP = 0.5;
            List<Integer> lens = new ArrayList<>();
            while (log < 9) {
                lens.add(Math.max(1, Math.min(n - 1, (int) Math.round(Math.pow(2, log)))));
                log += STEP;
            }
            Collections.sort(lens);
            for (int i = 0; i < lens.size(); i++) {
                if (i > 0 && lens.get(i).equals(lens.get(i - 1))) {
                    continue;
                }
                int step = lens.get(i);
                System.err.println(step);
                writer.println(step);
                for (int it = 0; it < ITERS; it++) {
                    int v = random.nextInt(n);
                    while (depth.get(new Revision(v)) < step) {
                        v = random.nextInt(n);
                    }
                    var result = measurePath(new Revision(v), step);
                    writer.println(result.single + " " + result.combined);
                }
            }
//            for (int step = 1; step < n; step++) {
//                if (step < 10 || step % 10 == 0) {
//                    System.err.println(step);
//                    writer.println(step);
//                    for (int it = 0; it < ITERS; it++) {
//                        int v = random.nextInt(n);
//                        while (depth.get(new Revision(v)) < step) {
//                            v = random.nextInt(n);
//                        }
//                        var result = measurePath(new Revision(v), step);
//                        writer.println(result.single + " " + result.combined);
//                    }
//                }
//            }
//            for (int i = 0; i < n; i++) {
//                var re = new Revision(i);
//                long single = 0;
//                List<Change> changeList = new ArrayList<>();
//                for (int iter = 0; !re.equals(Revision.NULL) && iter < K; iter++) {
//                    var ch = changes.get(re);
//                    changeList.addAll(ch);
//                    single += calcSize(ch);
//                    re = parents.get(re);
//                    costSingle[i][iter] = single;
//                    costCombined[i][iter] = calcSize(changeList);
//                }
//                for (int j = 0; j < K; j++) {
//                    writer.println(costSingle[i][j] + " " + costCombined[i][j]);
//                }
//                if (i % 100 == 0) {
//                    System.err.println("Done " + i + " vertices");
//                }
//            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long calcSize(List<Change> changes) {
        var index = new TrigramIndex();
        index.prepare(changes);
        return index.counter.size();
    }
}
