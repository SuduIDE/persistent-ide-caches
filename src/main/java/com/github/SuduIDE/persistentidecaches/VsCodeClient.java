package com.github.SuduIDE.persistentidecaches;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.SuduIDE.persistentidecaches.ccsearch.CamelCaseIndex;
import com.github.SuduIDE.persistentidecaches.ccsearch.CamelCaseIndexUtils;
import com.github.SuduIDE.persistentidecaches.ccsearch.Matcher;
import com.github.SuduIDE.persistentidecaches.changes.AddChange;
import com.github.SuduIDE.persistentidecaches.changes.Change;
import com.github.SuduIDE.persistentidecaches.changes.DeleteChange;
import com.github.SuduIDE.persistentidecaches.changes.ModifyChange;
import com.github.SuduIDE.persistentidecaches.changes.RenameChange;
import com.github.SuduIDE.persistentidecaches.records.FilePointer;
import com.github.SuduIDE.persistentidecaches.trigram.TrigramIndex;
import com.github.SuduIDE.persistentidecaches.trigram.TrigramIndexUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VsCodeClient {

    public static final String SEARCH = "search";
    public static final String CHANGES = "changes";
    public static final int BUSY_WAITING_MILLIS = 500;
    public static final String CHECKOUT = "checkout";
    public static final String CCSEARCH = "ccsearch";
    public static final int BUCKET_SIZE = 10;
    public static final String NEXT = "next";
    public static final String PREV = "prev";
    private static final char[] BUFFER = new char[16384];
    private static List<String> returned;
    private static int currentPos;
    private static long time;

    private static void printUsage() {
        System.err.println("""
            Usage:
            java VsCodeClient path_to_repository reset_db parseAll/parseHead trigram_index camel_case_index
            
            path_to_repository -- absolute path to repository
            reset_db -- true/false to reset databases
            parseAll/parseHead -- "parseAll" or "parseHead" to parse all refs or only HEAD
            trigram_index  -- true/false to enable trigram index
            camel_case_index  -- true/false to enable camel case index
            """
        );
    }

    @SuppressWarnings({"BusyWait", "InfiniteLoopStatement"})
    public static void main(final String[] args) throws IOException, InterruptedException {
        if (args.length != 5) {
            System.err.println("Wrong usage");
            printUsage();
        }
        try (final IndexesManager manager = new IndexesManager(args[1].equals("true"))) {
            TrigramIndex trigramHistoryIndex = null;
            final TrigramIndexUtils trigramIndexUtils;
            CamelCaseIndex camelCaseSearch = null;
            final CamelCaseIndexUtils camelCaseSearchUtils;
            if (args[3].equals("true")) {
                trigramHistoryIndex = manager.addTrigramIndex();
                trigramIndexUtils = trigramHistoryIndex.getTrigramIndexUtils();
            } else {
                trigramIndexUtils = null;
            }
            if (args[4].equals("true")) {
                camelCaseSearch = manager.addCamelCaseIndex();
                camelCaseSearchUtils = camelCaseSearch.getUtils();
            } else {
                camelCaseSearchUtils = null;
            }
            final var repPath = Path.of(args[0]);
            manager.parseGitRepository(repPath, args[3].equals("parseHead"));

            final ObjectMapper objectMapper = new ObjectMapper();
            final BufferedReader scanner = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = scanner.readLine();
                Thread.sleep(BUSY_WAITING_MILLIS);
                switch (line) {
                    case CHANGES -> {
                        final var read = scanner.read(BUFFER);
                        line = new String(BUFFER, 0, read);
                        final Changes changes = objectMapper.readValue(line, Changes.class);
                        final List<Change> processedChangesList = new ArrayList<>();
                        for (final ModifyChangeFromJSON modifyChangeFromJSON : changes.modifyChanges) {
                            final Path path = repPath.relativize(Path.of(modifyChangeFromJSON.uri));
                            final ModifyChange modifyChange = new ModifyChange(changes.timestamp,
                                () -> modifyChangeFromJSON.oldText,
                                () -> modifyChangeFromJSON.newText,
                                path,
                                path);
                            processedChangesList.add(modifyChange);
                        }
                        for (final CreateFileChangeFromJSON createFileChangeFromJSON : changes.addChanges) {
                            final AddChange addChange = new AddChange(changes.timestamp,
                                new FilePointer(repPath.relativize(Path.of(createFileChangeFromJSON.uri)), 0),
                                createFileChangeFromJSON.text);
                            processedChangesList.add(addChange);
                        }
                        for (final DeleteFileChangeFromJSON deleteFileChangeFromJSON : changes.deleteChanges) {
                            final DeleteChange deleteChange = new DeleteChange(changes.timestamp,
                                new FilePointer(repPath.relativize(Path.of(deleteFileChangeFromJSON.uri)), 0),
                                deleteFileChangeFromJSON.text);
                            processedChangesList.add(deleteChange);
                        }
                        for (final RenameFileChangeFromJSON renameFileChangeFromJSON : changes.renameChanges) {
                            final RenameChange renameChange = new RenameChange(changes.timestamp,
                                () -> renameFileChangeFromJSON.text,
                                () -> renameFileChangeFromJSON.text,
                                repPath.relativize(Path.of(renameFileChangeFromJSON.oldUri)),
                                repPath.relativize(Path.of(renameFileChangeFromJSON.newUri)));
                            processedChangesList.add(renameChange);
                        }
                        manager.nextRevision();
                        manager.applyChanges(processedChangesList);
                    }
                    case SEARCH -> {
                        final var read = scanner.read(BUFFER);
                        final var l = new String(BUFFER, 0, read);
                        currentPos = 0;
                        checkTime(() ->
                            returned = trigramIndexUtils.filesForString(l)
                                .stream()
                                .map(Path::toString)
                                .toList());
                        sendCurrentBucket();
                    }
                    case CHECKOUT -> {
                        final var read = scanner.read(BUFFER);
                        final var l = new String(BUFFER, 0, read);
                        checkTime(() -> manager.checkoutToGitRevision(l));
                        System.out.println(time);
                    }
                    case CCSEARCH -> {
                        final var read = scanner.read(BUFFER);
                        final var req = new String(BUFFER, 0, read);
                        checkTime(() -> returned =
                            camelCaseSearchUtils.getSymbolsFromAny(req).stream()
                                .map(it -> Stream.of(Stream.of(it.name()),
                                        Stream.of(manager.getFileCache().getObject(it.pathNum())),
                                        Arrays.stream(Matcher.letters(req, it.name())).mapToObj(Integer::toString))
                                    .flatMap(Function.identity())
                                    .map(Objects::toString)
                                    .collect(Collectors.joining(" ")))
                                .toList());
                        currentPos = 0;
                        sendCurrentBucket();
                    }
                    case NEXT -> {
                        currentPos += BUCKET_SIZE;
                        System.err.println("Next " + currentPos + " of " + returned.size());
                        sendCurrentBucket();
                    }
                    case PREV -> {
                        currentPos -= BUCKET_SIZE;
                        System.err.println("Prev " + currentPos + " of " + returned.size());
                        sendCurrentBucket();
                    }
                }
            }
        }
    }

    private static void checkTime(final Runnable runnable) {
        final long start = System.nanoTime();
        runnable.run();
        time = (System.nanoTime() - start) / 1_000_000;
    }

    private static void sendCurrentBucket() {
        System.out.println(
            Stream.concat(Stream.of(returned.size(), time).map(Object::toString),
                    returned.subList(currentPos, Math.min(currentPos + BUCKET_SIZE, returned.size())).stream())
                .collect(Collectors.joining("\n")));
    }

    private record ModifyChangeFromJSON(String uri, String oldText, String newText) {

    }

    private record CreateFileChangeFromJSON(String uri, String text) {

    }

    private record DeleteFileChangeFromJSON(String uri, String text) {

    }

    private record RenameFileChangeFromJSON(String oldUri, String newUri, String text) {

    }

    private record Changes(List<ModifyChangeFromJSON> modifyChanges,
                           List<CreateFileChangeFromJSON> addChanges,
                           List<DeleteFileChangeFromJSON> deleteChanges,
                           List<RenameFileChangeFromJSON> renameChanges,
                           long timestamp) {

    }
}