from collections import Counter
from sys import argv
import matplotlib.pyplot as plt
from math import *

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

  def analyze_p2():
    li = 1
    ITERS = int(l[li])
    li += 1
    lens = []
    comps = []
    while li < len(l):
      p2 = int(l[li])
      print(p2)
      li += 1
      if li + ITERS > len(l):
        break
      sum_big = 0
      sum_short = 0
      for _ in range(ITERS):
        big, short = map(int, l[li].split())
        sum_big += big
        sum_short += short
        li += 1
      comp = sum_big / sum_short
      print("number of samples:", ITERS)
      print("average compression:", comp)
      lens.append(log2(p2))
      comps.append(log2(sum_big / sum_short))
    print(lens)
    print(comps)
    plt.xticks(list(range(0, 9)))
    plt.xlabel('log2(Path length)')
    plt.ylabel('log2(Average compression)')
    plt.title(f'Average compression over {ITERS} random vertices, xodus (n={n})')
    plt.scatter(lens, comps)
    plt.savefig(f'plot-xodus-2.pdf', format='pdf')
    plt.show()

  def analyze_p2_hld():
    li = 1
    ITERS = int(l[li])
    li += 1
    lens = []
    comps = []
    while li < len(l):
      max_travels = 0
      p2 = int(l[li])
      print(p2)
      li += 1
      if li + ITERS > len(l):
        break
      sum_big = 0
      sum_short = 0
      sum_hld = 0
      for _ in range(ITERS):
        big, short, hld, travels = map(int, l[li].split())
        sum_big += big
        sum_short += short
        sum_hld += hld
        li += 1
        max_travels = max(max_travels, travels)
      comp = sum_big / sum_hld
      print("number of samples:", ITERS)
      print("average compression:", comp)
      print("max travels:", max_travels)
      lens.append(log2(p2))
      comps.append(log2(sum_big / sum_hld))
    print(lens)
    print(comps)
    plt.xticks(list(range(0, 9)))
    plt.xlabel('log2(Path length)')
    plt.ylabel('log2(Average compression)')
    plt.title(f'Average compression over {ITERS} random vertices, xodus (n={n})')
    plt.scatter(lens, comps)
    plt.savefig(f'plot-xodus-hld.pdf', format='pdf')
    plt.show()

  def analyze_hld_memory():
    total_big = 0
    total_memory = 0
    for li in range(1, n+1):
      length, big, short, memory = map(int, l[li].split())
      total_big += big
      total_memory += memory
    print(total_big, total_memory, total_memory / total_big)
    
  analyze_p2_hld()
  # analyze_hld_memory()
