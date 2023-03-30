package com.github.SuduIDE.persistentidecaches;

import com.github.SuduIDE.persistentidecaches.changes.AddChange;
import com.github.SuduIDE.persistentidecaches.changes.Change;
import com.github.SuduIDE.persistentidecaches.changes.CopyChange;
import com.github.SuduIDE.persistentidecaches.changes.DeleteChange;
import com.github.SuduIDE.persistentidecaches.changes.FileChange;
import com.github.SuduIDE.persistentidecaches.changes.FileHolderChange;
import com.github.SuduIDE.persistentidecaches.changes.ModifyChange;
import com.github.SuduIDE.persistentidecaches.changes.RenameChange;
import com.github.SuduIDE.persistentidecaches.lmdb.LmdbSha12Int;
import com.github.SuduIDE.persistentidecaches.records.FilePointer;
import com.github.SuduIDE.persistentidecaches.records.Revision;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.IndexDiffFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GitParser {

    public static final boolean PARSE_ONLY_TREE = false;
    private static final Logger LOG = LoggerFactory.getLogger(GitParser.class);
    private final Repository repository;
    private final IndexesManager indexesManager;
    private final int commitsLimit;
    private final LmdbSha12Int gitCommits2Revisions;

    public GitParser(Git git, IndexesManager indexesManager, LmdbSha12Int gitCommits2Revisions) {
        this(git, indexesManager, gitCommits2Revisions, Integer.MAX_VALUE);
    }

    public GitParser(Git git, IndexesManager indexesManager, LmdbSha12Int gitCommits2Revisions, int commitsLimit) {
        repository = git.getRepository();
        this.indexesManager = indexesManager;
        this.commitsLimit = commitsLimit;
        this.gitCommits2Revisions = gitCommits2Revisions;
    }


    public void parseAll() throws IOException {
        var refs = repository.getRefDatabase().getRefs();
        System.err.println("Parsing " + refs.size() + " refs");
        int cnt = 0;
        for (Ref ref : refs) {
            parseOne(ref.getObjectId());
            System.err.println("Parsed " + (++cnt) + "/" + refs.size() + " refs");
        }
    }

    public void parseHead() throws IOException {
        parseOne(repository.resolve(Constants.HEAD));
    }

    private void parseOne(ObjectId head) {
        LOG.info("Parsing ref: " + head.getName());
        try (RevWalk walk = new RevWalk(repository)) {
            Deque<RevCommit> commits = new ArrayDeque<>();
            RevCommit firstCommit = null;
            {
                walk.markStart(walk.parseCommit(head));
                for (var commit : walk) {
                    commits.add(commit);
                    if (gitCommits2Revisions.get(commit.getName()) != -1) {
                        firstCommit = commit;
                        break;
                    }
                }
            }
            if (!commits.iterator().hasNext()) {
                throw new RuntimeException("Repository hasn't commits");
            }
            LOG.info(String.format("%d commits found to process", commits.size()));

            if (firstCommit == null) {
                indexesManager.getRevisions().setCurrentRevision(Revision.NULL);
                indexesManager.checkout(Revision.NULL);
                firstCommit = commits.removeLast();
                parseFirstCommit(firstCommit);
            } else {
                var rev = new Revision(gitCommits2Revisions.get(firstCommit.getName()));
                indexesManager.getRevisions().setCurrentRevision(rev);
                indexesManager.checkout(rev);
            }
            var prevCommit = firstCommit;

            int commitsParsed = 0;
            int totalCommits = Math.min(commitsLimit, commits.size());
            while (commitsParsed < totalCommits) {
                if (commitsParsed % 100 == 0) {
                    System.err.printf("Processed %d commits out of %d %n", commitsParsed, totalCommits);
                }
                var commit = commits.removeLast();
                parseCommit(commit, prevCommit);
                prevCommit = commit;
                commitsParsed++;
            }
            System.err.println("Processed " + totalCommits + " commits");
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    void sendChanges(List<Change> changes, RevCommit commit) {
        changes.forEach(it -> {
            switch (it) {
                case FileChange fileChange -> indexesManager.getFileCache().tryRegisterNewFile(fileChange.getPlace().file());
                case FileHolderChange fileHolderChange -> {
                    indexesManager.getFileCache().tryRegisterNewFile(fileHolderChange.getOldFileName());
                    indexesManager.getFileCache().tryRegisterNewFile(fileHolderChange.getNewFileName());
                }
            }
        });
        int rev = gitCommits2Revisions.get(commit.getName());
        if (rev == -1) {
            indexesManager.getRevisions().setCurrentRevision(
                    indexesManager.getRevisions().addRevision(
                            indexesManager.getRevisions().getCurrentRevision()));
            gitCommits2Revisions.put(commit.getName(), indexesManager.getRevisions().getCurrentRevision().revision());
        } else {
            indexesManager.getRevisions().setCurrentRevision(new Revision(rev));
        }
        indexesManager.applyChanges(changes);
    }

    private void parseCommit(RevCommit commit, RevCommit prevCommit) throws IOException, GitAPIException {
        try (var tw = new TreeWalk(repository)) {
            tw.addTree(prevCommit.getTree());
            tw.addTree(commit.getTree());
            tw.setFilter(IndexDiffFilter.ANY_DIFF);
            tw.setFilter(AndTreeFilter.create(IndexDiffFilter.ANY_DIFF, PathSuffixFilter.create(".java")));
            tw.setRecursive(true);
            var rawChanges = DiffEntry.scan(tw);
            sendChanges(rawChanges.stream()
                            .map(it -> {
                                try {
                                    return processDiff(it);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .flatMap(List::stream)
                            .collect(Collectors.toList()),
                    commit
            );
        }
    }

    Supplier<String> fileGetter(AbbreviatedObjectId abbreviatedObjectId) {
        return () -> {
            try {
                return new String(repository.open(abbreviatedObjectId.toObjectId()).getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    List<Change> processDiff(DiffEntry diffEntry) throws IOException {
        return switch (diffEntry.getChangeType()) {
            case ADD -> List.of(new AddChange(System.currentTimeMillis(),
                    new FilePointer(Path.of(diffEntry.getNewPath()), 0),
                    new String(repository.open(diffEntry.getNewId().toObjectId()).getBytes()))
            );
            case MODIFY -> List.of(
                    new ModifyChange(System.currentTimeMillis(),
                            fileGetter(diffEntry.getOldId()),
                            fileGetter(diffEntry.getNewId()),
                            Path.of(diffEntry.getOldPath()),
                            Path.of(diffEntry.getNewPath())
                    ));
            case DELETE -> List.of(
                    new DeleteChange(System.currentTimeMillis(), new FilePointer(Path.of(diffEntry.getOldPath()), 0),
                            new String(repository.open(diffEntry.getOldId().toObjectId()).getBytes())));
            case RENAME -> List.of(
                    new RenameChange(System.currentTimeMillis(),
                            fileGetter(diffEntry.getOldId()),
                            fileGetter(diffEntry.getNewId()),
                            Path.of(diffEntry.getOldPath()),
                            Path.of(diffEntry.getNewPath())
                    ));
            case COPY -> List.of(
                    new CopyChange(System.currentTimeMillis(),
                            fileGetter(diffEntry.getOldId()),
                            fileGetter(diffEntry.getNewId()),
                            Path.of(diffEntry.getOldPath()),
                            Path.of(diffEntry.getNewPath())
                    ));
        };
    }

    private void parseFirstCommit(RevCommit first) {
        int rev = gitCommits2Revisions.get(first.getName());
        if (rev != -1) {
            indexesManager.getRevisions().setCurrentRevision(new Revision(rev));
            return;
        }
        List<Change> changes = new ArrayList<>();
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(first.getTree());
            treeWalk.setRecursive(true);
            while (treeWalk.next()) {
                changes.add(new AddChange(System.currentTimeMillis(),
                        new FilePointer(Path.of(treeWalk.getPathString()), 0),
                        new String(repository.open(treeWalk.getObjectId(0)).getBytes()))
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sendChanges(changes, first);
    }
}