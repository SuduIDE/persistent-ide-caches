package com.github.SuduIDE.persistentidecaches.ccsearch;

import com.github.SuduIDE.persistentidecaches.Index;
import com.github.SuduIDE.persistentidecaches.changes.AddChange;
import com.github.SuduIDE.persistentidecaches.changes.Change;
import com.github.SuduIDE.persistentidecaches.changes.ModifyChange;
import com.github.SuduIDE.persistentidecaches.records.Revision;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CamelCaseIndex implements Index<String, String> {

    private static <T extends Node & NodeWithSimpleName<?>> void findInClassMapAndAddNameInList(
            final CompilationUnit compilationUnit,
            final Class<T> token,
            final List<String> symbols) {
        findInClassMapAndAddNameInList(compilationUnit, token, Function.identity(), symbols);
    }

    private static <T extends Node, V extends NodeWithSimpleName<?>> void findInClassMapAndAddNameInList(
            final CompilationUnit compilationUnit,
            final Class<T> token,
            final Function<T, V> mapper,
            final List<String> symbols) {
        compilationUnit.findAll(token).stream()
                .map(mapper)
                .map(NodeWithSimpleName::getNameAsString)
                .forEach(symbols::add);
    }

    public static Symbols getSymbolsFromString(final String javaFile) {
        final var compilationUnit = StaticJavaParser.parse(javaFile);
        final var symbols = new Symbols(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        findInClassMapAndAddNameInList(compilationUnit, ClassOrInterfaceDeclaration.class,
                symbols.classOrInterfaceSymbols());
        findInClassMapAndAddNameInList(compilationUnit, FieldDeclaration.class,
                (FieldDeclaration it) -> it.getVariables().get(0),
                symbols.fieldSymbols()
        );
        findInClassMapAndAddNameInList(compilationUnit, MethodDeclaration.class, symbols.methodSymbols());
        return symbols;
    }

    @Override
    public void prepare(final List<? extends Change> changes) {

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

    private void processModifyChange(final ModifyChange modifyChange) {
    }

    private void processAddChange(final AddChange change) {
    }

    @Override
    public String getValue(final String s, final Revision revision) {
        return null;
    }

    @Override
    public void checkout(final Revision revision) {

    }
}
