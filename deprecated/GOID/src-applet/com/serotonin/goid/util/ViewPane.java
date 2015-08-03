package com.serotonin.goid.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

public class ViewPane extends JComponent {
    private static final long serialVersionUID = 1L;
    private static final double SCALE_FACTOR = 1.5;
    private static final Color COLOR_TIME = new Color(0, 0, 0, 0x80);

    private final Object mutex;
    private final List<Renderable> renderables = new ArrayList<Renderable>();
    private int lastMouseX;
    private int lastMouseY;
    private double translateX;
    private double translateY;
    private double scale = 1;
    private final AffineTransform transform = new AffineTransform();
    private String time = "";
    private final MessageDisplayMonitor messageDisplay;
    private boolean antialias = true;

    public ViewPane(Object mutex) {
        this.mutex = mutex;
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

        MouseHandler handler = new MouseHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);

        addKeyListener(new KeyHandler());

        setOpaque(true);

        messageDisplay = new MessageDisplayMonitor(this);
        Thread monitorThread = new Thread(messageDisplay);
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    public void addRenderable(Renderable r) {
        renderables.add(r);
    }

    public void removeRenderable(Renderable r) {
        renderables.remove(r);
    }

    private void calculateTransform() {
        synchronized (this) {
            transform.setToTranslation(translateX, translateY);
            transform.scale(scale, scale);
        }
        repaint();
    }

    public void setTranslation(double translateX, double translateY) {
        this.translateX = translateX;
        this.translateY = translateY;
        calculateTransform();
    }

    public double getTranslateX() {
        return translateX;
    }

    public double getTranslateY() {
        return translateY;
    }

    public double getScale() {
        return scale;
    }

    public void zoomIn() {
        Dimension size = getSize();
        scale *= SCALE_FACTOR;
        translateX += (translateX - (size.width >> 1)) * (SCALE_FACTOR - 1);
        translateY += (translateY - (size.height >> 1)) * (SCALE_FACTOR - 1);
        calculateTransform();
    }

    public void zoomOut() {
        Dimension size = getSize();
        scale /= SCALE_FACTOR;
        translateX = (translateX + (size.width * (SCALE_FACTOR - 1)) / 2) / SCALE_FACTOR;
        translateY = (translateY + (size.height * (SCALE_FACTOR - 1)) / 2) / SCALE_FACTOR;
        calculateTransform();
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void displayMessage(String message) {
        messageDisplay.setMessage(message);
    }

    public void setAntialias(boolean antialias) {
        this.antialias = antialias;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        // long start = System.currentTimeMillis();
        Graphics2D g = (Graphics2D) graphics;
        AffineTransform oldTransform = g.getTransform();

        Rectangle clip = g.getClipBounds();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        // g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (antialias)
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        else
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        synchronized (this) {
            AffineTransform at = g.getTransform();
            at.concatenate(transform);
            g.setTransform(at);
            // g2.setTransform(transform);

            // g2.setColor(Color.DARK_GRAY);
            // for (int x = clip.x - (clip.x % 100); x <= clip.x + clip.width;
            // x+=100) {
            // for (int y = clip.y - (clip.y % 100); y <= clip.y + clip.height;
            // y+=100)
            // g2.drawLine(x, y, x, y);
            // }

            synchronized (mutex) {
                for (Renderable r : renderables)
                    r.render(g);
            }

            // g2.setColor(Color.BLACK);
            // g2.drawLine(-2, 0, 2, 0);
            // g2.drawLine(0, -2, 0, 2);
        }

        g.setTransform(oldTransform);
        g.setColor(COLOR_TIME);
        FontMetrics fm = getFontMetrics(getFont());
        g.drawString(time, 5, fm.getAscent() + fm.getLeading() + 5);

        messageDisplay.render(g, getSize());

        // System.out.println("Repaint: " + (System.currentTimeMillis() - start) + "ms");
    }

    class MouseHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (!e.isControlDown()) {
                ViewPane.this.requestFocus();
                saveCoords(e);
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!e.isControlDown()) {
                translateX += e.getX() - lastMouseX;
                translateY += e.getY() - lastMouseY;
                saveCoords(e);
                calculateTransform();
            }
        }

        private void saveCoords(MouseEvent e) {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            // setToolTipText(Integer.toString(translateX) +","+
            // Integer.toString(translateY));
        }
    }

    class KeyHandler extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.isControlDown()) {
                switch (e.getKeyChar()) {
                case '+':
                    zoomIn();
                    break;
                case '-':
                    zoomOut();
                    break;
                }
            }
        }
    }
}
