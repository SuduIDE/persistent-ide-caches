package caches;

import caches.changes.Change;
import caches.records.Revision;
import caches.trigram.TrigramIndex;
import org.eclipse.jgit.events.RepositoryEvent;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Revisions {
    private final Map<Revision, Revision> parents = new HashMap<>();
    private Revision currentRevision = Revision.NULL;
    private int revisions = 0;
    public final Map<Revision, List<Change>> changes = new HashMap<>();
    private int[] depth, par, size, largest, whoLargest, degree, pathTop;

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
        if (dist > depth[re.revision() + 1]) {
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
        result.pathTop = re.revision() + 1;
        return result;
    }

    public MeasureResult measureOnlyPath(Revision re, int dist) {
        if (dist > depth[re.revision() + 1]) {
            throw new RuntimeException("dist too big");
        }
        List<Change> changeList = new ArrayList<>();
        MeasureResult result = new MeasureResult();
        for (int iter = 0; iter < dist; iter++) {
            var ch = changes.get(re);
            changeList.addAll(ch);
            var pre = parents.get(re);
            if (depth[re.revision() + 1] != depth[pre.revision() + 1] + 1) {
                throw new RuntimeException("Wrong depths");
            }
            re = pre;
        }
        result.combined = calcSize(changeList);
        result.pathTop = re.revision() + 1;
        return result;
    }

    private List<Integer> collectLengths(int sz, int need) {
        int pw = 1;
        while (pw < sz) {
            pw *= 2;
        }
        int ql = sz - need;
        int qr = sz;
        List<Integer> L = new ArrayList<>(), R = new ArrayList<>();
        int len = 1;
        for (ql += pw, qr += pw; ql < qr; ql /= 2, qr /= 2, len *= 2) {
            if (ql % 2 == 1) {
                L.add(len);
                ql++;
            }
            if (qr % 2 == 1) {
                R.add(len);
                qr--;
            }
        }
        Collections.reverse(L);
        R.addAll(L);
        return R;
    }

    public HLDMeasureResult hldMeasurePath(Revision re, int dist) {
        if (dist > depth[re.revision() + 1]) {
            throw new RuntimeException("dist too big");
        }
        HLDMeasureResult result = new HLDMeasureResult(measurePath(re, dist));
        int v = re.revision() + 1;
        while (dist > 0) {
            result.travel++;
            int dv = depth[v];
            int pv = pathTop[v];
            int can = dv - depth[pv];
            int take = Math.min(can, dist);
            List<Integer> lengths = collectLengths(can, take);
            int sum = 0;
            for (int d : lengths) {
                var up = measureOnlyPath(new Revision(v - 1), d);
                result.withHLD += up.combined;
                sum += d;
                if (depth[v] - depth[up.pathTop] != d) {
                    throw new RuntimeException("Travelled wrong distance");
                }
                v = up.pathTop;
            }
            if (sum != take) {
                throw new RuntimeException("Fail in HLD measure path");
            }
            dist -= take;
        }
        return result;
    }

    private static class MeasureResult {
        long single;
        long combined;
        int pathTop;
    }

    private static class HLDMeasureResult {
        long single;
        long combined;
        long withHLD;
        int travel;

        public HLDMeasureResult(MeasureResult measurePath) {
            this.single = measurePath.single;
            this.combined = measurePath.combined;
        }
    }

    private static class HLDPathInfo {
        int length;
        long single;
        long combined;
        long stored;
    }

    public void printInfo(String filename) {
//        int K = 5;
        int n = parents.size();

        depth = new int[n + 1];
        par = new int[n + 1];
        for (int i = 0; i < n; i++) {
            par[i + 1] = parents.get(new Revision(i)).revision() + 1;
            depth[i + 1] = depth[par[i + 1]] + 1;
        }

        size = new int[n + 1];
        largest = new int[n + 1];
        whoLargest = new int[n + 1];
        degree = new int[n + 1];
        pathTop = new int[n + 1];
        for (int i = n - 1; i >= 0; i--) {
            size[i + 1]++;
            degree[par[i + 1]]++;
            size[par[i + 1]] += size[i + 1];
        }
        size[0]++;
        if (size[0] != n + 1) {
            System.err.println(size[0] + " bad size root: should be " + (n + 1));
            throw new RuntimeException();
        }
        for (int i = n - 1; i >= 0; i--) {
            if (size[i + 1] > largest[par[i + 1]]) {
                largest[par[i + 1]] = size[i + 1];
                whoLargest[par[i + 1]] = i + 1;
            }
        }
        for (int i = 0; i < n; i++) {
            if (par[i + 1] > 0 && degree[par[i + 1]] == 1) {
                pathTop[i + 1] = pathTop[par[i + 1]];
            } else {
                pathTop[i + 1] = par[i + 1];
            }
        }

        List<HLDPathInfo> pathInfos = new ArrayList<>();
        for (int v = 0; v < n; v++) {
            if (degree[v + 1] == 1) {
                continue;
            }
            System.err.println("Measure HLD: " + (v+1) + "/" + n);
            int pv = pathTop[v + 1];
            HLDPathInfo info = new HLDPathInfo();
            info.length = depth[v + 1] - depth[pv];
            var whole = measurePath(new Revision(v), info.length);
            info.single = whole.single;
            info.combined = whole.combined;
            if (whole.pathTop != pv) {
                throw new RuntimeException("Wrong parent");
            }
            List<Integer> path = new ArrayList<>();
            int cur = v + 1;
            while (cur != pv) {
                path.add(cur);
                cur = par[cur];
            }
            if (path.size() != info.length) {
                throw new RuntimeException("Wrong path length");
            }
            Collections.reverse(path);
            int pw = 1;
            while (pw < info.length) {
                pw *= 2;
            }
            for (int len = 1; len <= pw; len *= 2) {
                System.err.println(len);
                for (int st = 0; st + len <= info.length; st += len) {
                    info.stored += measureOnlyPath(new Revision(path.get(st + len - 1) - 1), len).combined;
                }
            }
            pathInfos.add(info);
        }
        System.out.println(pathInfos.size() + " paths");
        for (var path : pathInfos) {
            System.out.println(path.length + " " + path.single + " " + path.combined + " " + path.stored);
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
                    while (depth[v + 1] < step) {
                        v = random.nextInt(n);
                    }
                    var result = hldMeasurePath(new Revision(v), step);
                    writer.println(result.single + " " + result.combined + " " + result.withHLD + " " + result.travel);
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
