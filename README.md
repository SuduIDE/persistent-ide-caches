# Persistent immutable indexes for next generation IDE

## Overview

Integrated Development Environment(**IDE**) is a great tool that increases software developer perfromance, providing **features** in addition to plain text editor. 
In order to provide fast response time for many features, IDE requires **indexes**. For example, for full text search through all files in opened project, one may want 
to use trigram index that is merely a map: `char,char,char -> file`. In state of the art IDEs (like IntellijIDEA) indexes are maintaned for current working copy only, i.e.
for current files on file system. The idea of this work is to create efficient data structure that can hold indexes for whole git repository (each commit) + working copy.
