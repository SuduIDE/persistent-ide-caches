# Trigram revision tree with computable trigram index

*The actual concept. The exproations about path compression are in [tree-analysis.md](https://github.com/SuduIDE/persistent-ide-caches/blob/main/tree-analysis.md). Th algorithm for checkouts and path compression are in [PathCompression.md](https://github.com/SuduIDE/persistent-ide-caches/blob/main/PathCompression.md).*

This document describes the basic idea of a trigram index data structure. The data structure consists of a single revision tree, in nodes of which the differences between the two versions are stored.

Such approach leads to an efficient time and memory complexity, and also makes it possible to introduce multiple optimizations to the solution.

## Motivation

The previous approach (described in [TrigramFilesStorage.md](https://github.com/SuduIDE/persistent-ide-caches/blob/main/TrigramFilesStorage.md)) featured an idea of storing a global *stack* of checkouts. To answer a query of finding the last relevant commit it was suggested to apply binary search, making use of the *stack* being sorted by time.

The solution works fine in case of a linear commit history, but in case of *branching tree logic*, as described in the linked document, the problems occur. Namely, whenever you checkout to some previous version, you are not allowed to go back, because the initial checkout gets popped from the *stack* as it was thought to be unnecessary.

## Data Structure: what is stored

The *global* revision tree is stored, where for every vertex (revision) the pointer to its parent is saved. Moreover, in every vertex we store the difference between this commit and its parent, so-called *delta*. Every *delta* stores information required to maintain trigram index. For example, we could store a list of changes like some trigram gets added or removed from some file.

## Data Structure: how to answer queries

We need to support queries of checking out to some version, commiting new changes and getting trigram information in some commit.

At every point of time we store trigram index of a current (active) revision. This includes storing a data structure, which for every trigram stores some information, which is modified by *deltas* introduced before.

### Get queries

So, in order to answer a query about some commit we first checkout to it. After that we just have to make a query to trigram index. Therefore, such query is answered in *O(answer size)*.

### Commit queries

Commit queries are also trivial: we create new vertex, assign its parent to current revision, store delta between two versions (commit description), and change the active revision to the created version. Therefore, such query is answered in *O(delta size)*.

### Checkout queries

In order to checkout to some version, we should recalculate the state of a trigram index data structure, and change the active revision to the one we checked out to.

Recalculating works as follows: let's denote the current version as `v1` and target version as `v2`. Let `LCA` be the lowest common ancestor of `v1` and `v2`. We need to rollback the *deltas* from `v1` to `LCA`, and add *deltas* from `LCA` to `v2`.

Recalculating deltas on some path up is done by simply iterating on parents, until we reach the target node. Therefore, such query is answered in *O(len)*, where *len* is denoted as a length of a unique simple path between `v1` and `v2` in tree. Finding `LCA` can be done in the same time with recalculating, basically we first go up from the lowest vertex, and then we go up from both one at a time, until we coincide in one vertex.

## Discussion

The described data structure allows for maintaining a versionable trigram index, the main idea being that we store the revision tree, and all information about the most recent version, and for checkout we recalculate the active version.

The only non-optimal time complexity in the data structure is checkout, being *O(len)*. It is assumed that on average real-world checkouts happen between close versions, and therefore this path should be small.