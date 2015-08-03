package com.serotonin.goid.applet;

import java.awt.geom.Point2D;

import com.serotonin.goid.util2d.GeomUtils;
import com.serotonin.goid.util2d.PolarPoint2D;

public class ScriptUtils {
    public double normalizeAngle(double a) {
        return GeomUtils.normalizeAngle(a);
    }

    public double minimumDifference(double angle1, double angle2) {
        return GeomUtils.minimumDifference(angle1, angle2);
    }

    public PolarPoint2D polarCoords(double radius, double angle) {
        return new PolarPoint2D(radius, angle);
    }

    public PolarPoint2D polarCoords(Point2D cartesian) {
        return new PolarPoint2D(cartesian);
    }

    public int nextInt(int max) {
        return MainPanel.RANDOM.nextInt(max);
    }

    public double constrain(double d, double magnitude) {
        return GeomUtils.constrain(d, magnitude);
    }
}
