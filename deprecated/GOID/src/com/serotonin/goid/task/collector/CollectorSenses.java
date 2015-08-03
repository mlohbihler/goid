package com.serotonin.goid.task.collector;

import java.util.Arrays;

import com.serotonin.goid.util.Senses;
import com.serotonin.goid.util2d.PolarPoint2D;

public class CollectorSenses implements Senses {
    private final String[] state = new String[6];
    public double orientation;
    public PolarPoint2D[] obstacles;
    public PolarPoint2D[] targets;
    public boolean targetCollected;
    public double lastMoveAmount;
    public double lastTurnAmount;

    public CollectorSenses() {
        Arrays.fill(state, "");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Senses(");
        sb.append("orientation=").append(orientation).append(", ");
        sb.append("obstacles=").append(Arrays.toString(obstacles)).append(", ");
        sb.append("targets=").append(Arrays.toString(targets)).append(", ");
        sb.append("targetCollected=").append(targetCollected).append(", ");
        sb.append("lastMoveAmount=").append(lastMoveAmount).append(", ");
        sb.append("lastTurnAmount=").append(lastTurnAmount).append(")");
        return sb.toString();
    }

    public String[] getState() {
        state[0] = "orientation=" + orientation;
        state[1] = "obstacles=" + Arrays.toString(obstacles);
        state[2] = "targets=" + Arrays.toString(targets);
        state[3] = "targetCollected=" + targetCollected;
        state[4] = "lastMoveAmount=" + lastMoveAmount;
        state[5] = "lastTurnAmount=" + lastTurnAmount;
        return state;
    }
}
