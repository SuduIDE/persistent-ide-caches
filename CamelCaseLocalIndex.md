# Global index + local index

*Third file about CamelCaseSearch. The previous file is [CamelCasePriorities.md](https://github.com/SuduIDE/persistent-ide-caches/blob/main/CamelCasePriorities.md).*

In the previous files ([CamelCaseSearch.md](https://github.com/SuduIDE/persistent-ide-caches/blob/main/CamelCaseSearch.md), [CamelCasePriorities.md](https://github.com/SuduIDE/persistent-ide-caches/blob/main/CamelCasePriorities.md)) the general approach for trigram search was discussed. However, it gives all words, that fulfills the pattern. But we want to constract the local index, which describes how good the word maps to the pattern.

## Beginning word symbol

First of all, let's fix the issue of searching the key words with only 2 uppercase letters. For this purpose, we can add fictitious symbol at the beginning of each key word and pattern. 

*For example, if we call this symbol as `$`, the key word `CamelCase` becomes `$CamelCase`, while pattern `CC` becomes `$CC`, which is now a trigram.*

## Local index

In previous approach only global index was discussed, that is based on global parameters, such as the number of occurrence in files and the usage time.

*However, one can notice that for the pattern `$cca` word `$CamelCaseAlgorithm` matches better, then the word `$CamelCaseSearch` as it match all capital letters.* 

To deal with it we introduce the local index. The most obvious one is the number of the capital letters, which maps the pattern. 

*If we consider `$` as capital letter, the pattern `$CamelCaseAlgorithm` matches 4 capital letter, while `$CamelCaseSearch` only 3, which gives the better collision for the first key word.*

Now we should consider the information while storing the data. There is also an obvious approach for it. Remind, that for each trigram we store the list of words with priorities. Now, we modify the priority. As we want to match as much capital letters as possible, the main priority for each trigram would be the number of capital letters in trigram. It helps us to get the trigrams with potentially bigger local index from the map. 

Therefore, in general, the algorithms works as in previous version, but with local index.