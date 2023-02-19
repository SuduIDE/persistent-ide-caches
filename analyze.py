from collections import Counter

with open("tree.txt", "rt") as f:
  l = f.readlines()
  n = int(l[0])+1
  graph = [[] for _ in range(n)]
  for i in range(1, n):
    par, v = map(int, l[i].split())
    graph[par+1].append(v+1)
  print(Counter([len(x) for x in graph]))