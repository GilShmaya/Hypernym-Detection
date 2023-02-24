package utils;

public class Node {

    private String word;
    private String ptag;
    private String label;
    private int head;

    public Node(String[] split) {
        String stem = StringBuilder(split);

        // todo: check stemmer
        Stemmer stemmer = new Stemmer();
        stemmer.add(stem.toCharArray(), stem.length());
        stemmer.stem();
        this.word = stemmer.toString();
        this.ptag = split[split.length - 3].toLowerCase();
        this.label = split[split.length - 2];
        this.head = Integer.parseInt(split[split.length - 1]);
    }

    public static String StringBuilder (String[] split) {
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
        return ptag;
    }

    public String getDepLabel() {
        return label;
    }

    public int getHead() {
        return head;
    }

}