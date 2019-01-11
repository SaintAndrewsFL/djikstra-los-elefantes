package net.saintandrews.algorithm.dijkstra;

/**
 * The left half of the long is the vertex, the right half is the distance.
 */
class EdgeMinHeap {

    /**
     * Stores two ints in a long.
     * a = (int) (long >> 32)
     * b = (int) long;
     *
     * @param a
     * @param b
     * @return long storing the two ints
     */
    public static long intsToLong(int a, int b) {
        return (long) a << 32 | b & 0xffffffffL;
    }

    private long[] tree;
    private int size;

    public EdgeMinHeap(int initialCapacity) {
        if (initialCapacity > 0)
            this.tree = new long[initialCapacity];
        else
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * @return the vertex of the edge with the lowest weight.
     */
    public int removeMin() {
        if (size == 0)
            throw new RuntimeException("EdgeMinHeap is empty");
        final int min = (int) (tree[0] >> 32); // vertex of the edge with the lowest weight
        size--;
        tree[0] = tree[size];
        if (size != 0) {
            int currentIndex = 0;
            while (currentIndex < size / 2) { // while not a leaf node
                int child = 2 * currentIndex + 1;
                if (child < size - 1 && (int) tree[child] > tree[child + 1])
                    child++; // makes this the index of child with the larger value
                if ((int) tree[currentIndex] <= (int) tree[child])
                    break;
                long temp = tree[currentIndex];
                tree[currentIndex] = tree[child];
                tree[child] = temp;
                currentIndex = child;  // Move down
            }
        }
        return min;
    }

    public void put(int vertex, int distance) {
        long element = intsToLong(vertex, distance);
        int currentIndex;
        tree[currentIndex = size] = element;
        size++;
        if (currentIndex == 0)
            return;

        int compareIndex = (currentIndex - 1) / 2; // Parent of last element
        while (compareIndex != -1 && (int) tree[compareIndex] > (int) element) {
            tree[currentIndex] = tree[compareIndex];
            tree[compareIndex] = element;
            currentIndex = compareIndex;
            compareIndex = (compareIndex - 1) / 2; // Parent
        }
    }

    @Override
    public String toString() {
        int iMax = size - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append((int) (tree[i] >> 32)).append(':').append((int) tree[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }
}

