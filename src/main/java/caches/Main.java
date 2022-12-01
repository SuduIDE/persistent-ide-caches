package caches;


import caches.changes.Change;
import caches.records.Revision;
import caches.trigram.TrigramHistoryIndex;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, GitAPIException {
        if (args.length < 1) {
            throw new RuntimeException("Needs path to repository as first arg");
        }
        new File(TrigramHistoryIndex.DIRECTORY).mkdir();
        Index<String, String> echoIndex = new Index<>() {
            @Override
            public String getValue(String s, Revision revision) {
                return null;
            }

            @Override
            public Revision getCurrentRevision() {
                return null;
            }

            @Override
            public List<Revision> getAllRevisions() {
                return null;
            }

            @Override
            public void checkout(Revision revision) {
            }

            @Override
            public void prepare(List<Change> changes) {
                changes.forEach(System.out::println);
            }

            @Override
            public void processChange(Change change) {

            }
        };
        try (Git git = Git.open(new File(args[0]))) {
            var parser = new GitParser(git, List.of(/*echoIndex,*/ new TrigramHistoryIndex()));
            parser.parse();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }
}
