package com.serotonin.goid.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

/**
 * NOTE: not for multithreaded use.
 * @author Matthew Lohbihler
 */
public class RenderUtils {
    private static final Rectangle CLIP_BOUNDS = new Rectangle();
    
    public static boolean isWithinClipBounds(Graphics2D g, Shape s) {
        return g.getClipBounds(CLIP_BOUNDS).intersects(s.getBounds());
    }
    
    public static boolean isWithinClipBounds(Graphics2D g, double x, double y) {
        return g.getClipBounds(CLIP_BOUNDS).contains(x, y);
    }
    
    public static void renderOutlined(Graphics2D g, Shape s, Color fill, Color outline) {
        if (isWithinClipBounds(g, s)) {
            g.setColor(fill);
            g.fill(s);
            g.setColor(outline);
            g.draw(s);
        }
    }
    
    public static void renderFill(Graphics2D g, Shape s, Color fill) {
        if (isWithinClipBounds(g, s)) {
            g.setColor(fill);
            g.fill(s);
        }
    }
}
