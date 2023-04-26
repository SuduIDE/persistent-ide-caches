package com.github.SuduIDE.persistentidecaches.ccsearch;

import com.github.SuduIDE.persistentidecaches.Index;
import com.github.SuduIDE.persistentidecaches.changes.AddChange;
import com.github.SuduIDE.persistentidecaches.changes.Change;
import com.github.SuduIDE.persistentidecaches.changes.DeleteChange;
import com.github.SuduIDE.persistentidecaches.changes.ModifyChange;
import com.github.SuduIDE.persistentidecaches.lmdb.CountingCacheImpl;
import com.github.SuduIDE.persistentidecaches.records.FilePointer;
import com.github.SuduIDE.persistentidecaches.records.Revision;
import com.github.SuduIDE.persistentidecaches.records.Trigram;
import com.github.SuduIDE.persistentidecaches.symbols.Symbol;
import com.github.SuduIDE.persistentidecaches.symbols.Symbols;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.lmdbjava.Env;

public class CamelCaseIndex implements Index<String, String> {

    public static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("[A-Za-z][a-z0-9]*([A-Z][a-z0-9]*)*");
    private final CountingCacheImpl<Symbol> symbolCache;
    private final CountingCacheImpl<Path> pathCache;
    private final TrigramSymbolCounterLmdb classCounter;
    private final TrigramSymbolCounterLmdb fieldCounter;
    private final TrigramSymbolCounterLmdb methodCounter;

    public CamelCaseIndex(final Env<ByteBuffer> env, final CountingCacheImpl<Symbol> symbolCache,
            final CountingCacheImpl<Path> pathCache) {
        this.symbolCache = symbolCache;
        this.pathCache = pathCache;
        methodCounter = new TrigramSymbolCounterLmdb(env, symbolCache, "method");
        fieldCounter = new TrigramSymbolCounterLmdb(env, symbolCache, "field");
        classCounter = new TrigramSymbolCounterLmdb(env, symbolCache, "class");
    }

    private static <T extends Node & NodeWithSimpleName<?>> void findInClassMapAndAddNameInList(
            final CompilationUnit compilationUnit,
            final Class<T> token,
            final List<String> symbols) {
        compilationUnit.findAll(token).stream()
                .map(NodeWithSimpleName::getNameAsString)
                .forEach(symbols::add);
    }

    private static <T extends Node, V extends NodeWithSimpleName<?>> void findInClassMapAndAddNameInList(
            final CompilationUnit compilationUnit,
            @SuppressWarnings("SameParameterValue") final Class<T> token,
            final Function<T, Stream<V>> flatMapper,
            final List<String> symbols) {
        compilationUnit.findAll(token).stream()
                .flatMap(flatMapper)
                .map(NodeWithSimpleName::getNameAsString)
                .forEach(symbols::add);
    }

    public static Symbols getSymbolsFromString(final String javaFile) {
        final CompilationUnit compilationUnit;
        try {
            compilationUnit = StaticJavaParser.parse(javaFile);
        } catch (final Exception e) {
            System.err.println("Something went wrong on parsing java file: " + e.getMessage());
            return new Symbols(List.of(), List.of(), List.of());
        }
        final var symbols = new Symbols(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        findInClassMapAndAddNameInList(compilationUnit, ClassOrInterfaceDeclaration.class,
                symbols.classOrInterfaceSymbols());
        findInClassMapAndAddNameInList(compilationUnit, MethodDeclaration.class, symbols.methodSymbols());
        findInClassMapAndAddNameInList(compilationUnit, FieldDeclaration.class,
                (FieldDeclaration it) -> it.getVariables().stream(),
                symbols.fieldSymbols()
        );
        return symbols;
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
                .flatMap(pair -> pair.getValue().stream()
                        .map(trigram -> new TrigramSymbol(trigram, new Symbol(pair.getKey(), fileNum))))
                .collect(Collectors.groupingBy(it -> it, Collectors.summingInt(e -> 1)));
    }

    @Override
    public void processChanges(final List<? extends Change> changes) {
        changes.forEach(change -> {
                    switch (change) {
                        case final ModifyChange modifyChange -> processModifyChange(modifyChange);
                        case final AddChange addChange -> processAddChange(addChange);
                        case final DeleteChange deleteChange -> processDeleteChange(deleteChange);
                        default -> {
                        }
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
                new DeleteChange(modifyChange.getTimestamp(),
                        new FilePointer(modifyChange.getOldFileName(), 0),
                        modifyChange.getOldFileContent())
        );
        processAddChange(
                new AddChange(modifyChange.getTimestamp(),
                        new FilePointer(modifyChange.getNewFileName(), 0),
                        modifyChange.getNewFileContent()));
    }

    private void processDeleteChange(final DeleteChange change) {
        final var symbolsFile = getSymbolsFromString(change.getDeletedString());
        final var fileNum = pathCache.getNumber(change.getPlace().file());
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

    private void processAddChange(final AddChange change) {
        final var symbolsFile = getSymbolsFromString(change.getAddedString());
        final var fileNum = pathCache.getNumber(change.getPlace().file());
        symbolsFile.concatedStream().forEach(it -> symbolCache.tryRegisterNewObj(
                new Symbol(it, pathCache.getNumber(change.getPlace().file()))));
        classCounter.add(collectCounter(symbolsFile.classOrInterfaceSymbols(), fileNum));
        fieldCounter.add(collectCounter(symbolsFile.fieldSymbols(), fileNum));
        methodCounter.add(collectCounter(symbolsFile.methodSymbols(), fileNum));
//        print(change.getPlace());
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
