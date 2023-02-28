# Hypernym-Detection


### Design :

#### Overview Program Flow :

In this assignment we experienced with a research paper - "Learning syntactic patterns for automatic hypernym discovery", re-designing its algorithm for the map-reduce pattern and experimenting its quality on a large-scale input as follows :
1. Go over the input corpus - "Google Syntactic N-Grams" and extract the path between each pair
of words that appear in the 'Biarcs' data set. each special path - mark as a pattern (while ignoring paths  that contains less distinct noun pairs than DPmin (dpmin = The minimal number of unique noun pairs for each dependency path) Dependency path with less distinct noun-pairs should not be considered as a feature.
2. Build the Features vector for each noun pair (from the patterns collection).
3. Saving a boolean parameter for each pattern, indicates whether the pair of words are hypernym (based on the annotated set - hypernym.txt)
4. Training part: building a classifier-  using Weka software for classification, based on the results from the map-reduce steps (features vector) and the annotated set we train a classifier that determine if a noun pair is in the hypernym/hyponym relation or not.



#### First part - 

The first part of the program is built using MapReduce system consist of 2 steps - PatternParser & FeaturesVectorBuilder

##### step 1 - PatternParser

The PatternParser job is responsible for the following:
 * Parse each sentence to a dependency tree
 * Check for each pattern if there are less distinct noun pairs than dpmin.

Mapper: 
- Mapper class : Map every line of PatternParser's output into a dependency tree and for each path in the tree creates the output :
<Pattern, PairofNouns>
- Reducer: emit <PairOfNouns, index> for every noun of pairs that has more noun pairs than dpmin.

##### step 2 - FeaturesVectorBuilder

The FeaturesVectorBuilder job is responsible for the following:
* Parse the data from the annotated set.
* Stem the pair.
* Aggregate the annotated set data & the PatternParser's output.
* Create a features vector for each noun pair 

Mapper: 
- Mapper class : Map every sentence from the Ngrams input into <pairOfNouns(w1, w2, false, total), patternIndex>
- MapperClassAnnotated : Map every line of the annotated set into <pairOfNouns(w1, w2, isHypernym, -1), -1>

Reducer: Creates a features vector for each noun pair.




#### Second part -

The classifier parse the output and runs Weka software with "10-fold cross-validation" - 
a method of evaluating a model's performance by dividing the data into 10 subsets and iteratively training and testing the model on different subsets.





### Program components :

##### components in step 1 (PatternParser) :
* Dependency Tree - represent a sentence in a form of dependency tree. uses to get the patterns between different nodes (words).
* Node - represent a single node from the dependency tree.

##### shared components :
* PairOfNouns - represents a pair of noun taken from an input sentence (contains a boolean indicates whether the pairs are 
hpernm and holds a counter represent the total count of the specific pair).
* FeaturesVectorLength - a singleton class that responsible for export and import the features vector length to S3.




### Communication : 
step1:    
    Map input records=21471169
    Map output records=9625754
		Map output bytes=345408131
		Map output materialized bytes=100806078
		Input split bytes=508
		Combine input records=0
		Combine output records=0
		Reduce input groups=20261
		Reduce shuffle bytes=100806078
		Reduce input records=9625754
		Reduce output records=9430041
step2:
    Map input records=9572566
		Map output records=9572566
		Map output bytes=248809615
		Map output materialized bytes=66704952
		Input split bytes=1533
		Combine input records=0
		Combine output records=0
		Reduce input groups=1447410
		Reduce shuffle bytes=66704952
		Reduce input records=9572566
		Reduce output records=1296608


### Results (Precision, Recall and F1 measures) :
DPmin = 50:
- Precision ~ 0.71
- F1 measures ~ 0.76
- Recall ~ 0.95

DPmin=100:
- Precision ~ 0.74
- F1 measures ~ 0.85
- Recall ~ 0.97




### Analysis :
- False Positive:
  dog book
  two level
  Tree Negative 
  red type
  1950 industri

- True Positive:
 1 number
 2010 year
 -1 Negative
 cat pet
 blue color

- False Negative –
  left direction
  exciting emotion
  rock music
  submit action


Upon examining the vectors of the false noun-pair classes generated in the second map-reduce phase, we observed that they generally had low values and sparse vectors compared to the positive noun-pair classes. Consequently, it is probable that the classifier lacks adequate information to accurately classify these noun-pairs.

It is evident that many of the false cases in classification arise due to the presence of the postTags 'in' or 'to' in the shortest path between the words of the noun-pair. This occurrence is common in both true positive and true negative classifications and therefore decreases the accuracy of the classifier. For instance, the shortest path for the true positive noun-pair "1 number" contains the dependency relation 'nn:dep:in:prep:nns', while the false positive noun-pair has a dependency relation of 'nns:pobj:in:prep:nns'.

