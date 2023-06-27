package com.github.SuduIDE.persistentidecaches.symbols;

public record Symbol(String name, int pathNum) implements Comparable<Symbol> {

    public static final Symbol MIN = new Symbol(null, Integer.MIN_VALUE);
    public static final Symbol MAX = new Symbol(null, Integer.MAX_VALUE);

    @Override
    public int compareTo(final Symbol o) {
        final int res = Integer.compare(pathNum, o.pathNum);
        return res == 0 ? name.compareTo(o.name) : res;
    }
}
