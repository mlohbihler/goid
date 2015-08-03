package com.serotonin.goid.task.arm;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import com.serotonin.goid.applet.MainPanel;
import com.serotonin.goid.util.BasicEnvironment;

public class ArmEnvironment extends BasicEnvironment {
    private static final Color TARGET_COLOR = new Color(0xa0, 0, 0);
    public static final int TARGET_SIZE = 6;

    private ArmBody armBody;
    private final Ellipse2D target = new Ellipse2D.Double(50, 50, TARGET_SIZE, TARGET_SIZE);

    public void init() {
        positionTarget();
    }

    public void setArmBody(ArmBody armBody) {
        this.armBody = armBody;
        add(armBody);
    }

    public Point2D getTargetLocation() {
        return new Point2D.Double(target.getCenterX(), target.getCenterY());
    }

    @Override
    public void next(long turn) {
        super.next(turn);

        if (onTarget()) {
            armBody.setTargetCollected(true);
            positionTarget();
        }
        else
            armBody.setTargetCollected(false);
    }

    private boolean onTarget() {
        return armBody.getWristBounds().contains(target.getBounds2D());
    }

    @Override
    public void render(Graphics2D g) {
        super.render(g);
        g.setColor(TARGET_COLOR);
        g.fill(target);
    }

    private void positionTarget() {
        while (true) {
            int[] p = Data.TARGET_POINTS[MainPanel.RANDOM.nextInt(Data.TARGET_POINTS.length)];
            target.setFrame(p[0] - (TARGET_SIZE >> 1), p[1] - (TARGET_SIZE >> 1), TARGET_SIZE, TARGET_SIZE);
            if (!onTarget())
                break;
        }
    }
}
