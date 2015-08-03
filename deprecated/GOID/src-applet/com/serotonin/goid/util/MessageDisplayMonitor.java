package com.serotonin.goid.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class MessageDisplayMonitor implements Runnable {
    private static final int COLORS = 40;
    private static final int BACKGROUND_MARGIN = 10;

    private final Component component;
    private final Color border = new Color(0x88, 0x88, 0x88);
    private final Color back = new Color(0xff, 0xff, 0xff, 0x80);
    private final Color fore = new Color(0x55, 0x55, 0x55);

    private Font font;
    private String message;
    private int remainder;

    public MessageDisplayMonitor(Component component) {
        this.component = component;
    }

    public void setMessage(String message) {
        if (message == null)
            remainder = 0;
        else
            // The additional amount here is the amount of "time" that the message will be displayed at full alpha.
            remainder = COLORS + 10;

        this.message = message;

        synchronized (this) {
            notify();
        }
    }

    public void run() {
        long waitTime;
        while (true) {
            if (remainder > 0) {
                remainder--;
                waitTime = 30;
                component.repaint();
            }
            else
                waitTime = 2000;

            synchronized (this) {
                try {
                    wait(waitTime);
                }
                catch (InterruptedException e) {
                }
            }
        }
    }

    public void render(Graphics2D g, Dimension size) {
        if (font == null)
            font = g.getFont().deriveFont(Font.BOLD, 26);

        int localRemainder = remainder;
        String localMessage = message;
        if (localRemainder > 0 && localMessage != null) {
            if (localRemainder > COLORS)
                localRemainder = COLORS;

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((float) localRemainder) / COLORS));

            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            int width = fm.stringWidth(localMessage);
            int height = fm.getAscent();

            // Center
            // int x = (size.width - width) >> 1;
            // int y = size.height >> 1;

            // Lower right
            int x = size.width - width - BACKGROUND_MARGIN - BACKGROUND_MARGIN;
            int y = size.height - fm.getDescent() - BACKGROUND_MARGIN - BACKGROUND_MARGIN;

            int mx = x - BACKGROUND_MARGIN;
            int my = y - height - BACKGROUND_MARGIN;
            int mw = width + BACKGROUND_MARGIN + BACKGROUND_MARGIN;
            int mh = height + fm.getDescent() + BACKGROUND_MARGIN + BACKGROUND_MARGIN;

            g.setColor(back);
            g.fillRoundRect(mx, my, mw, mh, 10, 10);

            g.setColor(border);
            g.drawRoundRect(mx, my, mw, mh, 10, 10);

            g.setColor(fore);
            g.drawString(localMessage, x, y);
        }
    }
}
