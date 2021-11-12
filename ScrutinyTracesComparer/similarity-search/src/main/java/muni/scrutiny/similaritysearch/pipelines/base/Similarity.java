package muni.scrutiny.similaritysearch.pipelines.base;

public class Similarity implements Comparable<Similarity> {
    private final int firstIndex;
    private final int lastIndex;
    private final double distance;

    public Similarity(int firstIndex, int lastIndex, double distance) {
        this.firstIndex = firstIndex;
        this.lastIndex = lastIndex;
        this.distance = distance;
    }

    public int getFirstIndex() {
        return firstIndex;
    }

    public int getLastIndex() {
        return lastIndex;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return "<" + firstIndex + ", "+ lastIndex +"> at distance: " + distance + "\n";
    }

    @Override
    public int compareTo(Similarity s) {
        if (this.getDistance() == s.getDistance())
            return 0;
        if (this.getDistance() > s.getDistance())
            return 1;
        return -1;
    }
}