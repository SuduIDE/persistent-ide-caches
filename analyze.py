from collections import Counter
from sys import argv

print(argv[1])
with open(argv[1], "rt") as f:
  l = f.readlines()
  n = int(l[0])

  def analyze_degrees():
    n += 1
    graph = [[] for _ in range(n)]
    for i in range(1, n):
      par, v = map(int, l[i].split())
      graph[par+1].append(v+1)
    print(sorted(Counter([len(x) for x in graph]).most_common()))

  def analyze_cost():
    li = 1
    k = int(l[li])
    li += 1
    sum_comp = 0
    cnt_comp = 0
    sum_big = 0
    sum_short = 0
    for v in range(n):
      for i in range(k):
        big, short = map(int, l[li].split())
        li += 1
        if i == k - 1 and big > 0 and short > 0:
          sum_comp += big / short
          sum_big += big
          sum_short += short
          cnt_comp += 1
    print("number of samples:", cnt_comp)
    print("average ratio:", sum_comp / cnt_comp)
    print("average compression:", sum_big / sum_short)

  analyze_cost()
