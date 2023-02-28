# Hypernym-Detection


### Design :

#### Overview Program Flow :

In this assignment we experienced with a research paper - "Learning syntactic patterns for automatic hypernym discovery", re-designing its algorithm for the map-reduce pattern and experimenting its quality on a large-scale input as follows :
1. Go over the input corpus - "Google Syntactic N-Grams" and extract the path between each pair
of words that appear in the 'Biarcs' data set. each special path - mark as a pattern (while ignoring paths  that contains less distinct noun pairs than DPmin (dpmin = The minimal number of unique noun pairs for each dependency path) Dependency path with less distinct noun-pairs should not be considered as a feature.
2. Build the Features vector for each noun pair (from the patterns collection).
3. Saving a boolean parameter for each pattern, indicates whether the pair of words are hypernym (based on the annotated set - hypernym.txt)
4. Training part: building a classifier-  using Weka software for classification, based on the results from the map-reduce steps (features vector) and the annotated set we train a classifier that determine if a noun pair is in the hypernym/hyponym relation or not.


##### First part - 

The first part of the program is built using MapReduce system consist of 2 steps - PatternParser & FeaturesVectorBuilder

1. step 1 - PatternParser

The PatternParser job is responsible for the following:
 * Parse each sentence to a dependency tree
 * Check for each pattern if there are less distinct noun pairs than dpmin.

Mapper: 
- Mapper class : Map every line of PatternParser's output into a dependency tree and for each path in the tree creates the output :
<Pattern, PairofNouns>
- Reducer: emit <PairOfNouns, index> for every noun of pairs that has more noun pairs than dpmin.

2. step 2 - FeaturesVectorBuilder

The FeaturesVectorBuilder job is responsible for the following:
* Parse the data from the annotated set.
* Stem the pair.
* Aggregate the annotated set data & the PatternParser's output.
* Create a features vector for each noun pair 

Mapper: 
- Mapper class : Map every sentence from the Ngrams input into <pairOfNouns(w1, w2, false, total), patternIndex>
- MapperClassAnnotated : Map every line of the annotated set into <pairOfNouns(w1, w2, isHypernym, -1), -1>

Reducer: Creates a features vector for each noun pair.

##### Second part -

The classifier parse the output and runs Weka software with "10-fold cross-validation" - 
a method of evaluating a model's performance by dividing the data into 10 subsets and iteratively training and testing the model on different subsets.


### Program components :

##### components in step 1 (PatternParser) :
* Dependency Tree - represent a sentence in a form of dependency tree. uses to get the patterns between different nodes (words).
* Node - represent a single node from the dependency tree.

##### components in step 2 (FeaturesVectorBuilder) :
* PatternInfo - ????

##### shared components :
* PairOfNouns - represents a pair of noun taken from an input sentence (contains a boolean indicates whether the pairs are 
hpernm and holds a counter represent the total count of the specific pair).
* FeaturesVectorLength - ????


### Communication : 


### Results (Precision, Recall and F1 measures) :


### Analysis :


