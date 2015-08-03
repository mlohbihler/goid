package com.serotonin.goid.util2d;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ShapeDelegator<T extends Shape> implements Shape {
    protected final T shape;

    public ShapeDelegator(T shape) {
        this.shape = shape;
    }

    public boolean contains(double x, double y, double w, double h) {
        return shape.contains(x, y, w, h);
    }

    public boolean contains(double x, double y) {
        return shape.contains(x, y);
    }

    public boolean contains(Point2D p) {
        return shape.contains(p);
    }

    public boolean contains(Rectangle2D r) {
        return shape.contains(r);
    }

    public Rectangle getBounds() {
        return shape.getBounds();
    }

    public Rectangle2D getBounds2D() {
        return shape.getBounds2D();
    }

    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return shape.getPathIterator(at, flatness);
    }

    public PathIterator getPathIterator(AffineTransform at) {
        return shape.getPathIterator(at);
    }

    public boolean intersects(double x, double y, double w, double h) {
        return shape.intersects(x, y, w, h);
    }

    public boolean intersects(Rectangle2D r) {
        return shape.intersects(r);
    }
}
