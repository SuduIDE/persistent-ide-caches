package caches.utils;

import caches.Index;
import caches.changes.Change;
import caches.records.Revision;

import java.util.List;

public class EchoIndex implements Index<String, String> {

    public static final String SEP = "-------------------";

    @Override
    public void prepare(List<? extends Change> changes) {
        System.out.println("Echo: prepare");
        changes.forEach(System.out::println);
        System.out.println(SEP);
    }

    @Override
    public void processChanges(List<? extends Change> changes) {
        System.out.println("Echo: process");
        changes.forEach(System.out::println);
        System.out.println(SEP);
    }

    @Override
    public String getValue(String o, Revision revision) {
        System.out.println("Echo: get " + o + " from revision: " + revision);
        return null;
    }

    @Override
    public void checkout(Revision revision) {
        System.out.println("Echo: checkout to " + revision);
    }
}
