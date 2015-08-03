package com.serotonin.goid.util2d;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.util.List;

import com.serotonin.goid.util.RenderUtils;
import com.serotonin.goid.util.Renderable;

public class Obstacle extends ShapeDelegator<Path2D.Float> implements Renderable {
    private static final Color OUTLINE_COLOR = new Color(0x40, 0x20, 0);
    private static final Color FILL_COLOR = new Color(0x80, 0x40, 0);

    public Obstacle(int[] xs, int[] ys) {
        super(new Path2D.Float());

        if (xs.length != ys.length)
            throw new IllegalArgumentException("arrays are of difference lengths");

        shape.moveTo(xs[0], ys[0]);
        for (int i = 1; i < xs.length; i++)
            shape.lineTo(xs[i], ys[i]);
        shape.closePath();
    }

    public Obstacle(int[][] points) {
        this(points, 0, 0);
    }

    public Obstacle(int[][] points, int offsetX, int offsetY) {
        super(new Path2D.Float());

        shape.moveTo(points[0][0] + offsetX, points[0][1] + offsetY);
        for (int i = 1; i < points.length; i++)
            shape.lineTo(points[i][0] + offsetX, points[i][1] + offsetY);
        shape.closePath();
    }

    public Obstacle(List<Point> points) {
        this(points, 0, 0);
    }

    public Obstacle(List<Point> points, int offsetX, int offsetY) {
        super(new Path2D.Float());

        if (!points.isEmpty()) {
            Point p = points.get(0);
            shape.moveTo(p.x + offsetX, p.y + offsetY);

            for (int i = 1; i < points.size(); i++) {
                p = points.get(i);
                shape.lineTo(p.x + offsetX, p.y + offsetY);
            }

            shape.closePath();
        }
    }

    public void render(Graphics2D g) {
        RenderUtils.renderOutlined(g, shape, FILL_COLOR, OUTLINE_COLOR);
    }
}
