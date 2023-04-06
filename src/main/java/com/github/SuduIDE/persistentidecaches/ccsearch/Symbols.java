package com.github.SuduIDE.persistentidecaches.ccsearch;

import java.util.Set;

public record Symbols(Set<String> classOrInterfaceSymbols, Set<String> fieldSymbols, Set<String> methodSymbols) {

}
