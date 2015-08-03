package com.serotonin.goid.util;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class AgentInfoRenderer {
    private static final Color STATE_BACK = new Color(0xff, 0xff, 0xff, 0x80);
    private static final Color STATE_FORE = new Color(0x33, 0x33, 0x33);
    private static final int STATE_MARGIN = 3;

    public static void render(Graphics2D g, Senses senses, int x, int y) {
        String[] message = senses.getState();
        FontMetrics fm = g.getFontMetrics();
        int w = 0;
        int i;
        for (i = 0; i < message.length; i++) {
            int sw = fm.stringWidth(message[i]);
            if (w < sw)
                w = sw;
        }

        g.setColor(STATE_BACK);
        g.fillRect(x, y, w + (STATE_MARGIN << 1), fm.getHeight() * message.length + (STATE_MARGIN << 1));
        g.setColor(STATE_FORE);
        g.drawRect(x, y, w + (STATE_MARGIN << 1), fm.getHeight() * message.length + (STATE_MARGIN << 1));

        for (i = 0; i < message.length; i++)
            g.drawString(message[i], x + STATE_MARGIN, y + fm.getHeight() * i + fm.getLeading() + fm.getAscent()
                    + STATE_MARGIN);
    }
}
