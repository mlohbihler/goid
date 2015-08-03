package com.serotonin.goid.task.collector;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.serotonin.goid.applet.MainPanel;
import com.serotonin.goid.util.BasicEnvironment;
import com.serotonin.goid.util.RenderUtils;
import com.serotonin.goid.util2d.ShapeIndex;

public class CollectorEnvironment extends BasicEnvironment {
    private static final Color TARGET_COLOR = new Color(0xA0, 0xA0, 0);
    private static final int TARGET_ADD_SIGMA = 20;

    public static final int TARGET_SIZE = 6;

    private final List<TargetCenter> targetCenters = new CopyOnWriteArrayList<TargetCenter>();
    private CollectorBody collectorBody;
    private final ShapeIndex<Shape> targetIndex = new ShapeIndex<Shape>();
    private int newTargetDelay;

    public void init() {
        // Create the target centers.
        while (targetCenters.size() < 3) {
            int x = MainPanel.RANDOM.nextInt(1200) + 100;
            int y = MainPanel.RANDOM.nextInt(700) + 200;

            if (getIndex().findContainers(x, y).isEmpty())
                targetCenters.add(new TargetCenter(new Point2D.Double(x, y)));
        }
    }

    public void setCollectorBody(CollectorBody collectorBody) {
        this.collectorBody = collectorBody;
        add(collectorBody);
    }

    public List<Shape> findTargets(Shape perception) {
        return targetIndex.findIntersecting(perception);
    }

    @Override
    public void next(long turn) {
        super.next(turn);

        collectorBody.setTargetCollected(false);
        for (Shape picked : targetIndex.findContained(collectorBody.getShape())) {
            targetIndex.remove(picked);
            collectorBody.setTargetCollected(true);
        }

        if (newTargetDelay <= 0) {
            addTarget();
            newTargetDelay = targetIndex.size() * targetIndex.size();
        }

        newTargetDelay--;
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(TARGET_COLOR);
        for (Shape target : targetIndex) {
            if (RenderUtils.isWithinClipBounds(g, target))
                g.fill(target);
        }

        super.render(g);
    }

    private void addTarget() {
        addTarget(targetCenters.get(MainPanel.RANDOM.nextInt(targetCenters.size())));
    }

    private void addTarget(TargetCenter center) {
        int attempts = 100;
        while (true) {
            if (attempts == 0) {
                // System.out.println("Target placement aborted");
                break;
            }
            attempts--;

            double x = MainPanel.RANDOM.nextGaussian() * TARGET_ADD_SIGMA + center.getPoint().getX();
            double y = MainPanel.RANDOM.nextGaussian() * TARGET_ADD_SIGMA + center.getPoint().getY();

            Shape target = new Ellipse2D.Double(x - (TARGET_SIZE >> 1), y - (TARGET_SIZE >> 1), TARGET_SIZE,
                    TARGET_SIZE);

            if (!getIndex().findIntersecting(target).isEmpty())
                continue;
            if (!targetIndex.findIntersecting(target).isEmpty())
                continue;

            targetIndex.put(target);

            break;
        }
    }
}
