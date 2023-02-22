# Analysis of github repository tree structure

In order to determine how the future algorithm will perform, it was decided to perform an analysis of some open-source repositories. Those included [xodus](https://github.com/JetBrains/xodus/), [commons-lang](https://github.com/apache/commons-lang), [soot](https://github.com/soot-oss/soot), [intellij-community](https://github.com/JetBrains/intellij-community).

## Analysis of tree structure

For every beforementioned repository we constructed a tree of all the commits, including all branches. To analyze this tree we calculated the degree distribution of those trees. The results are as follows (degree means the number of children in a tree):

### Xodus

Degree 0: 12

Degree 1: 2924

Degree 2: 5

Degree 3: 3

### Commons-lang

Degree 0: 80

Degree 1: 7437

Degree 2: 77

Degree 3: 1

### Soot

Degree 0: 42

Degree 1: 7308

Degree 2: 37

Degree 3: 2

### IntelliJ-Community

Degree 0: 1571

Degree 1: 477143

Degree 2: 1346

Degree 3: 80

Degree 4: 8

Degree 5: 4

Degree 6: 3

Degree 10: 1

## Conclusions

From here we can make a conclusion that most of the vertices have degree equal to one, so our tree looks like a bunch of chains. The developed algorithm should account for this fact.
