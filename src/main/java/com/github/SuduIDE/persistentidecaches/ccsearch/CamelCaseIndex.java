package com.github.SuduIDE.persistentidecaches.ccsearch;

import com.github.SuduIDE.persistentidecaches.Index;
import com.github.SuduIDE.persistentidecaches.changes.AddChange;
import com.github.SuduIDE.persistentidecaches.changes.Change;
import com.github.SuduIDE.persistentidecaches.changes.ModifyChange;
import com.github.SuduIDE.persistentidecaches.records.FilePointer;
import com.github.SuduIDE.persistentidecaches.records.Revision;
import com.github.SuduIDE.persistentidecaches.records.Trigram;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

public class CamelCaseIndex implements Index<String, String> {

    public static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("[A-Za-z][a-z0-9]*([A-Z][a-z0-9]*)*");
    Map<TrigramPriorityWord, Long> classCounter = new HashMap<>();
    Map<TrigramPriorityWord, Long> fieldCounter = new HashMap<>();
    Map<TrigramPriorityWord, Long> methodCounter = new HashMap<>();

    private static <T extends Node & NodeWithSimpleName<?>> void findInClassMapAndAddNameInSet(
            final CompilationUnit compilationUnit,
            final Class<T> token,
            final Set<String> symbols) {
        compilationUnit.findAll(token).stream()
                .map(NodeWithSimpleName::getNameAsString)
                .forEach(symbols::add);
    }

    private static <T extends Node, V extends NodeWithSimpleName<?>> void findInClassMapAndAddNameInSet(
            final CompilationUnit compilationUnit,
            @SuppressWarnings("SameParameterValue") final Class<T> token,
            final Function<T, Stream<V>> flatMapper,
            final Set<String> symbols) {
        compilationUnit.findAll(token).stream()
                .flatMap(flatMapper)
                .map(NodeWithSimpleName::getNameAsString)
                .forEach(symbols::add);
    }

    public static Symbols getSymbolsFromString(final String javaFile) {
        final var compilationUnit = StaticJavaParser.parse(javaFile);
        final var symbols = new Symbols(new HashSet<>(), new HashSet<>(), new HashSet<>());
        findInClassMapAndAddNameInSet(compilationUnit, ClassOrInterfaceDeclaration.class,
                symbols.classOrInterfaceSymbols());
        findInClassMapAndAddNameInSet(compilationUnit, MethodDeclaration.class, symbols.methodSymbols());
        findInClassMapAndAddNameInSet(compilationUnit, FieldDeclaration.class,
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

    @Override
    public void processChanges(final List<? extends Change> changes) {
        changes.forEach(change -> {
                    switch (change) {
                        case ModifyChange modifyChange -> processModifyChange(modifyChange);
                        case AddChange addChange -> processAddChange(addChange);
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
        processAddChange(new AddChange(modifyChange.getTimestamp(),
                new FilePointer(modifyChange.getNewFileName(), 0),
                modifyChange.getNewFileContent()));
    }

    @Override
    public String getValue(final String s, final Revision revision) {
        return null;
    }

    private void processAddChange(final AddChange change) {
        final var symbolsFile = getSymbolsFromString(change.getAddedString());
        classCounter = symbolsFile.classOrInterfaceSymbols().stream()
                .map(it -> Pair.of(it, getInterestTrigrams(it)))
                .flatMap(pair -> pair.getValue().stream()
                        .map(trigram -> new TrigramPriorityWord(trigram, getPriority(trigram, pair.getKey()), pair.getKey())))
                .collect(Collectors.groupingBy(it -> it, Collectors.counting()));

    }

    @Override
    public void checkout(final Revision revision) {

    }
}
