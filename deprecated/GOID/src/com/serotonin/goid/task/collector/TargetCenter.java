package com.serotonin.goid.task.collector;

import java.awt.geom.Point2D;

public class TargetCenter {
    private final Point2D point;

    public TargetCenter(Point2D point) {
        this.point = point;
    }

    public Point2D getPoint() {
        return point;
    }
}
