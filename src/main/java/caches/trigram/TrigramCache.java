package caches.trigram;

import caches.records.CheckoutTime;
import caches.records.Revision;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static caches.trigram.TrigramNode.HEADER_BYTE_SIZE;

public class TrigramCache {
    public static final List<CheckoutTime> checkouts = new ArrayList<>();

    // TODO
    public static void pushNode(File trigramFile, long timestamp, List<TrigramNode.FileAction> actions) throws FileNotFoundException {
        int parent = 0;
        var revision = getRevision(timestamp);
        try (FileOutputStream writer = new FileOutputStream(trigramFile, true)) {
            writer.write(new TrigramNode(revision, parent, actions).toBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Revision getRevision(long timestamp) {
        return new Revision(1);
//        return checkouts.get(checkouts.size() - 1).revision();
    }

}
