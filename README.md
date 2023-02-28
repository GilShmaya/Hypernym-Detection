# Hypernym-Detection


### Design :

#### Overview Program Flow :

In this assignment we experienced with a research paper - "Learning syntactic patterns for automatic hypernym discovery", re-designing its algorithm for the map-reduce pattern and experimenting its quality on a large-scale input as follows :
1. Go over the input corpus - "Google Syntactic N-Grams" and extract the path between each pair
of words that appear in the 'Biarcs' data set. each special path - mark as a pattern (while ignoring paths  that contains less distinct noun pairs than DPmin (dpmin = The minimal number of unique noun pairs for each dependency path) Dependency path with less distinct noun-pairs should not be considered as a feature.
2. Build the Features vector for each noun pair (from the patterns collection).
3. Saving a boolean parameter for each pattern, indicates whether the pair of words are hypernym (based on the annotated set - hypernym.txt)
4. Training part: building a classifier-  using Weka software for classification, based on the results from the map-reduce steps (features vector) and the annotated set we train a classifier that determine if a noun pair is in the hypernym/hyponym relation or not.
