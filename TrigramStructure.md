# Trigram Structure

*The first concept. The next file of the concept is [TrigramFilesStorage.md](https://github.com/SuduIDE/persistent-ide-caches/blob/main/TrigramFilesStorage.md).*

*This concept doesn't work because of impossibility of checkouting and branching of particular commit in independent trigram trees. It can't be found in which vertex the actual position is.*

The data structure with linear memory that implements the interaction with trigrams such as adding, removing, swithcing version and search sufficiently fast.

## Data structure configuration

---

*The simple approach is one data structure that contains a pair of trigram and position. It may be implimented as B-tree or B-+tree. It would requires **O(log(number_of_trigrams))** time for each operation. It may be the most efficient implimentation, however, below is the structure that we discussed.*

---

The data structure consists of two layers: the data structure that stores **trigrams** and for each used trigram store all positions in files (**positions**) of this trigram in a LocalHistory. In fact, the first data structure is simply a map from trigram to its History.

Note, that trigrams are easily comparable. It would be very convenient if positions were also comparable.

### Trigram data structure

Trigram data structure is map from trigram to its history. It consumes **O(number_of_trigrams)** memory and allows to find/add trigram in **O(log(number_of_trigrams))** time.

As trigrams are the keys of fixed sizes, it is logical to choose B-tree.

### Data structure for particular trigram

For particular trigram the persistent data structure is stored. This data structure contains only commits that contains the particular trigram. The memory is **O(number_of_occurrence_of_the_trigram)** and the time for request is **O(log(number_of_occurence_of_the_trigram))**.

Again, it can be a B-tree. However, it may be useful to know the next element and use B+-tree. For example, is can be used to find next occurrence of the trigram or to find all the trigrams.

## Data structure methods

* **Initialization**
	
	Initialization requires only the link to the Trigram data structure and the current revision. Thus, the initialization requires **O(1)** memory and **O(1)** time.

* **Add trigram** 

	`void addTrigram(trigram, position, revision)`
	
	Find the trigram in the map and add it if there wasn't the trigram. Add the position of the trigram to the particular revision of the persistent tree. As a default, the revision is current version of the text.
	
	Memory **O(1)**, time **O(log(number_of_trigrams) + log(number_of_occurrence_of_the_trigram))**.
	
* **Remove trigram**

	`boolean removeTrigram(trigram, position, revision)`
	
	Find the trigram in the map. Remove the position of the trigram from the particular revision of the persistent tree (may returns the existence of the position of the trigram). As a default, the revision is current version of the text.
	
	Memory **O(1)**, time **O(log(number_of_trigrams) + log(number_of_occurrence_of_the_trigram))**.
	
* **Find next trigram according to the position**

	`Position nextOccurrence(trigram, position, revision)`

	Find the trigram in the map. Find the first higher position of the trigram from the particular revision of the persistent tree. As a default, the revision is current version of the text.

	Memory **O(1)**, time **O(log(number_of_trigrams) + log(number_of_occurrence_of_the_trigram))**.

* **Find all positions of trigram**

	`List<Position> allOccurrence(trigram, position, revision)`
	
	Find the trigram in the map. Returns all positions stored in revision version of the data structure. As a default, the revision is current version of the text.
	
	Memory **O(number_of_occurrence_of_the_trigram)**, time **O(log(number_of_trigrams) + number_of_occurrence_of_the_trigram)**.

## Text methods

* **Add symbol**

	`addSymbol(char, position)`
	
	Runs several requests to the data structure. If symbol is inserted between other symbols, removes several trigrams. Then, add new trigrams.
	
	**O(1)** requests to the data structure.
	
* **Remove symbol**

	`removeSymbol(char, position)`
	
	Runs several requests to the data structure. Removes several trigrams that contain the symbol. Then, add new trigrams that contain the neighboring symbols.
		
	**O(1)** requests to the data structure.
	
* **Add string**

	Runs several requests to the data structure. If string is inserted between other symbols, removes several trigrams. Then, add all new trigrams.
	
	**O(len(string))** requests to the data structure.