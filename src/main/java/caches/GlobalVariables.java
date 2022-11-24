package caches;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public class GlobalVariables {
    public static final Map<Integer, File> filesInProject = new TreeMap<>();
    public static final Map<File, Integer> reverseFilesInProject = new TreeMap<>();
}
