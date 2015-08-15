package goid.simulation.util;

import java.awt.Point;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicPoint {
    final public AtomicInteger x;
    final public AtomicInteger y;

    public AtomicPoint() {
        this(0, 0);
    }

    public AtomicPoint(int x, int y) {
        this.x = new AtomicInteger(x);
        this.y = new AtomicInteger(y);
    }

    public void add(int x, int y) {
        this.x.addAndGet(x);
        this.y.addAndGet(y);
    }

    public void set(int x, int y) {
        this.x.set(x);
        this.y.set(y);
    }

    public Point times(float multiplicand) {
        return new Point((int) (x.get() * multiplicand), (int) (y.get() * multiplicand));
    }

    public int x() {
        return x.get();
    }

    public int y() {
        return y.get();
    }
}
