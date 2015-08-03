package com.serotonin.goid.task.donut;

import com.serotonin.goid.util.Actuators;

public class BugMotors implements Actuators {
    public double forwardMovement;
    public double backwardMovement;
    public double turn;

    public void clear() {
        forwardMovement = 0;
        backwardMovement = 0;
        turn = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Actuators(");
        sb.append("forwardMovement=").append(forwardMovement).append(", ");
        sb.append("backwardMovement=").append(backwardMovement).append(", ");
        sb.append("turn=").append(turn).append(")");
        return sb.toString();
    }
}
