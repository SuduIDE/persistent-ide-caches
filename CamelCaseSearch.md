# CamelCase search

This document describes the algorithm of CamelCase search using trigram index. 

It allows a fast search algoithm with linear memory, while the priority of the results is discussive.

## CamelCase patterns

The assumption is that the camel case search use the combination of the preffixes of the first words.

*For example, for the phrase `CamelCaseSearch`*

*`CCS`, `Camel`, `CamC` and `CamCSearch` are **the patterns**, while `Case`, `CamelSearch`, `CS` and `CelCase` are **not the patterns** we are looking for*.

The case sensitivity is discussive here.

One can notice that after each letter in the pattern goes only the next letter of the current word or the first letter of the next word. Therefore, there are not greater than 4 trigrams, starting with each letter of the word.

*For example, for the phrase `CamelCaseSearch` the trigrams, starting from `m` are*

`mel`, `meC`, `mCa` and `mCC`.

## Data storage

For each word in text we can store all trigrams described above. Therefore, the number of all trigrams is `4*TextLength` in any datastructure, which stores the trigrams.

## Pattern search

For the given pattern we simply search of the subtrigrmas of the pattern and intersect the results. It should work faster, than simple whole text search. As an optimisation we can first look for the trigrams with less results.