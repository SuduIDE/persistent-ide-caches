package caches;

import caches.lmdb.LmdbInt2File;
import caches.lmdb.LmdbSha12Int;
import caches.lmdb.LmdbString2Int;
import org.lmdbjava.Env;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class GlobalVariables {

    public static final String FILES = "files";
    public static final String LMDB_DIRECTORY = ".lmdb";
    public static final Map<File, Integer> reverseFilesInProject = new HashMap<>();
    public static Revisions revisions;
    public static Env<ByteBuffer> env;
    public static LmdbInt2File filesInProject;
    public static LmdbSha12Int gitCommits2Revisions;
    public static LmdbString2Int variables;


    public static void tryRegisterNewFile(File file) {
        if (reverseFilesInProject.get(file) == null) {
            var fileNum = variables.get(FILES);
            filesInProject.put(fileNum, file);
            reverseFilesInProject.put(file, fileNum);
            variables.put(FILES, fileNum + 1);
        }
    }

    public static void restoreFilesFromDB() {
        filesInProject.forEach((integer, file) -> reverseFilesInProject.put(file, integer));
    }

    public static void initFiles() {
        if (variables.get(FILES) == -1) {
            variables.put(FILES, 0);
        }
    }
}
