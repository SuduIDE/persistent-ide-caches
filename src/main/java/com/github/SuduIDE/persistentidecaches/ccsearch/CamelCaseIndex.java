package com.github.SuduIDE.persistentidecaches.ccsearch;

import com.github.SuduIDE.persistentidecaches.Index;
import com.github.SuduIDE.persistentidecaches.IndexesManager;
import com.github.SuduIDE.persistentidecaches.changes.AddChange;
import com.github.SuduIDE.persistentidecaches.changes.Change;
import com.github.SuduIDE.persistentidecaches.changes.DeleteChange;
import com.github.SuduIDE.persistentidecaches.changes.ModifyChange;
import com.github.SuduIDE.persistentidecaches.lmdb.CountingCacheImpl;
import com.github.SuduIDE.persistentidecaches.lmdb.LmdbRevisionFile2ListString;
import com.github.SuduIDE.persistentidecaches.records.FilePointer;
import com.github.SuduIDE.persistentidecaches.records.Revision;
import com.github.SuduIDE.persistentidecaches.records.Trigram;
import com.github.SuduIDE.persistentidecaches.symbols.Symbol;
import com.github.SuduIDE.persistentidecaches.symbols.Symbols;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.lmdbjava.Env;

public class CamelCaseIndex implements Index<String, String> {

    public static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("[A-Za-z][a-z0-9]*([A-Z][a-z0-9]*)*");
    private final IndexesManager manager;
    private final CountingCacheImpl<Symbol> symbolCache;
    private final CountingCacheImpl<Path> pathCache;
    private final TrigramSymbolCounterLmdb classCounter;
    private final TrigramSymbolCounterLmdb fieldCounter;
    private final TrigramSymbolCounterLmdb methodCounter;
    private final LmdbRevisionFile2ListString classCache;
    private final LmdbRevisionFile2ListString fieldCache;
    private final LmdbRevisionFile2ListString methodCache;

    public CamelCaseIndex(final IndexesManager manager, final Env<ByteBuffer> env,
            final CountingCacheImpl<Symbol> symbolCache,
            final CountingCacheImpl<Path> pathCache, final Env<ByteBuffer> cacheEnv) {
        this.manager = manager;
        this.symbolCache = symbolCache;
        this.pathCache = pathCache;
        methodCounter = new TrigramSymbolCounterLmdb(env, symbolCache, "method");
        fieldCounter = new TrigramSymbolCounterLmdb(env, symbolCache, "field");
        classCounter = new TrigramSymbolCounterLmdb(env, symbolCache, "class");
        methodCache = new LmdbRevisionFile2ListString(cacheEnv, "method_parse_cache");
        fieldCache = new LmdbRevisionFile2ListString(cacheEnv, "field_parse_cache");
        classCache = new LmdbRevisionFile2ListString(cacheEnv, "class_parse_cache");
    }

    public static Symbols getSymbolsFromString(final String javaFile) {
        final var symbols = JavaSymbolListener.getSymbolsFromString(javaFile);
        return new Symbols(
                symbols.classOrInterfaceSymbols().stream().filter(CamelCaseIndex::isCamelCase).toList(),
                symbols.fieldSymbols().stream().filter(CamelCaseIndex::isCamelCase).toList(),
                symbols.methodSymbols().stream().filter(CamelCaseIndex::isCamelCase).toList()
        );
    }

    static boolean isCamelCase(final String name) {
        return CAMEL_CASE_PATTERN.matcher(name).matches();
    }

    static List<Trigram> getInterestTrigrams(final String symbolName) {
        final String[] parts = symbolName.split("(?=[A-Z])");
        final List<Trigram> trigrams = new ArrayList<>();
        final var normalizedParts = Stream.concat(Stream.of("$"), Arrays.stream(parts)).map(String::getBytes).toList();
        for (int partIndex = 0; partIndex < normalizedParts.size(); partIndex++) {
            final var part = normalizedParts.get(partIndex);
            for (int indexInPart = 0; indexInPart < normalizedParts.get(partIndex).length; indexInPart++) {
                final var thisByte = part[indexInPart];
                if (indexInPart + 2 < normalizedParts.get(partIndex).length) {
                    trigrams.add(new Trigram(new byte[]{
                            thisByte,
                            part[indexInPart + 1],
                            part[indexInPart + 2]
                    }));
                }
                if (indexInPart + 1 < normalizedParts.get(partIndex).length && partIndex + 1 < normalizedParts.size()) {
                    trigrams.add(new Trigram(new byte[]{
                            thisByte,
                            part[indexInPart + 1],
                            normalizedParts.get(partIndex + 1)[0]
                    }));
                }
                if (partIndex + 1 < normalizedParts.size() && normalizedParts.get(partIndex + 1).length >= 2) {
                    trigrams.add(new Trigram(new byte[]{
                            thisByte,
                            normalizedParts.get(partIndex + 1)[0],
                            normalizedParts.get(partIndex + 1)[1]
                    }));
                }
                if (partIndex + 2 < normalizedParts.size()) {
                    trigrams.add(new Trigram(new byte[]{
                            thisByte,
                            normalizedParts.get(partIndex + 1)[0],
                            normalizedParts.get(partIndex + 2)[0]
                    }));
                }
            }
        }
        return trigrams;
    }

    static int getPriority(final Trigram trigram, final String word) {
        return 1;
    }

    private static Map<TrigramSymbol, Integer> collectCounter(final List<String> symbols, final int fileNum) {
        return symbols.stream()
                .map(it -> Pair.of(it, getInterestTrigrams(it)))
                .map(it -> Pair.of(it.getLeft(), it.getRight().stream().map(Trigram::toLowerCase).toList()))
                .map(it -> Pair.of(new Symbol(it.getKey(), fileNum), it.getRight()))
                .flatMap(pair -> pair.getValue().stream()
                        .map(trigram -> new TrigramSymbol(trigram, pair.getKey())))
                .collect(Collectors.groupingBy(it -> it, Collectors.summingInt(e -> 1)));
    }

    public Symbols getSymbolsFromStringWithCache(final Supplier<String> javaFile, final int pathNum, final int revision) {
        final List<String> classCached = classCache.get(revision, pathNum);
        if (classCached != null) {
            final List<String> methodCached = methodCache.get(revision, pathNum);
            final List<String> fieldCached = fieldCache.get(revision, pathNum);
            return new Symbols(classCached, fieldCached, methodCached);
        } else {
            final Symbols symbols = getSymbolsFromString(javaFile.get());
            classCache.put(revision, pathNum, symbols.classOrInterfaceSymbols());
            methodCache.put(revision, pathNum, symbols.methodSymbols());
            fieldCache.put(revision, pathNum, symbols.fieldSymbols());
            return symbols;
        }
    }

    @Override
    public void processChanges(final List<? extends Change> changes) {
        changes.forEach(change -> {
                    if (Objects.requireNonNull(change) instanceof final ModifyChange modifyChange) {
                        processModifyChange(modifyChange);
                    } else if (change instanceof final AddChange addChange) {
                        processAddChange(addChange.getPlace().file(), addChange::getAddedString);
                    } else if (change instanceof final DeleteChange deleteChange) {
                        processDeleteChange(deleteChange.getPlace().file(), deleteChange::getDeletedString);
                    }
                }
        );
    }

    @Override
    public void prepare(final List<? extends Change> changes) {
        processChanges(changes);
    }

    private void processModifyChange(final ModifyChange modifyChange) {
        processDeleteChange(
                modifyChange.getOldFileName(), modifyChange.getOldFileGetter()
        );
        processAddChange(
                modifyChange.getNewFileName(), modifyChange.getNewFileGetter()
        );
    }

    private void processAddChange(final Path newFileName, final Supplier<String> newFileContent) {
        final var fileNum = pathCache.getNumber(newFileName);
        final var symbolsFile = getSymbolsFromStringWithCache(newFileContent, fileNum,
                manager.getRevisions().getCurrentRevision().revision());
        symbolsFile.concatedStream().forEach(it -> symbolCache.tryRegisterNewObj(
                new Symbol(it, pathCache.getNumber(newFileName))));
        classCounter.add(collectCounter(symbolsFile.classOrInterfaceSymbols(), fileNum));
        fieldCounter.add(collectCounter(symbolsFile.fieldSymbols(), fileNum));
        methodCounter.add(collectCounter(symbolsFile.methodSymbols(), fileNum));
//        print(change.getPlace());
    }

    private void processDeleteChange(final Path oldFilePath, final Supplier<String> oldFileContent) {
        final var fileNum = pathCache.getNumber(oldFilePath);
        final var symbolsFile = getSymbolsFromStringWithCache(oldFileContent, fileNum,
                -manager.getRevisions().getCurrentRevision().revision());
        classCounter.decrease(collectCounter(symbolsFile.classOrInterfaceSymbols(), fileNum));
        fieldCounter.decrease(collectCounter(symbolsFile.fieldSymbols(), fileNum));
        methodCounter.decrease(collectCounter(symbolsFile.methodSymbols(), fileNum));
//        print(change.getPlace());
    }

    private void print(final FilePointer place) {
        if (place.file().endsWith("Implementor.java")) {
            classCounter.forEach(((trigram, symbol, integer) -> System.out.println(symbol + " " + integer)));
        }
    }

    @Override
    public String getValue(final String s, final Revision revision) {
        return null;
    }


    @Override
    public void checkout(final Revision revision) {
//        throw new UnsupportedOperationException();
    }

    public CamelCaseIndexUtils getUtils() {
        return new CamelCaseIndexUtils(this);
    }

    public TrigramSymbolCounterLmdb getClassCounter() {
        return classCounter;
    }

    public TrigramSymbolCounterLmdb getFieldCounter() {
        return fieldCounter;
    }

    public TrigramSymbolCounterLmdb getMethodCounter() {
        return methodCounter;
    }
}
