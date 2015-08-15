/*
 * Copyright (c) 2015, Serotonin Software Inc.
 *
 * This file is part of GoID.
 *
 * GoID is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of 
 * the License, or (at your option) any later version.
 *
 * GoID is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public 
 * License along with GoID. If not, see <http://www.gnu.org/licenses/>.
 */
package goid.simulation;

import goid.simulation.util.FloatPoint;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Matthew Lohbihler
 */
public class Viewer extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final float MAX_ZOOM = 10;
    private static final float MIN_ZOOM = 0.1F;
    private static final float ZOOM_CHANGE = 1.1F;

    // The current location of the mouse in panel coordinates
    final Point mouseOffset = new Point();
    // The offset, in world coordinates, of the current view.
    final FloatPoint worldOffset = new FloatPoint();
    float zoom = 1;
    AffineTransform tx = new AffineTransform();

    Environment env;

    public Viewer(Environment environment) {
        env = environment;

        worldOffset.set(0, 0);

        MouseAdapter mouseListener = new MouseAdapter() {
            Point origin = new Point();

            @Override
            public void mousePressed(MouseEvent e) {
                origin.x = e.getX();
                origin.y = e.getY();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseOffset.setLocation(e.getPoint());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                pan(e.getX() - origin.x, e.getY() - origin.y);
                origin.x = e.getX();
                origin.y = e.getY();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() > 0 && zoom < MAX_ZOOM)
                    zoom(zoom * ZOOM_CHANGE, e.getX(), e.getY());
                else if (e.getWheelRotation() < 0 && zoom > MIN_ZOOM)
                    zoom(zoom / ZOOM_CHANGE, e.getX(), e.getY());
            }
        };

        addMouseMotionListener(mouseListener);
        addMouseListener(mouseListener);
        addMouseWheelListener(mouseListener);

        setDoubleBuffered(true);
    }

    void pan(int x, int y) {
        worldOffset.add(-x * zoom, -y * zoom);
    }

    void zoom(float newZoom, int x, int y) {
        if (newZoom > MAX_ZOOM)
            newZoom = MAX_ZOOM;
        else if (newZoom < MIN_ZOOM)
            newZoom = MIN_ZOOM;

        worldOffset.add(x * (zoom - newZoom), y * (zoom - newZoom));

        zoom = newZoom;
    }

    @Override
    public void paint(Graphics g1) {
        super.paint(g1);

        Graphics2D g = (Graphics2D) g1;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        // Save the transform
        AffineTransform origTx = g.getTransform();
        tx.setTransform(origTx);
        g.setTransform(tx);

        g.scale(1 / zoom, 1 / zoom);
        g.translate(-worldOffset.xInt(), -worldOffset.yInt());

        env.getCtx().iterateElementsCopy(e -> e.render(g));

        // Reset the transform
        g.setTransform(origTx);

        // Print some stats on the graphics
        g.drawString(formatTime(), 10, 20);

        g.drawString("Mouse: " + mouseOffset.x + "," + mouseOffset.y, 10, 35);
        Point w = viewToWorldCoordinates(mouseOffset.x, mouseOffset.y);
        g.drawString("World: " + w.x + "," + w.y, 10, 50);
        g.drawString("Zoom: " + zoom, 10, 65);
    }

    private String formatTime() {
        long time = env.getCtx().getElapsedTime();
        StringBuilder sb = new StringBuilder();

        // millis
        sb.append('.').append(StringUtils.leftPad(Long.toString(time % 1000), 3, '0'));
        time /= 1000;

        // seconds
        sb.insert(0, StringUtils.leftPad(Long.toString(time % 60), 2, '0')).insert(0, ':');
        time /= 60;

        // minutes
        sb.insert(0, StringUtils.leftPad(Long.toString(time % 60), 2, '0')).insert(0, ':');
        time /= 60;

        // hours
        sb.insert(0, StringUtils.leftPad(Long.toString(time % 24), 2, '0'));
        time /= 24;

        if (time > 0) {
            sb.insert(0, "d ");
            sb.insert(0, time);
        }

        return sb.toString();
    }

    Point viewToWorldCoordinates(int x, int y) {
        return new Point((int) (x * zoom + worldOffset.x()), (int) (y * zoom + worldOffset.y()));
    }
}
