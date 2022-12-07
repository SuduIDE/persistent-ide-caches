package caches.trigram;

import caches.Index;
import caches.changes.*;
import caches.records.Revision;
import caches.records.Trigram;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public class TrigramHistoryIndex implements Index<String, List<Revision>> {

    public static final String DIRECTORY = ".trigrams/";
    public final Preparer preparer = new Preparer();
    private final Map<Trigram, File> trigramsFiles = new HashMap<>();

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
        changes.forEach(this::processChange);
    }

    private void processChange(Change change) {
        switch (change) {
            case AddChange addChange:
                var trigrams = getTrigramsCount(addChange.getAddedString());
                trigrams.decrease(getTrigramsCount(addChange.getPlace().file()));
                trigrams.getAsMap().entrySet().stream()
                        .filter((entry) -> entry.getValue() == 0)
                        .forEach((entry) -> {
                            pushNewNode(entry.getKey(), change.getTimestamp(), TrigramNode.Action.ADD);
                        });
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + change);
        }
    }

    private void pushNewNode(Trigram trigram, long timestamp, TrigramNode.Action action) {
        try {
            if (!trigramsFiles.containsKey(trigram)) {
                var file = generateFileName(trigram);
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException("Can't create file" + file.getAbsolutePath(), e);
                }
                trigramsFiles.put(trigram, file);
            }
            TrigramCache.pushNode(trigramsFiles.get(trigram), timestamp,
                    action);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Not found file" + trigramsFiles.get(trigram), e);
        }
    }

    private File generateFileName(Trigram trigram) {
        var stringBuilder = new StringBuilder();
        trigram.trigram().codePoints().forEachOrdered(it -> stringBuilder.append(it).append("_"));
        return new File(DIRECTORY + stringBuilder);
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

    private class Preparer {
        private final TrigramCounter counter = new TrigramCounter();

        public void process(List<Change> changes) {
            var addCounter = new TrigramFileCounter();
            var deleteCounter = new TrigramFileCounter();
            changes.forEach(it -> countChange(it, addCounter, deleteCounter));
            List<TrigramNode.FileAction> fileActions = new ArrayList<>();
            // TODO
//            addCounter.getAsMap().forEach((trigram, map) ->
//            {
//                map.forEach((file, added) -> {
//                            if (counter.get(trigram) == 0 && added != 0) {
//                                fileActions.add(new TrigramNode.FileAction(file, ))
//                            }
//                        }
//                );
//            });
//            deleteCounter.getAsMap().forEach(((trigram, deleted) -> {
//                if (counter.get(trigram) == deleted && addCounter.get(trigram) == 0) {
//                    pushNewNode(trigram, changes.get(0).getTimestamp(), TrigramNode.Action.DELETE);
//                }
//            }));
//            counter.add(addCounter);
//            counter.decrease(deleteCounter);
        }

        private boolean validateFilename(String filename) {
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

        private void countChange(Change change, TrigramFileCounter addCounter, TrigramFileCounter deleteCounter) {
            if (!validateChange(change)) return;
            switch (change) {
                case AddChange addChange ->
                        addCounter.add(addChange.getPlace().file(), getTrigramsCount(addChange.getAddedString()));
                case ModifyChange modifyChange -> {
                    deleteCounter.add(modifyChange.getOldFileName(), getTrigramsCount(modifyChange.getOldFileContent()));
                    addCounter.add(modifyChange.getNewFileName(), getTrigramsCount(modifyChange.getNewFileContent()));
                }
                case CopyChange copyChange ->
                        addCounter.add(copyChange.getNewFileName(), getTrigramsCount(copyChange.getNewFileName()));
                case RenameChange renameChange -> {
                    deleteCounter.add(renameChange.getOldFileName(), getTrigramsCount(renameChange.getOldFileContent()));
                    addCounter.add(renameChange.getNewFileName(), getTrigramsCount(renameChange.getNewFileContent()));
                }
                case DeleteChange deleteChange ->
                        deleteCounter.add(deleteChange.getPlace().file(), getTrigramsCount(deleteChange.getDeletedString()));
            }
        }
    }
}
