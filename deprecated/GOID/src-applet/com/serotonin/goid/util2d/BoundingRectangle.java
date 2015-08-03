package com.serotonin.goid.util2d;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Provides integer bounding of floating point rectangles to avoid floating point rounding errors.
 * 
 * @author Matthew Lohbihler
 */
public class BoundingRectangle {
    public int x;
    public int y;
    public int w;
    public int h = -1;
    
    public BoundingRectangle() {
    }
    
    public BoundingRectangle(Point2D p) {
        add(p);
    }
    
    public BoundingRectangle(Rectangle2D r) {
        add(r);
    }
    
    public void reset() {
        h = -1;
    }
    
    public void add(Point2D p) {
        if (h == -1) {
            x = (int)Math.floor(p.getX());
            y = (int)Math.floor(p.getY());
            w = (int)Math.ceil(p.getX()) - x;
            h = (int)Math.ceil(p.getY()) - y;
        }
        else {
            int temp;
            if (x > p.getX()) {
                temp = x;
                x = (int)Math.floor(p.getX());
                w += temp - x;
            }
            if (y > p.getY()) {
                temp = y;
                y = (int)Math.floor(p.getY());
                h += temp - y;
            }
            if (x + w < p.getX())
                w = (int)Math.ceil(p.getX()) - x;
            if (y + h < p.getY())
                h = (int)Math.ceil(p.getY()) - y;
        }
    }
    
    public void add(Rectangle2D r) {
        if (h == -1) {
            x = (int)Math.floor(r.getX());
            y = (int)Math.floor(r.getY());
            w = (int)Math.ceil(r.getX() + r.getWidth()) - x;
            h = (int)Math.ceil(r.getY() + r.getHeight()) - y;
        }
        else {
            int temp;
            if (x > r.getX()) {
                temp = x;
                x = (int)Math.floor(r.getX());
                w += temp - x;
            }
            if (y > r.getY()) {
                temp = y;
                y = (int)Math.floor(r.getY());
                h += temp - y;
            }
            if (x + w < r.getX() + r.getWidth())
                w = (int)Math.ceil(r.getX() + r.getWidth()) - x;
            if (y + h < r.getY() + r.getHeight())
                h = (int)Math.ceil(r.getY() + r.getHeight()) - y;
        }
    }
    
    public void add(BoundingRectangle r) {
        if (h == -1) {
            x = r.x;
            y = r.y;
            w = r.w;
            h = r.h;
        }
        else {
            int temp;
            if (x > r.x) {
                temp = x;
                x = r.x;
                w += temp - x;
            }
            if (y > r.y) {
                temp = y;
                y = r.y;
                h += temp - y;
            }
            if (x + w < r.x + r.w)
                w = r.x + r.w - x;
            if (y + h < r.y + r.h)
                h = r.y + r.h - y;
        }
    }
    
    public boolean contains(Rectangle2D r) {
        if (h == -1)
            return false;
        return x <= r.getX() && x + w >= r.getX() + r.getWidth() &&
                y <= r.getY() && y + h >= r.getY() + r.getHeight();
    }
    
    public boolean contains(Point2D p) {
        if (h == -1)
            return false;
        return contains(p.getX(), p.getY());
    }
    
    public boolean contains(BoundingRectangle r) {
        if (h == -1)
            return false;
        return x <= r.x && x + w >= r.x + r.w && y <= r.y && y + h >= r.y + r.h;
    }
    
    public boolean contains(double px, double py) {
        if (h == -1)
            return false;
        return x <= px && x + w >= px && y <= py && y + h >= py;
    }
    
    public boolean intersects(Rectangle2D r) {
        if (h == -1)
            return false;
        double maxx = r.getX() + r.getWidth();
        double maxy = r.getY() + r.getHeight();
        return (x <= r.getX() && r.getX() <= x + w || x <= maxx && maxx <= x + w) &&
                (y <= r.getY() && r.getY() <= y + h || y <= maxy && maxy <= y + h);
    }
    
    public int size() {
        if (h == -1)
            return 0;
        return w * h;
    }
    
    @Override
    public String toString() {
        return "BoundingRectangle[x="+ x +",y="+ y +",w="+ w +",h="+ h +"]";
    }
}
