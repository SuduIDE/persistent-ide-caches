package caches;

import caches.changes.AddChange;
import caches.changes.Change;
import caches.changes.DeleteChange;
import caches.changes.ModifyChange;
import caches.changes.RenameChange;
import caches.records.FilePointer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VsCodeClient {

    public static final String SEARCH = "search";
    public static final String CHANGES = "changes";
    public static final int BUSY_WAITING_MILLIS = 500;
    private static final char[] BUFFER = new char[16384];

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 1) {
            throw new RuntimeException("Needs path to repository as first arg");
        }
        try (IndexesManager manager = new IndexesManager(true)) {
            var trigramHistoryIndex = manager.addTrigramIndex();
            var trigramIndexUtils = trigramHistoryIndex.getTrigramIndexUtils();
            manager.parseGitRepository(Path.of(args[0]));

            ObjectMapper objectMapper = new ObjectMapper();
            BufferedReader scanner = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = scanner.readLine();
                Thread.sleep(BUSY_WAITING_MILLIS);
                if (line.equals(CHANGES)) {
                    var read = scanner.read(BUFFER);
                    line = new String(BUFFER, 0, read);
                    Changes changes = objectMapper.readValue(line, Changes.class);
                    List<Change> processedChangesList = new ArrayList<>();
                    for (ModifyChangeFromJSON modifyChangeFromJSON : changes.modifyChanges) {
                        ModifyChange modifyChange = new ModifyChange(modifyChangeFromJSON.timestamp,
                                () -> modifyChangeFromJSON.oldText,
                                () -> modifyChangeFromJSON.newText,
                                new File(modifyChangeFromJSON.uri),
                                new File(modifyChangeFromJSON.uri));
                        processedChangesList.add(modifyChange);
                    }
                    for (CreateFileChangeFromJSON createFileChangeFromJSON : changes.createChanges) {
                        AddChange addChange = new AddChange(createFileChangeFromJSON.timestamp,
                                new FilePointer(new File(createFileChangeFromJSON.uri), 0),
                                createFileChangeFromJSON.text);
                        processedChangesList.add(addChange);
                    }
                    for (DeleteFileChangeFromJSON deleteFileChangeFromJSON : changes.deleteChanges) {
                        DeleteChange deleteChange = new DeleteChange(deleteFileChangeFromJSON.timestamp,
                                new FilePointer(new File(deleteFileChangeFromJSON.uri), 0),
                                deleteFileChangeFromJSON.text);
                        processedChangesList.add(deleteChange);
                    }
                    for (RenameFileChangeFromJSON renameFileChangeFromJSON : changes.renameChanges) {
                        RenameChange renameChange = new RenameChange(renameFileChangeFromJSON.timestamp,
                                () -> renameFileChangeFromJSON.text,
                                () -> renameFileChangeFromJSON.text,
                                new File(renameFileChangeFromJSON.oldUri),
                                new File(renameFileChangeFromJSON.newUri));
                        processedChangesList.add(renameChange);
                    }
                    manager.applyChanges(processedChangesList);
                } else if (line.equals(SEARCH)) {
                    var read = scanner.read(BUFFER);
                    line = new String(BUFFER, 0, read);
                    System.out.println(trigramIndexUtils.filesForString(line));
                }
            }
        }
    }

    private record ModifyChangeFromJSON(long timestamp, String uri, String oldText, String newText) {

    }

    private record CreateFileChangeFromJSON(long timestamp, String uri, String text) {

    }

    private record DeleteFileChangeFromJSON(long timestamp, String uri, String text) {

    }

    private record RenameFileChangeFromJSON(long timestamp, String oldUri, String newUri, String text) {

    }

    private record Changes(List<ModifyChangeFromJSON> modifyChanges,
                           List<CreateFileChangeFromJSON> createChanges,
                           List<DeleteFileChangeFromJSON> deleteChanges,
                           List<RenameFileChangeFromJSON> renameChanges) {

    }
}