package com.serotonin.goid.util2d;

import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.serotonin.NotImplementedException;

public class GeomUtils {
    public static final double TWOPI = 2 * Math.PI;

    /**
     * Returns the intersection point of the given infinite lines. If the lines are parallel, null is returned.
     */
    public static Point2D lineIntersectionPoint(double x1, double y1, double slope1, double x2, double y2, double slope2) {
        if (slope1 == slope2)
            return null;

        double yinter1 = yIntersect(x1, y1, slope1);
        double yinter2 = yIntersect(x2, y2, slope2);

        double x, y;
        if (Double.isInfinite(slope1)) {
            x = x1;
            y = slope2 * x + yinter2;
        }
        else if (Double.isInfinite(slope2)) {
            x = x2;
            y = slope1 * x + yinter1;
        }
        else {
            x = (yinter2 - yinter1) / (slope1 - slope2);
            y = slope1 * x + yinter1;
        }

        return new Point2D.Double(x, y);
    }

    /**
     * Returns the intersection point of the infinite lines represented by the given line objects. If the lines are
     * parallel, null is returned.
     */
    public static Point2D lineIntersectionPoint(Line2D line1, Line2D line2) {
        return lineIntersectionPoint(line1.getX1(), line1.getY1(), slope(line1), line2.getX1(), line2.getY1(),
                slope(line2));
    }

    /**
     * Returns the intersection point of the line segments represented by the given line objects. If the line segments
     * do not meet or the lines are parallel, null is returned.
     */
    public static Point2D segmentIntersectionPoint(Line2D line1, Line2D line2) {
        if (line1.intersectsLine(line2))
            return lineIntersectionPoint(line1, line2);
        return null;
    }

    public static void adjustLength(Line2D segment, double coef) {
        segment.setLine(segment.getX1(), segment.getY1(), (segment.getX2() - segment.getX1()) * coef + segment.getX1(),
                (segment.getY2() - segment.getY1()) * coef + segment.getY1());
    }

    public static void adjustLength(Point2D vector, double coef) {
        vector.setLocation(vector.getX() * coef, vector.getY() * coef);
    }

    /**
     * This method returns only one of two possible points on the line with the given distance from the given point. The
     * second point is trivially derived as (2x - ex, 2y - ey), where x and y were given, and ex and ey were returned.
     * Use the mirrorPoint method to change the point accordingly.
     * 
     * @param x
     * @param y
     * @param slope
     * @param length
     * @return
     */
    public static Point2D endPoint(double x, double y, double slope, double length) {
        if (slope == 0)
            return new Point2D.Double(x + length, y);
        if (slope == Double.POSITIVE_INFINITY)
            return new Point2D.Double(x, y + length);
        if (slope == Double.NEGATIVE_INFINITY)
            return new Point2D.Double(x, y - length);

        double dx = length / Math.sqrt(slope * slope + 1);
        return new Point2D.Double(x + dx, y + dx * slope);
    }

    public static void mirrorPoint(Point2D pointToMirror, double x, double y) {
        pointToMirror.setLocation(x * 2 - pointToMirror.getX(), y * 2 - pointToMirror.getY());
    }

    public static double slope(Line2D line) {
        if (line.getX1() == line.getX2()) {
            if (line.getY1() > line.getY2())
                return Double.NEGATIVE_INFINITY;
            return Double.POSITIVE_INFINITY;
        }
        return (line.getY2() - line.getY1()) / (line.getX2() - line.getX1());
    }

    public static double yIntersect(Point2D point, double slope) {
        return yIntersect(point.getX(), point.getY(), slope);
    }

    public static double yIntersect(double x, double y, double slope) {
        if (Double.isInfinite(slope))
            return Double.NaN;
        return y - (x * slope);
    }

    public static double negativeInverse(double a) {
        if (a == 0)
            return Double.POSITIVE_INFINITY;
        return -1 / a;
    }

    public static double normalBound(double a) {
        if (a > 1)
            a = 1;
        if (a < 0)
            a = 0;
        return a;
    }

    public static double normalZeroBound(double a) {
        if (a > 1)
            a = 1;
        if (a < -1)
            a = -1;
        return a;
    }

    public static double normalizeAngle(double a) {
        a = a % TWOPI;
        if (a > Math.PI)
            a -= TWOPI;
        else if (a < -Math.PI)
            a += TWOPI;
        return a;
    }

    public static double normalizeAnglePositive(double a) {
        if (a >= 0 && a < TWOPI)
            return a;
        return (a + TWOPI) % TWOPI;
    }

    public static double minimumDifference(double angle1, double angle2) {
        angle1 = normalizeAngle(angle1);
        angle2 = normalizeAngle(angle2);

        double diff = angle1 - angle2;
        if (diff < -Math.PI)
            return diff + Math.PI * 2;
        if (diff > Math.PI)
            return diff - Math.PI * 2;
        return diff;
    }

    public static double constrain(double d, double magnitude) {
        return constrain(d, -magnitude, magnitude);
    }

    public static boolean betweenInclusive(double value, double b1, double b2) {
        if (b1 > b2)
            return b2 <= value && value <= b1;
        return b1 <= value && value <= b2;
    }

    public static double constrain(double d, double min, double max) {
        if (d > max)
            return max;
        if (d < min)
            return min;
        return d;
    }

    /**
     * Returns the nearest point on the infinite line to the given point.
     */
    public static Point2D nearestPoint(Point2D point, double x, double y, double slope) {
        return nearestPoint(point.getX(), point.getY(), x, y, slope);
    }

    public static Point2D nearestPoint(double px, double py, double lx, double ly, double slope) {
        return lineIntersectionPoint(lx, ly, slope, px, py, negativeInverse(slope));
    }

    /**
     * Returns the nearest point on the line to the given point. If the nearest point on the infinite line is not on the
     * line segment, the nearest endpoint is returned.
     */
    public static Point2D nearestPoint(Point2D point, Line2D line) {
        return nearestPoint(point.getX(), point.getY(), line);
    }

    public static Point2D nearestPoint(double x, double y, Line2D line) {
        Point2D nearest = nearestPoint(x, y, line.getX1(), line.getY1(), slope(line));

        // We can use the x coordinates to determine if the nearest point is on the line segment, unless the line is
        // vertical. But then, we can just use the y coordinate.
        if (line.getX1() == line.getX2()) {
            // Use y.
            if (line.getY1() < line.getY2()) {
                if (nearest.getY() < line.getY1())
                    return line.getP1();
                if (nearest.getY() <= line.getY2())
                    return nearest;
                return line.getP2();
            }

            if (nearest.getY() < line.getY2())
                return line.getP2();
            if (nearest.getY() <= line.getY1())
                return nearest;
            return line.getP1();
        }

        if (line.getX1() < line.getX2()) {
            if (nearest.getX() < line.getX1())
                return line.getP1();
            if (nearest.getX() <= line.getX2())
                return nearest;
            return line.getP2();
        }

        if (nearest.getX() < line.getX2())
            return line.getP2();
        if (nearest.getX() <= line.getX1())
            return nearest;
        return line.getP1();
    }

    public static Point2D nearestPoint(Point2D source, Area target) {
        if (target == null || target.isEmpty())
            return null;

        if (target.contains(source))
            return source;

        Point2D nearestPoint = null;
        double nearestDistanceSq = 0;
        Point2D point;
        double distanceSq;
        Line2D line = new Line2D.Double();

        PathIterator pathIterator = target.getPathIterator(null);
        double[] coords = new double[6];
        while (!pathIterator.isDone()) {
            int segType = pathIterator.currentSegment(coords);
            if (segType == PathIterator.SEG_MOVETO)
                line.setLine(coords[0], coords[1], 0, 0);
            else if (segType == PathIterator.SEG_LINETO) {
                // Shift the line segment.
                line.setLine(coords[0], coords[1], line.getX1(), line.getY1());
                point = nearestPoint(source, line);
                distanceSq = source.distanceSq(point);
                if (nearestPoint == null || nearestDistanceSq > distanceSq) {
                    nearestPoint = point;
                    nearestDistanceSq = distanceSq;
                }
            }
            else if (segType == PathIterator.SEG_QUADTO)
                throw new NotImplementedException(); // Possibly can use path flattening to deal with this easily.
            else if (segType == PathIterator.SEG_CUBICTO)
                throw new NotImplementedException(); // Possibly can use path flattening to deal with this easily.

            pathIterator.next();
        }

        return nearestPoint;
    }

    public static List<Area> splitNoncontiguousAreas(Area area) {
        List<Area> result = new ArrayList<Area>();
        splitNoncontiguousAreas(area, result);
        return result;
    }

    public static void splitNoncontiguousAreas(Area area, List<Area> result) {
        if (area != null && !area.isEmpty()) {
            Path2D path = new Path2D.Double();
            PathIterator pathIterator = area.getPathIterator(null);
            double[] coords = new double[6];
            while (!pathIterator.isDone()) {
                int segType = pathIterator.currentSegment(coords);
                if (segType == PathIterator.SEG_MOVETO)
                    path.moveTo(coords[0], coords[1]);
                else if (segType == PathIterator.SEG_LINETO)
                    path.lineTo(coords[0], coords[1]);
                else if (segType == PathIterator.SEG_QUADTO)
                    path.quadTo(coords[0], coords[1], coords[2], coords[3]);
                else if (segType == PathIterator.SEG_CUBICTO)
                    path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                else if (segType == PathIterator.SEG_CLOSE) {
                    result.add(new Area(path));
                    path.reset();
                }
                pathIterator.next();
            }
        }
    }

    public static double round(double a, int decimalPlaces) {
        if (a == 0)
            return 0;
        double power = StrictMath.pow(10, decimalPlaces);
        return StrictMath.round(a * power) / power;
    }

    public static double drag(double d, double dragAmount) {
        if (dragAmount < 0)
            dragAmount = -dragAmount;

        if (d < 0) {
            d += dragAmount;
            if (d > 0)
                d = 0;
        }
        else {
            d -= dragAmount;
            if (d < 0)
                d = 0;
        }

        return d;
    }

    public static void main(String[] args) {
        Point2D source = new Point2D.Double(2, 0);

        Path2D trap = new Path2D.Double();
        trap.moveTo(1, 2);
        trap.lineTo(2, 4);
        trap.lineTo(6, 4);
        trap.lineTo(7, 2);
        trap.closePath();

        Path2D trig = new Path2D.Double();
        trig.moveTo(2, 1);
        trig.lineTo(4, 6);
        trig.lineTo(6, 1);
        trig.closePath();

        Area a = new Area(trap);
        a.subtract(new Area(trig));

        System.out.println(nearestPoint(source, a));
    }
}
