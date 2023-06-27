package com.github.SuduIDE.persistentidecaches.utils.indexes;

import com.github.SuduIDE.persistentidecaches.Index;
import com.github.SuduIDE.persistentidecaches.changes.Change;
import com.github.SuduIDE.persistentidecaches.records.Revision;
import java.util.List;

public class EchoIndex implements Index<String, String> {

    public static final String SEP = "-------------------";

    @Override
    public void prepare(final List<? extends Change> changes) {
        System.out.println("Echo: prepare");
        changes.forEach(System.out::println);
        System.out.println(SEP);
    }

    @Override
    public void processChanges(final List<? extends Change> changes) {
        System.out.println("Echo: process");
        changes.forEach(System.out::println);
        System.out.println(SEP);
    }

    @Override
    public String getValue(final String o, final Revision revision) {
        System.out.println("Echo: get " + o + " from revision: " + revision);
        return null;
    }

    @Override
    public void checkout(final Revision revision) {
        System.out.println("Echo: checkout to " + revision);
    }
}
