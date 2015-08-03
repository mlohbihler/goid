package com.serotonin.goid.util2d;

import java.awt.geom.Point2D;

public class PolarPoint2D {
    private double radius;
    private double angle;
    
    public PolarPoint2D() {
        // no op
    }
    
    public PolarPoint2D(Point2D point) {
        radius = Math.sqrt(point.getX()*point.getX() + point.getY()*point.getY());
        if (point.getX() >= 0)
            angle = Math.atan(point.getY() / point.getX());
        else
            angle = Math.atan(point.getY() / point.getX()) + Math.PI;
    }
    
    public PolarPoint2D(double radius, double angle) {
        this.radius = radius;
        this.angle = angle;
    }
    
    public Point2D toCartesian() {
        if (radius <= 0 || Double.isNaN(angle))
            return new Point2D.Double();
        return new Point2D.Double(radius * Math.cos(angle), radius * Math.sin(angle));
    }

    public double getRadius() {
        return radius;
    }
    public void setRadius(double radius) {
        this.radius = radius;
    }
    public double getAngle() {
        return angle;
    }
    public void setAngle(double angle) {
        this.angle = angle;
    }
    
    @Override
    public String toString() {
        return "PolarPoint2D(r="+ radius +",a="+ angle +")";
    }
    
    
    public static void main(String[] args) {
//        System.out.println(new PolarPoint2D(0,0).toCartesian());
//        System.out.println(new PolarPoint2D(1,0).toCartesian());
//        System.out.println(new PolarPoint2D(1,1).toCartesian());
//        System.out.println(new PolarPoint2D(0,1).toCartesian());
//        System.out.println(new PolarPoint2D(-1,1).toCartesian());
//        System.out.println(new PolarPoint2D(-1,0).toCartesian());
//        System.out.println(new PolarPoint2D(-1,-1).toCartesian());
//        System.out.println(new PolarPoint2D(0,-1).toCartesian());
//        System.out.println(new PolarPoint2D(1,-1).toCartesian());
        
        System.out.println(new PolarPoint2D(new Point2D.Double(1,3)).getAngle());
    }
}
