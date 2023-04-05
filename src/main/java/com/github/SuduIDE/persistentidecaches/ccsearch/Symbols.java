package com.github.SuduIDE.persistentidecaches.ccsearch;

import java.util.List;

public record Symbols(List<String> classOrInterfaceSymbols, List<String> fieldSymbols, List<String> methodSymbols) {

}
