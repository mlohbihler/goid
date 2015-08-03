package com.serotonin.goid.task.collector;

import com.serotonin.goid.util.Actuators;

public class CollectorActuators implements Actuators {
    public double move;
    public double turn;

    public void clear() {
        move = 0;
        turn = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Actuators(");
        sb.append("move=").append(move).append(", ");
        sb.append("turn=").append(turn).append(")");
        return sb.toString();
    }
}
