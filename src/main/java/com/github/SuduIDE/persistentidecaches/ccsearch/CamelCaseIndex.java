package com.github.SuduIDE.persistentidecaches.ccsearch;

import com.github.SuduIDE.persistentidecaches.Index;
import com.github.SuduIDE.persistentidecaches.changes.AddChange;
import com.github.SuduIDE.persistentidecaches.changes.Change;
import com.github.SuduIDE.persistentidecaches.changes.ModifyChange;
import com.github.SuduIDE.persistentidecaches.records.FilePointer;
import com.github.SuduIDE.persistentidecaches.records.Revision;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CamelCaseIndex implements Index<String, String> {

    public static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("[A-Za-z][a-z0-9]*([A-Z][a-z0-9]*)*");
    private final Symbols symbols = new Symbols(new HashSet<>(), new HashSet<>(), new HashSet<>());

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
        findInClassMapAndAddNameInSet(compilationUnit, FieldDeclaration.class,
                (FieldDeclaration it) -> it.getVariables().stream(),
                symbols.fieldSymbols()
        );
        findInClassMapAndAddNameInSet(compilationUnit, MethodDeclaration.class, symbols.methodSymbols());
        return symbols;
    }

    static boolean isCamelCase(final String name) {
        return CAMEL_CASE_PATTERN.matcher(name).matches();
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

    }

    @Override
    public void checkout(final Revision revision) {

    }
}
