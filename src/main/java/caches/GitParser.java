package caches;

import caches.changes.*;
import caches.records.FilePointer;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static caches.GlobalVariables.revisions;
import static caches.GlobalVariables.tryRegisterNewFile;

public class GitParser {

    private static final Logger LOG = LoggerFactory.getLogger(GitParser.class);
    private final Git git;
    private final Repository repository;
    private final List<ChangeProcessor> indexes;
    private final int commitsLimit;

    public GitParser(Git git, List<ChangeProcessor> indices) {
        this.git = git;
        repository = git.getRepository();
        this.indexes = indices;
        this.commitsLimit = Integer.MAX_VALUE;
    }

    public GitParser(Git git, List<ChangeProcessor> indices, int commitsLimit) {
        this.git = git;
        repository = git.getRepository();
        this.indexes = indices;
        this.commitsLimit = commitsLimit;
    }

    public void parse() {
        try (RevWalk walk = new RevWalk(repository)) {
            Deque<RevCommit> commits = new ArrayDeque<>();
            {
                ObjectId head = repository.resolve(Constants.HEAD);
                walk.markStart(walk.parseCommit(head));
                walk.forEach(commits::add);
            }
            if (!commits.iterator().hasNext()) {
                throw new RuntimeException("Repository hasn't commits");
            }
            LOG.info(String.format("%d commits finded to process", commits.size()));
            System.out.printf("%d finded to process%n", commits.size());
            var firstCommit = commits.removeLast();
            parseFirstCommit(firstCommit);
            var prevCommit = firstCommit;
            int commitsParsed = 0;
            int totalCommits = Math.min(commitsLimit, commits.size());
            while (commitsParsed < totalCommits) {
                if (commitsParsed % 100 == 0) {
                    System.out.printf("Processed %d commits out of %d %n", commitsParsed, totalCommits);
                }
                var commit = commits.removeLast();
                parseCommit(commit, prevCommit);
                prevCommit = commit;
                commitsParsed++;
            }
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    void sendChanges(List<Change> changes) {
        changes.forEach(it -> {
            switch (it) {
                case FileChange fileChange -> tryRegisterNewFile(fileChange.getPlace().file());
                case FileHolderChange fileHolderChange -> {
                    tryRegisterNewFile(fileHolderChange.getOldFileName());
                    tryRegisterNewFile(fileHolderChange.getNewFileName());
                }
            }
        });
        revisions.setCurrentRevision(revisions.addRevision(revisions.getCurrentRevision()));
        indexes.forEach(it -> it.prepare(changes));
    }

    private void parseCommit(RevCommit commit, RevCommit prevCommit) throws IOException, GitAPIException {
        try (var tw = new TreeWalk(repository)) {
            tw.addTree(prevCommit.getTree());
            tw.addTree(commit.getTree());
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
                    .collect(Collectors.toList())
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
                    new FilePointer(new File(diffEntry.getNewPath()), 0),
                    new String(repository.open(diffEntry.getNewId().toObjectId()).getBytes()))
            );
            case MODIFY -> List.of(
                    new ModifyChange(System.currentTimeMillis(),
                            fileGetter(diffEntry.getOldId()),
                            fileGetter(diffEntry.getNewId()),
                            new File(diffEntry.getOldPath()),
                            new File(diffEntry.getNewPath())
                    ));
            case DELETE -> List.of(
                    new DeleteChange(System.currentTimeMillis(), new FilePointer(new File(diffEntry.getOldPath()), 0),
                            new String(repository.open(diffEntry.getOldId().toObjectId()).getBytes())));
            case RENAME -> List.of(
                    new RenameChange(System.currentTimeMillis(),
                            fileGetter(diffEntry.getOldId()),
                            fileGetter(diffEntry.getNewId()),
                            new File(diffEntry.getOldPath()),
                            new File(diffEntry.getNewPath())
                    ));
            case COPY -> List.of(
                    new CopyChange(System.currentTimeMillis(),
                            fileGetter(diffEntry.getOldId()),
                            fileGetter(diffEntry.getNewId()),
                            new File(diffEntry.getOldPath()),
                            new File(diffEntry.getNewPath())
                    ));
        };
    }

    private void parseFirstCommit(RevCommit first) {
        List<Change> changes = new ArrayList<>();
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(first.getTree());
            treeWalk.setRecursive(true);
            while (treeWalk.next()) {
                changes.add(new AddChange(System.currentTimeMillis(),
                        new FilePointer(new File(treeWalk.getPathString()), 0),
                        new String(repository.open(treeWalk.getObjectId(0)).getBytes()))
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sendChanges(changes);
    }
}