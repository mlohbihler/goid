package com.serotonin.goid.task.donut;

import java.util.Arrays;

import com.serotonin.goid.util.Senses;

public class BugSenses implements Senses {
    private final String[] state = new String[3];
    public double orientation;
    public boolean blocked;
    public double[] obstacles;

    public BugSenses() {
        Arrays.fill(state, "");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Senses(");
        sb.append("orientation=").append(orientation).append(", ");
        sb.append("blocked=").append(blocked).append(", ");
        sb.append("obstacles=").append(Arrays.toString(obstacles)).append(")");
        return sb.toString();
    }

    public String[] getState() {
        state[0] = "orientation=" + orientation;
        state[1] = "blocked=" + blocked;
        state[2] = "obstacles=" + Arrays.toString(obstacles);
        return state;
    }
}
