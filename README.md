# Experimental repository for DCFinder algorithm with new metrics implementation
---

## Introduction

This is a getting-started package for Metanome algorithm developer. It contains two skeleton projects "DCFinder" and 
"MetanomeTestRunner". DCFinder is the actual profiling algorithm. The jar-file that you can build from this algorithm 
can be imported into a running instance of the Metanome tool. Because the graphical import is impractical during development, 
we use the second project, which is the MetanomeTestRunner, to run the algorithm with a set of predefined parameters during 
development. The MetanomeTestRunner mocks the Metanome backend and algorithmically performs the configuration, which is 
usually performed by the user. The essential difference between the algorithm and test runner project is that the algorithm 
only depends on the Metanome interface whereas the test runner depends on the entire Metanome backend.
Original developer introduction can be viewed from (Metanome Algorithm Developer Guider)[https://github.com/HPI-Information-Systems/Metanome/wiki/Metanome-Algorithm-Developer-Guide].

## Requirements
- Java JDK 1.7 (or higher)
- Apache Maven

## Add testing data
1. Import data file into `/workspace/MetanomeTestRunner/data folder.`
2. Set dataset configureation in the `/workspace/MetanomeTestRunner/src/main/java/de/uni_potsdam/hpi/metanome_test_runner/config/Config.java`. Make sure to add the dataset name in the 'Dataset' collection.

## Configuration setting
`algorithmArg datasetArg approximation_degree cross_col_min_overlap cross_col_bollean`
