package com.serotonin.goid.util2d;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class Circle extends Ellipse2D.Double {
    private static final long serialVersionUID = 1L;

    public Circle(double x, double y, double radius) {
        super(x - radius, y - radius, radius * 2, radius * 2);
    }

    public void setCenter(Point2D p) {
        setCenter(p.getX(), p.getY());
    }

    public void setCenter(double x, double y) {
        super.x = x - super.width / 2;
        super.y = y - super.height / 2;
    }

    public void setLocation(Circle that) {
        x = that.x;
        y = that.y;
    }

    public void translate(Point2D p) {
        translate(p.getX(), p.getY());
    }

    public void translate(double x, double y) {
        super.x += x;
        super.y += y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
