package utils;

import org.tartarus.snowball.ext.englishStemmer;

public class Node {

    private final String word; // the actual word in the corpus
    private final String pTag; // part of speech tag
    private final String label; // depth label
    private final int head; // the head of the current token
    private final englishStemmer englishStemmer = new englishStemmer();

    public Node(String[] split) {
        String stem = StringBuilder(split);

        // todo: check stemmer
        englishStemmer.setCurrent(arr[0]);
        englishStemmer.stem();
        String word1 = englishStemmer.getCurrent();

        Stemmer stemmer = new Stemmer();
//        stemmer.add(stem.toCharArray(), stem.length());
//        stemmer.stem();
        this.word = stemmer.toString();
        this.pTag = split[split.length - 3].toLowerCase();
        this.label = split[split.length - 2];
        this.head = Integer.parseInt(split[split.length - 1]);
    }

    public static String StringBuilder(String[] split) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < split.length - 3; index++) {
            builder.append(split[index]).append("/");
        }
        builder.setLength(builder.length() - 1);
        String to_stem = builder.toString();
        return to_stem;
    }

    public String getWord() {
        return word;
    }

    public String getPosTag() {
        return pTag;
    }

    public String getDepLabel() {
        return label;
    }

    public int getHead() {
        return head;
    }

}