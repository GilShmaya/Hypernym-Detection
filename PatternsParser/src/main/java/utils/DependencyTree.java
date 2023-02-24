package utils;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DependencyTree {
    private final static List<String> listOfNouns = Arrays.asList("nn", "nns", "nnp", "nnps");

    private final SimpleDirectedGraph<Node, DefaultEdge> tree;
    private final int count;
    private final List<TreePattern> patterns;


    public DependencyTree(String line) {
        String[] splitLine = line.split("\t");
        List<Node> treeNodes = this.parsing(splitLine[1]);
        this.count = Integer.parseInt(splitLine[2]);

        tree = new SimpleDirectedGraph<>(DefaultEdge.class);
        // build the tree
        for (Node node : treeNodes) {
            tree.addVertex(node);
        }
        for (Node node : treeNodes) {
            if (node.getHead() > 0)
                tree.addEdge(treeNodes.get(node.getHead() - 1), node);
        }
        // find the tree patterns
        this.patterns = new ArrayList<>();
        findPatterns(treeNodes, listOfNouns);
    }

    private List<Node> parsing (String s) {
        String[] split = s.split(" ");
        List<Node> nodes = new ArrayList<>();

        for (String node : split) {
            String[] splitLine = node.split("/");
            nodes.add(new Node(splitLine));
        }

        return nodes;
    }

    public List<TreePattern> patterns() {
        return patterns;
    }


    private void findPatterns(List<Node> nodes, List<String> nouns) {
        DijkstraShortestPath shortestPath = new DijkstraShortestPath(this.tree);
        for (Node node1 : nodes) {
            if (!nouns.contains(node1.getPosTag()))
                continue;
            for (Node node2 : nodes) {
                if (node1.equals(node2) || !nouns.contains(node2.getPosTag()))
                    continue;
                GraphPath<Node, DefaultEdge> path = shortestPath.getPath(node1, node2);
                if (path != null && path.getVertexList().size() > 1) {
                    patterns.add(new TreePattern(path.getVertexList(), count));
                }
            }
        }
    }
}