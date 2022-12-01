package caches.trigram;

import caches.records.CheckoutTime;
import caches.records.Revision;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static caches.trigram.TrigramNode.BYTE_SIZE;

public class TrigramCache {
    public static final List<CheckoutTime> checkouts = new ArrayList<>();

    public static void pushNode(File trigramFile, long timestamp, TrigramNode.Action action) throws FileNotFoundException {
        int parent;
        try {
            var size = Files.size(trigramFile.toPath());
            parent = size == 0 ? 0 : (int) ((size - BYTE_SIZE) / BYTE_SIZE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var revision = getRevision(timestamp);
        try (FileOutputStream writer = new FileOutputStream(trigramFile, true)) {
            writer.write(new TrigramNode(revision, parent, action).toBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Revision getRevision(long timestamp) {
        return new Revision(1);
//        return checkouts.get(checkouts.size() - 1).revision();
    }

}
