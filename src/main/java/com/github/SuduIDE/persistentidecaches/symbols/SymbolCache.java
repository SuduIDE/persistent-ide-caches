package com.github.SuduIDE.persistentidecaches.symbols;


import com.github.SuduIDE.persistentidecaches.lmdb.LmdbIntCountingCache;
import com.github.SuduIDE.persistentidecaches.lmdb.maps.LmdbInt2Obj;
import com.github.SuduIDE.persistentidecaches.lmdb.maps.LmdbString2Int;

public class SymbolCache extends LmdbIntCountingCache<Symbol> {

    public SymbolCache(final LmdbInt2Obj<Symbol> symbolLmdbInt2Obj,
            final LmdbString2Int variables) {
        super("symbols", symbolLmdbInt2Obj, variables);
    }
}

