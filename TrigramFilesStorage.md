# Architecture of trigram storage

## Global structure

*The first concept. The previous file of the concept is [TrigramStructure.md](https://github.com/SuduIDE/persistent-ide-caches/blob/main/TrigramStructure.md).*

*The description of the actual concept is in [TrigramRevisionTree.md](https://github.com/SuduIDE/persistent-ide-caches/blob/main/TrigramRevisionTree.md).*

*This concept doesn't work because of impossibility of checkouting and branching of particular commit in independent trigram trees. It can't be found in which vertex the actual position is.*

The data structure for each trigram stores files in which it is used. It processes following type of operations: add file with trigram, remove file with trigram, checkout to the previous version and find all files with particular trigram.

The data structure consists of three main parts.

1. **Map from trigram to data structure with files** (second part).
1. **Persistent stack with files** for each particular trigram.
1. **Stack** which is used **for the checkout to the right version**.

## Operations

Operations can be one two types: **operations with trigram** and **checkout**.

For the operations of first type several operations are done. First, the particular data structure is gotten from map. Second, checkout to the actual version is done. And, finally, for the relevant stack the operation is executed.

For the checkout no operations with trigram data structure is done. However, the stack with checkout versions is updated as unnecessary checkouts are removed.

## Checkout logic

All actual checkouts stores in `Stack<time, version>`. By actual checkouts we consider increasing `version` with the increase of `time`. Indeed, if `time1 < time2` and `version1 > version2`, then user later checkout to the more previous version and first checkout is unnecessery.

Consider two types of operations.

* **Operations with trigram.** 

	Some checkouts can be done after the last change for this trigram. Therefore, we first checkout to the actual version. We know the time of last update in the data structure. Using binary search we find in stack the minimum value of time >= last time in data structure (last important checkout). Then checkout to that version (note, that there weren't any changes later and none of the later checkouts are important). The logic of this operation will be considered in the next part.
* **Checkout.** 

	For new checkout we should update the checkout stack to the actual values. For this purpose we simply pop the checkouts while its version is greater than the version of current checkout.

## Operations with trigrams

For each trigram we store the list with changes (add file, remove file) with the time of update and the link to the previous change. To get all the information about the trigram we find the last update corresponding to the curent version and then simply takes the previous one while it exists. Adding makes at the end of the list. However, we should always know the last actual change for the trigram. For this purpose the checkout logic should be implemented. First we find the actual version for the trigram as described above.

There are two types of logic with different complexity can be implemented. 

1. **Linear logic**. 

	All commits are consecutive and after checkout the removed part becomes forgotten forever. The commits stucture looks like a stack. 
2. **Branching tree logic**. 

	All commits are important and checkout can be done to the arbitrary previous state. The commit structure looks like a tree.

### Linear logic

The update history can be stored simply as a stack even without the link to the previous update. For each request we can simply remove the last update until its time is greater than the checkout time. After that the stack would describe the current structure.

### Branching tree logic

Here we store the list of events with full information. However, we still should find the last actual update. For this purpose at each branching we add fictious vertex with the time of branching (note, that branching appears only after checkout and the branching time is the checkout time). So, if between adding the checkout happened, two events would be added instead of one. Each event has a link to the previous event and all the information can be taken using this relations. For checkout, we find using binary search last important commit (with time less then the version of checkout) and it would be the actual one.