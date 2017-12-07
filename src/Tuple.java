public class Tuple {
    public byte x;
    public byte y;

    Tuple (byte b1, byte b2) {
        x = b1;
        y = b2;
    }

    @Override
    public String toString() {
        return "(" + Byte.toString(x) + "," + Byte.toString(y) + ")";
    }
}
