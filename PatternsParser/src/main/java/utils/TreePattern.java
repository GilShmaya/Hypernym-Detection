package utils;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import java.util.List;

import utils.FeaturesVectorLength;
import utils.PairOfNouns;

public class TreePattern {
    private String StringPattern;
    private PairOfNouns pairOfNouns;

    public TreePattern(List<Node> path, IntWritable total_count) {

        Node treeSource =path.get(0);
        Node treeTarget = path.get(path.size()-1);

        Text text1 = new Text(treeSource.getWord());
        Text text2 = new Text(treeTarget.getWord());
        PairOfNouns newPair = new PairOfNouns(text1, text2, total_count);
        this.pairOfNouns = newPair;

        // update pattern
        this.StringPattern = "";
        for(int index=0; index<path.size(); index++){
            if(index == path.size() -1){
                this.StringPattern += path.get(index).getPosTag();
            }
            else{
                this.StringPattern += path.get(index).getPosTag() +":"+path.get(index).getDepLabel()+":";
            }
        }
    }

    public String getPattern() {
        return StringPattern;
    }

    public PairOfNouns getPair() {
        return pairOfNouns;
    }
}