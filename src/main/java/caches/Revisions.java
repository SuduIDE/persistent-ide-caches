package caches;

import caches.records.Revision;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Revisions {
    private final Map<Revision, Revision> parents = new HashMap<>();
    private Revision currentRevision = Revision.NULL;
    private int revisions = 0;

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

    public void printInfo() {
        System.err.println(parents.size());
        try (var writer = new PrintWriter("/home/golikovnik/work/persistent-ide-caches/tree.txt")) {
            writer.println(parents.size());
            for (int i = 0; i < parents.size(); i++) {
                var re = getParent(new Revision(i));
                int ind = re == null ? -1 : re.revision();
                writer.println(ind + " " + i);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
