package com.serotonin.goid.task.maze;

import java.util.Arrays;

import com.serotonin.goid.util.Senses;

public class RatSenses implements Senses {
    private final String[] state = new String[5];
    public double probe = 10;
    public boolean leftEye, rightEye;
	public boolean blocked;
	public double goalDistance;

    public RatSenses() {
        Arrays.fill(state, "");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Senses(");
        sb.append("probe=").append(probe).append(", ");
        sb.append("blocked=").append(blocked).append(", ");
        sb.append("leftEye=").append(leftEye).append(", ");
        sb.append("rightEye=").append(rightEye).append(", ");
        sb.append("goalDistance=").append(goalDistance).append(")");
        return sb.toString();
    }

    public String[] getState() {
    	state[0] = "probe=" + probe;
    	state[1] = "blocked=" + blocked;
        state[2] = "leftEye=" + leftEye;
        state[3] = "rightEye=" + rightEye;
        state[4] = "goalDistance=" + goalDistance;
        return state;
    }
}
