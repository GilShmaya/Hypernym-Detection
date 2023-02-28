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

1) step 1 - PatternParser :
###### Mapper :
Gets the ngram resource input (Google Syntactic Ngrams) and parses each sentence to a dependency tree. 
after creating the tree - creates a key value output for each path in the tree : <String pattern, Noun pair>. 
###### Reducer :
Creates an output (key, value) only for the patterns that has more noun pairs than dpmin.
those patterns will be considered as Features and will appear in the final vector with special index entry. 
<Key, value> : <NounPair, index of the pattern in the final vector>. 

2) step 2 - FeaturesVectorBuilder
The input for the second step: (1) the patterns with their specific index (from step1) (2) The given annotated set (hypernym.txt).
###### Mapper : ?
The Map step of the second job is to go over the annotated set and the output pairs from the first job and to create to following output :
###### Reducer : ?
 

##### Second part -

The classifier parse the output and runs Weka software with "10-fold cross-validation" - 
a method of evaluating a model's performance by dividing the data into 10 subsets and iteratively training and testing the model on different subsets.


### Program components :

##### components in step 1 (PatternParser) :
1. Dependency Tree - represent a sentence in a form of dependency tree. uses to get the patterns between different nodes (words).
2. Node - represent a single node from the dependency tree.

##### components in step 2 (FeaturesVectorBuilder) :
1. PatternInfo - ????

##### shared components :
3. PairOfNouns - represents a pair of noun taken from an input sentence (contains a boolean indicates whether the pairs are 
hpernm and holds a counter represent the total count of the specific pair).
2. FeaturesVectorLength - ????


### Communication : 


### Results (Precision, Recall and F1 measures) :


### Analysis :


