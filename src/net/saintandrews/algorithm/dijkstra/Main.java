package net.saintandrews.algorithm.dijkstra;

import java.io.File;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        printShortestPath(1, 0, 3);
        printShortestPath(2, 0, 7);
        printShortestPath(3, 0, 9);
        printShortestPath(4, 0, 16);
        printShortestPath(4, 13, 9);
    }

    private static final String GRAPH_PREFIX = "graphs/graph", GRAPH_POSTFIX = ".txt";

    private static void printShortestPath(int graphNumber, int from, int to) {
        Graph graph = Graph.fromFile(new File(GRAPH_PREFIX + graphNumber + GRAPH_POSTFIX));
        Graph.ShortestPath shortestPaths = graph.shortestPathFrom(from);
        System.out.println("Distance: " + shortestPaths.distances[to] + '\t' + Arrays.toString(shortestPaths.to(to)));
    }
}
