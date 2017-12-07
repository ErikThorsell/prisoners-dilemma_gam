public class Touble {
    public double x;
    public double y;

    public Touble (double b1, double b2) {
        x = b1;
        y = b2;
    }

    @Override
    public String toString() {
        return "(" + Double.toString(x) + "," + Double.toString(y) + ")";
    }
}
