package chartprocessing;

public class Boundary {
    private double lowerBound;
    private double upperBound;
    private int lowerBoundIndex;
    private int upperBoundIndex;

    public Boundary(double lowerBound, double upperBound, int lowerBoundIndex, int upperBoundIndex) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.lowerBoundIndex = lowerBoundIndex;
        this.upperBoundIndex = upperBoundIndex;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }

    public int getLowerBoundIndex() {
        return lowerBoundIndex;
    }

    public void setLowerBoundIndex(int lowerBound) {
        this.lowerBoundIndex = lowerBoundIndex;
    }

    public int getUpperBoundIndex() {
        return upperBoundIndex;
    }

    public void setUpperBoundIndex(int upperBound) {
        this.upperBoundIndex = upperBoundIndex;
    }
}
