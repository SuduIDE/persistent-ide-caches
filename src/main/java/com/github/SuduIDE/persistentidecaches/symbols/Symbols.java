package com.github.SuduIDE.persistentidecaches.symbols;

import java.util.List;
import java.util.stream.Stream;

public record Symbols(List<String> classOrInterfaceSymbols, List<String> fieldSymbols, List<String> methodSymbols) {

    public Stream<String> concatedStream() {
        return Stream.concat(Stream.concat(classOrInterfaceSymbols.stream(), fieldSymbols.stream()),
                methodSymbols.stream());
    }
}
