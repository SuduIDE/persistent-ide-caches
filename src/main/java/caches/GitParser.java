package caches;

import caches.changes.*;
import caches.records.FilePointer;
import caches.records.Revision;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.IndexDiffFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
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

import static caches.GlobalVariables.*;

public class GitParser {

    private static final Logger LOG = LoggerFactory.getLogger(GitParser.class);
    private final Git git;
    private final Repository repository;
    private final List<Index<?, ?>> indexes;
    private final int commitsLimit;
    public static final boolean PARSE_ONLY_TREE = false;

    public GitParser(Git git, List<Index<?, ?>> indices) {
        this.git = git;
        repository = git.getRepository();
        this.indexes = indices;
        this.commitsLimit = Integer.MAX_VALUE;
    }

    public GitParser(Git git, List<Index<?, ?>> indices, int commitsLimit) {
        this.git = git;
        repository = git.getRepository();
        this.indexes = indices;
        this.commitsLimit = commitsLimit;
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

    public void parseOne(ObjectId head) {
        LOG.info("Parsing ref: " + head.getName());
        try (RevWalk walk = new RevWalk(repository)) {
            Deque<RevCommit> commits = new ArrayDeque<>();
            RevCommit firstCommit = null;
            {
                walk.markStart(walk.parseCommit(head));
                for (var c : walk) {
                    if (!parsedCommits.containsKey(c.getName())) {
                        commits.addLast(c);
                        continue;
                    }
                    firstCommit = c;
                    break;
                }
            }

            LOG.info(String.format("%d commits finded to process", commits.size()));
            System.out.printf("%d finded to process%n", commits.size());

            if (firstCommit == null) {
                revisions.setCurrentRevision(Revision.NULL);
                indexes.forEach(i -> i.checkout(Revision.NULL));
                firstCommit = commits.removeLast();
                parseFirstCommit(firstCommit);
            } else {
                var re = parsedCommits.get(firstCommit.getName());
                revisions.setCurrentRevision(re);
                indexes.forEach(i -> i.checkout(re));
            }
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
            System.out.println("Processed " + totalCommits + " commits");
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public void parse() throws IOException {
        parseOne(repository.resolve(Constants.HEAD));
    }

    void sendChanges(List<Change> changes, Revision revision) {
        changes.forEach(it -> {
            switch (it) {
                case FileChange fileChange -> tryRegisterNewFile(fileChange.getPlace().file());
                case FileHolderChange fileHolderChange -> {
                    tryRegisterNewFile(fileHolderChange.getOldFileName());
                    tryRegisterNewFile(fileHolderChange.getNewFileName());
                }
            }
        });
        revisions.setCurrentRevision(revision);
        indexes.forEach(it -> it.prepare(changes));
        if (!PARSE_ONLY_TREE && revisions.changes.put(revision, changes) != null) {
            throw new RuntimeException("Found parent twice");
        }
    }

    void sendChanges(List<Change> changes) {
        sendChanges(changes, revisions.addRevision(revisions.getCurrentRevision()));
    }

    private void parseCommit(RevCommit commit, RevCommit prevCommit) throws IOException, GitAPIException {
        Revision rev = parsedCommits.computeIfAbsent(commit.getName(), c -> revisions.addRevision(revisions.getCurrentRevision()));

        if (PARSE_ONLY_TREE) {
            sendChanges(List.of(), rev);
            return;
        }

        try (var tw = new TreeWalk(repository)) {
            tw.addTree(prevCommit.getTree());
            tw.addTree(commit.getTree());
            tw.setFilter(IndexDiffFilter.ANY_DIFF);
            tw.setFilter(AndTreeFilter.create(IndexDiffFilter.ANY_DIFF, PathSuffixFilter.create(".java")));
            tw.setRecursive(true);
            var rawChanges = DiffEntry.scan(tw);
            var changes = rawChanges.stream()
                    .map(it -> {
                        try {
                            return processDiff(it);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
//            System.err.println(rev.revision() + " " + commit.getName() + " " + commit.getShortMessage() + " " + changes.size());
            sendChanges(changes, rev);
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
        if (!revisions.getCurrentRevision().equals(Revision.NULL)) {
            throw new RuntimeException("First commit should have null revision parent");
        }
        Revision rev = parsedCommits.computeIfAbsent(first.getName(), c -> revisions.addRevision(revisions.getCurrentRevision()));

        LOG.info("First commit: " + first.getShortMessage());
        if (PARSE_ONLY_TREE) {
            sendChanges(List.of(), rev);
            return;
        }

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
        sendChanges(changes, rev);
    }
}