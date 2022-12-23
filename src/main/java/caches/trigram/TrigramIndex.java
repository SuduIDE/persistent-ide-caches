package caches.trigram;

import caches.GlobalVariables;
import caches.Index;
import caches.changes.*;
import caches.records.Revision;
import caches.records.Trigram;
import caches.records.TrigramFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class TrigramIndex implements Index<TrigramFile, Integer> {

    public final TrigramCache cache = new TrigramCache();
    public final Preparer preparer = new Preparer();
    public TrigramFileCounter counter = new TrigramFileCounter();

    public TrigramIndex() {

    }

    private static TrigramCounter getTrigramsCount(String str) {
        TrigramCounter result = new TrigramCounter();
        for (int i = 3; i <= str.length(); i++) {
            Trigram trigram = new Trigram(str.substring(i - 3, i));
            result.add(trigram);
        }
        return result;
    }

    private static TrigramCounter getTrigramsCount(File file, int pos, int length) {
        TrigramCounter result = new TrigramCounter();
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
                result.add(trigram);
                sb.deleteCharAt(0).append((char) read);
                index++;
                read = reader.read();
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Error on reading file " + file.getName(), e);
        }
    }

    private static TrigramCounter getTrigramsCount(File file) {
        return getTrigramsCount(file, 0, Integer.MAX_VALUE);
    }

    @Override
    public void prepare(List<Change> changes) {
        preparer.process(changes);
    }

    @Override
    public void processChanges(List<Change> changes) {
        preparer.process(changes);
    }

    private void processChange(Change change, List<TrigramDataFileCluster.TrigramFileDelta> deltas) {
        if (Objects.requireNonNull(change) instanceof AddChange addChange) {
            var trigrams = getTrigramsCount(addChange.getAddedString());
            trigrams.forEach(((trigram, delta) -> deltas.add(
                    new TrigramDataFileCluster.TrigramFileDelta(trigram, addChange.getPlace().file(), delta
                    ))));
        } else {
            throw new IllegalStateException("Unexpected value: " + change);
        }
    }

    private void pushActions(List<TrigramDataFileCluster.TrigramFileDelta> deltas, long timestamp) {
        cache.pushCluster(timestamp, deltas);
    }

    @Override
    public Integer getValue(TrigramFile trigramFile, Revision revision) {
        var currentRevision = new Revision(GlobalVariables.currentRevision.get());
        if (revision.equals(currentRevision)) {
            return counter.get(trigramFile.trigram(), trigramFile.file());
        } else {
            checkout(revision);
            var ans = counter.get(trigramFile.trigram(), trigramFile.file());
            checkout(currentRevision);
            return ans;
        }
    }

    @Override
    public void checkout(Revision targetRevision) {
        cache.cacheRevision(targetRevision);
        var currentRevision = new Revision(GlobalVariables.currentRevision.get());
        var targetCounter = counter.copy();
        while (!currentRevision.equals(targetRevision)) {
            if (currentRevision.revision() > targetRevision.revision()) {
                targetCounter.decrease(cache.getDataCluster(currentRevision));
                currentRevision = cache.getParent(currentRevision);
            } else {
                targetCounter.add(cache.getDataCluster(targetRevision));
                targetRevision = cache.getParent(targetRevision);
            }
        }
        counter = targetCounter;
    }





    private class Preparer {

        public void process(List<Change> changes) {
            var delta = new TrigramFileCounter();
            changes.forEach(it -> countChange(it, delta));
            counter.add(delta);
            var deltas = new ArrayList<TrigramDataFileCluster.TrigramFileDelta>();
            delta.forEach((it) -> deltas.add(new TrigramDataFileCluster.TrigramFileDelta(it.trigram(), it.file(), it.value())));
            if (!changes.isEmpty()) pushActions(deltas, changes.get(0).getTimestamp());
        }

        private boolean validateFilename(String filename) {
//            if (filename.endsWith("java")) {
//                return true;
//            }
            return Stream.of(".java"/*, ".txt", ".kt", ".py"*/).anyMatch(filename::endsWith);
        }

        private boolean validateChange(Change change) {
            List<String> filenames = switch (change) {
                case FileChange fileChange -> List.of(fileChange.getPlace().file().getName());
                case FileHolderChange fileHolderChange -> List.of(fileHolderChange.getOldFileName().getName(),
                        fileHolderChange.getNewFileName().getName());
            };
            return filenames.stream().anyMatch(this::validateFilename);
        }

        private void countChange(Change change, TrigramFileCounter delta) {
            if (!validateChange(change)) return;
            switch (change) {
                case AddChange addChange ->
                        delta.add(addChange.getPlace().file(), getTrigramsCount(addChange.getAddedString()));
                case ModifyChange modifyChange -> {
                    delta.decrease(modifyChange.getOldFileName(), getTrigramsCount(modifyChange.getOldFileContent()));
                    delta.add(modifyChange.getNewFileName(), getTrigramsCount(modifyChange.getNewFileContent()));
                }
                case CopyChange copyChange ->
                        delta.add(copyChange.getNewFileName(), getTrigramsCount(copyChange.getNewFileName()));
                case RenameChange renameChange -> {
                    delta.decrease(renameChange.getOldFileName(), getTrigramsCount(renameChange.getOldFileContent()));
                    delta.add(renameChange.getNewFileName(), getTrigramsCount(renameChange.getNewFileContent()));
                }
                case DeleteChange deleteChange ->
                        delta.add(deleteChange.getPlace().file(), getTrigramsCount(deleteChange.getDeletedString()));
            }
        }
    }
}
