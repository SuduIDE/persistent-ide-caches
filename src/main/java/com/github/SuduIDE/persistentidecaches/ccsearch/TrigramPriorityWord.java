package com.github.SuduIDE.persistentidecaches.ccsearch;

import com.github.SuduIDE.persistentidecaches.records.Trigram;

public record TrigramPriorityWord(Trigram trigram, int priority, String word) {

}
