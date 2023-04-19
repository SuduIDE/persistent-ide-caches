package com.github.SuduIDE.persistentidecaches.git;

import static com.github.SuduIDE.persistentidecaches.IntegrationIndexesTest.FILES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.github.SuduIDE.persistentidecaches.GitParser;
import com.github.SuduIDE.persistentidecaches.IndexesManager;
import com.github.SuduIDE.persistentidecaches.PathCache;
import com.github.SuduIDE.persistentidecaches.changes.AddChange;
import com.github.SuduIDE.persistentidecaches.changes.Change;
import com.github.SuduIDE.persistentidecaches.changes.ModifyChange;
import com.github.SuduIDE.persistentidecaches.lmdb.maps.LmdbSha12Int;
import com.github.SuduIDE.persistentidecaches.utils.DummyRevisions;
import com.github.SuduIDE.persistentidecaches.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class GitParserTest {

    @TempDir
    public Path gitDir;

    public static IndexesManager mockedIndexManager() {
        final var indexesManager = mock(IndexesManager.class);
        doReturn(new DummyRevisions()).when(indexesManager).getRevisions();
        doReturn(mock(PathCache.class)).when(indexesManager).getFileCache();
        return indexesManager;
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testParseOneBranch() throws IOException, GitAPIException {
        try (final Git git = Git.init().setDirectory(gitDir.toFile()).call()) {
            for (final var c : FILES) {
                final Path file = gitDir.resolve(c);
                FileUtils.createParentDirectories(file.getParent());
                Files.writeString(file, c.toString());
                git.add()
                        .addFilepattern(c.toString())
                        .call();
                git.commit()
                        .setMessage(c.toString())
                        .call();
            }
            final var indexesManager = mockedIndexManager();
            final var db = Mockito.mock(LmdbSha12Int.class);
            doReturn(-1).when(db).get(any());

            final ArgumentCaptor<List<Change>> requestCaptor = ArgumentCaptor.forClass(List.class);

            new GitParser(git, indexesManager, db).parseHead();

            Mockito.verify(indexesManager, times(FILES.size()))
                    .applyChanges(requestCaptor.capture());

            Assertions.assertEquals(requestCaptor.getAllValues().size(), FILES.size());
            requestCaptor.getAllValues().forEach(it -> Assertions.assertEquals(it.size(), 1));
            Assertions.assertTrue(requestCaptor.getAllValues().stream().flatMap(Collection::stream)
                    .allMatch(it -> it instanceof AddChange));
            Assertions.assertEquals(requestCaptor.getAllValues().stream().mapToLong(Collection::size).sum(),
                    FILES.size());
            FILES.forEach(file -> {
                final var change = requestCaptor.getAllValues().stream().flatMap(Collection::stream)
                        .map(it -> (AddChange) it)
                        .filter(it -> it.getPlace().file().equals(file))
                        .toList();
                Assertions.assertEquals(change.size(), 1);
                Assertions.assertEquals(change.get(0).getAddedString(), file.toString());
            });
        }
        try (final Git git = Git.open(gitDir.toFile())) {
            for (final var c : FILES) {
                final Path file = gitDir.resolve(c);
                Files.writeString(file, c.toString().repeat(3));
                git.add()
                        .addFilepattern(c.toString())
                        .call();
                git.commit()
                        .setMessage(c.toString())
                        .call();
            }
            final var indexesManager = mockedIndexManager();
            final var db = Mockito.mock(LmdbSha12Int.class);
            doReturn(-1).when(db).get(any());

            final ArgumentCaptor<List<Change>> requestCaptor = ArgumentCaptor.forClass(List.class);

            new GitParser(git, indexesManager, db).parseHead();

            Mockito.verify(indexesManager, times(2 * FILES.size()))
                    .applyChanges(requestCaptor.capture());

            Assertions.assertEquals(requestCaptor.getAllValues().size(), 2 * FILES.size());
            requestCaptor.getAllValues().forEach(it -> Assertions.assertEquals(it.size(), 1));
            Assertions.assertEquals(requestCaptor.getAllValues().stream().flatMap(Collection::stream)
                    .filter(it -> it instanceof ModifyChange).count(), FILES.size());
            Assertions.assertEquals(requestCaptor.getAllValues().stream().mapToLong(Collection::size).sum(),
                    2L * FILES.size());
            FILES.forEach(file -> {
                final var changes = requestCaptor.getAllValues().stream().flatMap(Collection::stream)
                        .filter(it -> it instanceof ModifyChange)
                        .map(it -> (ModifyChange) it)
                        .filter(it -> it.getNewFileName().equals(file))
                        .toList();
                Assertions.assertEquals(changes.size(), 1);
                final var change = changes.get(0);
                Assertions.assertEquals(change.getOldFileContent(), file.toString());
                Assertions.assertEquals(change.getNewFileContent(), file.toString().repeat(3));
                Assertions.assertEquals(change.getOldFileName(), change.getNewFileName());
            });
        }
    }
}
