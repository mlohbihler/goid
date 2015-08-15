package goid.simulation.util;

public class FloatPoint {
    public float x;
    public float y;

    public FloatPoint() {
        this(0, 0);
    }

    public FloatPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void add(float x, float y) {
        this.x += x;
        this.y += y;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public FloatPoint times(float multiplicand) {
        return new FloatPoint(x * multiplicand, y * multiplicand);
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public int xInt() {
        return (int) x;
    }

    public int yInt() {
        return (int) y;
    }
}
