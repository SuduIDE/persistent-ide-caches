# CamelCase search optimization and priorities

The first most naive approach of CamelCase search was described in [CamelCaseSearch.md](https://github.com/SuduIDE/persistent-ide-caches/blob/main/CamelCaseSearch.md). Also the patterns and the corresponding words were described.

This file describes the optimized algorithm and the priorities of the trigrams.

## Priorities

The most obvious approach is to range words according to the number of times it exists in the text. The time of last occurrence also matters as Also the type of the key word such as `File name`, `Class name` etc. can be taken into account. All this information can be combined in one integer number, `priority`.

## Algorithm

The naive algorithm finds all key words, which appears for each trigram and intersect the lists. We should notice, that we still should check the correctness of the found words as trigrams can appear in different order and places as in the pattern. 

Our **goal** is to find the several (let say, `K`) best words, which correspond to given pattern.

First, we can find only the list of the words with the **least popular trigram** and check all of them. It is solvable by the storing of the map from trigram to the list of words.

But we can further optimize it. As we already mention, we only need to get **the *best* words**. Therefore, we can store set of the objects `<trigram, priority, word>`. Also for each trigram we store the number of words with this trigram. All this datastructures can be processed only similar to [Trigram Revision Tree](https://github.com/SuduIDE/persistent-ide-caches/blob/main/TrigramRevisionTree.md).

### Online search

For the particular given pattern we first find the least popular trigram. Then, for this trigram we check all the words starting from the *best* and check if they correspond to the given pattern while we wouldn't find `K` best. We also can find more words continuing from the last found one.

Moreover, if the pattern increase and some of the found words don't coincide, we still can continue from the last found one or find less popular trigram and words in its list also starting from the last found one. The choise depends on the number of the *worse* words in each particular case.