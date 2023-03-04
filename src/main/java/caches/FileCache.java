package caches;

import caches.lmdb.LmdbInt2File;
import caches.lmdb.LmdbString2Int;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FileCache {
    private final String FILES = "files";
    private final LmdbInt2File filesInProject;
    private final Map<File, Integer> reverseFilesInProject = new HashMap<>();
    private final LmdbString2Int variables;

    public FileCache(LmdbInt2File filesInProject, LmdbString2Int variables) {
        this.filesInProject = filesInProject;
        this.variables = variables;
    }

    public int getNumber(File  file) {
        return reverseFilesInProject.get(file);
    }

    public File getFile(int fileNum) {
        return filesInProject.get(fileNum);
    }

    public void tryRegisterNewFile(File file) {
        if (reverseFilesInProject.get(file) == null) {
            var fileNum = variables.get(FILES);
            filesInProject.put(fileNum, file);
            reverseFilesInProject.put(file, fileNum);
            variables.put(FILES, fileNum + 1);
        }
    }

    public void restoreFilesFromDB() {
        filesInProject.forEach((integer, file) -> reverseFilesInProject.put(file, integer));
    }

    public void initFiles() {
        if (variables.get(FILES) == -1) {
            variables.put(FILES, 0);
        }
    }
}
