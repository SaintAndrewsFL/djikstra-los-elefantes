package net.saintandrews.algorithm.dijkstra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Stores a Graph using an adjacency matrix. Provides static utility methods for loading a graph from a text file.
 */
public class Graph {
    /**
     * For efficiency, a long is used to store two ints. The weight of the edge and the the index of the vertex it is attached to.
     */
    public final long[][] adjacencyList;

    public Graph(int numberOfVertices) {
        adjacencyList = new long[numberOfVertices][];
    }

    @Override
    public String toString() {
        int iMax = adjacencyList.length - 1;
        if (iMax == -1)
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (int i = 0; ; i++) {
            sb.append(i).append(':');
            edgeArrayToString(sb, adjacencyList[i]);
            if (i == iMax)
                return sb.append('}').toString();
            sb.append(", ");
        }
    }

    private static StringBuilder edgeArrayToString(StringBuilder sb, long[] arr) {
        int iMax = arr.length - 1;
        if (iMax == -1)
            return sb.append("[]");

        sb.append('[');
        for (int i = 0; ; i++) {
            sb.append((int) (arr[i] >> 32)).append(':').append((int) arr[i]);
            if (i == iMax)
                return sb.append(']');
            sb.append(", ");
        }
    }

    public ShortestPath shortestPathFrom(int start) {
        return new ShortestPath(start);
    }

    public final class ShortestPath {
        public final int[] distances, pathVertices;
        private final int start;

        /**
         * Finds the shortest distance to every other vertex on the graph, storing them in {@link #distances}.
         * <p>
         * Furthermore, stores the vertex from which the shortest path arrives to for each vertex in {@link #pathVertices}.
         *
         * @param start
         */
        private ShortestPath(int start) {
            this.start = start;
            this.distances = new int[adjacencyList.length];
            this.pathVertices = new int[adjacencyList.length];

            for (int i = 0; i < distances.length; i++)
                distances[i] = Integer.MAX_VALUE;
            distances[start] = 0;

            EdgeMinHeap minHeap = new EdgeMinHeap(adjacencyList.length);
            minHeap.put(start, 0);

            boolean[] marked = new boolean[adjacencyList.length];
            while (!minHeap.isEmpty()) {
                int vertex = minHeap.removeMin();
                if (marked[vertex]) continue;
                marked[vertex] = true;
                for (long edge : adjacencyList[vertex]) {
                    int toVertex = (int) (edge >> 32), toDistance = distances[vertex] + (int) edge;
                    if (toVertex == start)
                        continue;
                    if (distances[toVertex] > toDistance) {
                        distances[toVertex] = toDistance;
                        pathVertices[toVertex] = vertex;
                        if (!marked[toVertex])
                            minHeap.put(toVertex, distances[toVertex]);
                    }
                }
            }
        }

        /**
         * @param to the destination vertex
         * @return an array of vertices including the start and end.
         */
        public int[] to(int to) {
            if (to > distances.length)
                throw new IllegalArgumentException("Destination vertex does not exist");
            if (pathVertices[to] == start)
                return new int[]{start, to};
            List<Integer> path = new ArrayList<>();
            path.add(to);
            for (int v = to; (v = pathVertices[v]) != start; )
                path.add(v);
            path.add(start);
            int[] pathInt = new int[path.size()];
            for (int i = 0; i < path.size(); i++) // Reverse the array
                pathInt[pathInt.length - i - 1] = path.get(i);
            return pathInt;
        }
    }

    /**
     * The last line of the file should be either "directed" for a directed graph or "undirected" for an undirected one.
     *
     * @see #fromLines(List, boolean)
     */
    public static Graph fromFile(File file) {
        List<String> lines = readLines(file);
        if (lines.size() <= 1)
            throw new RuntimeException("Graph file contains no vertices.");
        return fromLines(lines.subList(0, lines.size() - 1), lines.get(lines.size() - 1).equalsIgnoreCase("directed"));
    }

    /**
     * Loads an undirected graph from a list of adjacent vertices.
     * <p>
     * The first number is the line number of connected vertex.
     * The second is weight of this edge (an integer).
     * These pairs of numbers can be repeated to form as many edges between vertices as desired.
     * Here is a square, undirected graph with equally weighted edges.
     * <pre>
     * 2 1
     * 3 1
     * 4 1
     * 1 1
     * </pre>
     * <p>
     * Empty lines are valid vertices.
     *
     * @param lines
     * @param isDirected Whether or not to make directed edges
     * @return
     */
    public static Graph fromLines(List<String> lines, boolean isDirected) {
        final class Edge {
            final int vertex, weight;

            public Edge(int vertex, int weight) {
                this.vertex = vertex;
                this.weight = weight;
            }

            public long toLong() {
                return EdgeMinHeap.intsToLong(vertex, weight);
            }

            @Override
            public boolean equals(Object other) {
                if (!(other instanceof Edge))
                    return false;
                return this.vertex == ((Edge) other).vertex;
            }

            @Override
            public int hashCode() {
                return vertex;
            }
        }

        @SuppressWarnings("unchecked")
        HashSet<Edge>[] adjacencyList = new HashSet[lines.size()];
        for (int i = 0; i < adjacencyList.length; i++)
            adjacencyList[i] = new HashSet<>();

        ListIterator<String> iterator = lines.listIterator();
        while (iterator.hasNext()) {
            Scanner scanner = new Scanner(iterator.next());
            while (scanner.hasNextInt()) {
                int vertex = -1, weight;
                try {
                    vertex = scanner.nextInt() - 1;
                    weight = scanner.nextInt();
                } catch (InputMismatchException e) {
                    throw new RuntimeException("Vertex on line " + iterator.nextIndex() + " is malformed. Make sure all values are valid ints");
                } catch (NoSuchElementException e) {
                    throw new RuntimeException("Vertex on line " + iterator.nextIndex() + " has an edge without a weight: " + (vertex + 1));
                }
                if (vertex >= lines.size())
                    throw new RuntimeException("Vertex on line " + iterator.nextIndex() + " connected to non-existent vertex: " + (vertex + 1));
                // Store the weight of each vertex in a long for efficiency
                boolean edgeExists = !adjacencyList[iterator.previousIndex()].add(new Edge(vertex, weight));
                // If the graph is undirected, add the reverse edge also
                if (!isDirected)
                    edgeExists |= !adjacencyList[vertex].add(new Edge(iterator.previousIndex(), weight));
                if (edgeExists)
                    throw new RuntimeException("Invalid graph. Vertex on line " + iterator.nextIndex() + " is defined twice!");
            }
        }

        Graph graph = new Graph(lines.size());
        for (int i = 0; i < graph.adjacencyList.length; i++)
            graph.adjacencyList[i] = adjacencyList[i].stream().mapToLong(Edge::toLong).toArray();
        return graph;
    }

    /**
     * Reads all the lines from a text file, preserving while empty lines.
     *
     * @param file
     * @return
     */
    private static List<String> readLines(File file) {
        if (file == null)
            throw new NullPointerException("File is null.");
        if (!file.exists())
            throw new RuntimeException("File (" + file + ") does not exist.");
        if (file.isDirectory())
            throw new RuntimeException("File (" + file + ") is a directory.");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            char[] buffer = new char[64];
            int charsRead, lastCharsRead = 0;
            StringBuilder sb = new StringBuilder();
            // Empty lines are valid vertices. Using readLine() strips the newline characters, so we would have no way of knowing if there was an empty last line.
            while ((charsRead = reader.read(buffer)) > 0)
                sb.append(buffer, 0, lastCharsRead = charsRead);
            if (lastCharsRead == 0)
                return Collections.emptyList();
            // -1 limit allows .split(...) to return empty substrings
            return Arrays.asList(sb.toString().split("\\r\\n|\\n|\\r", -1));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}