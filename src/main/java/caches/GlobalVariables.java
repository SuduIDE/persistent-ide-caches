package caches;

import caches.records.Revision;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalVariables {

    public static final AtomicInteger fileCounter = new AtomicInteger();
    public static final Revisions revisions = new Revisions();
    public static final Map<Integer, File> filesInProject = new TreeMap<>();
    public static final Map<File, Integer> reverseFilesInProject = new TreeMap<>();
    public static final Map<String, Revision> parsedCommits = new HashMap<>();

    public static void tryRegisterNewFile(File file) {
        if (reverseFilesInProject.get(file) == null) {
            filesInProject.put(fileCounter.get(), file);
            reverseFilesInProject.put(file, fileCounter.getAndIncrement());
        }
    }
}
