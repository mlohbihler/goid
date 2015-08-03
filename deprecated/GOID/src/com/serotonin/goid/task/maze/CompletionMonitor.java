package com.serotonin.goid.task.maze;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;

import com.serotonin.goid.applet.TaskListener;
import com.serotonin.goid.util.Renderable;
import com.serotonin.goid.util.TurnListener;

public class CompletionMonitor implements TurnListener, Renderable {
    private static final Color TARGET_COLOR = new Color(0xe0, 0xe0, 0xc0);
    private static final Color REGION_COLOR = new Color(0x80, 0xff, 0x80);

    private TaskListener taskListener;
    private final RatBody ratBody;
    private final List<Shape> regions;
    private int nextRegion = 0;

    public CompletionMonitor(RatBody ratBody, List<Shape> regions) {
        this.ratBody = ratBody;
        ratBody.goalLocations = this;
        this.regions = regions;
    }

    public void setTaskListener(TaskListener taskListener) {
        this.taskListener = taskListener;
    }

    public void next(long turn) {
        if (!isComplete()) {
            if (regions.get(nextRegion).contains(ratBody.getBounds())) {
                nextRegion++;
                if (isComplete()) {
                    String time = taskListener.getDispatcherTime();
                    int score = 8000 - ((int) turn);
                    taskListener.taskCompleted(score, time, "Task completed in " + time);
                }
                else {
                    int left = regions.size() - nextRegion;
                    String message;
                    if (left == 1)
                        message = "1 region left";
                    else
                        message = Integer.toString(left) + " regions to go";
                    taskListener.taskMessage(message);
                }
            }
        }
    }

    public boolean isComplete() {
        return nextRegion >= regions.size();
    }

    public void render(Graphics2D g) {
        if (!isComplete()) {
            g.setColor(TARGET_COLOR);
            g.fill(regions.get(nextRegion));
        }

        g.setColor(REGION_COLOR);
        for (Shape region : regions)
            g.draw(region);
    }

    public void reset() {
        nextRegion = 0;
    }

	public Point2D getPositionOfActiveGoal() {
		if (regions.size() > nextRegion) {
			return new Point2D.Double(regions.get(nextRegion).getBounds().getCenterX(), regions.get(nextRegion).getBounds().getCenterY());
		} else {
			return new Point();
		}
	}
}
