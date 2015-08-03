package com.serotonin.goid.task.arm;

import com.serotonin.goid.util.Actuators;

public class ArmActuators implements Actuators {
    public double shoulderTorque;
    public double elbowTorque;

    public void clear() {
        shoulderTorque = 0;
        elbowTorque = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Actuators(");
        sb.append("shoulderTorque=").append(shoulderTorque).append(", ");
        sb.append("elbowTorque=").append(elbowTorque).append(")");
        return sb.toString();
    }
}
