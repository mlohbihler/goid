package com.serotonin.goid.task.arm;

import java.awt.geom.Point2D;
import java.util.Arrays;

import com.serotonin.goid.util.Senses;

public class ArmSenses implements Senses {
    private final String[] state = new String[11];
    public Point2D targetLocation;
    public Point2D wristToTarget;
    public Point2D elbowToTarget;
    public Point2D wristLocation;
    public double shoulderAngle;
    public double elbowAngle;
    public double shoulderMomentum;
    public double elbowMomentum;
    public double realForearmAngle;
    public boolean targetCollected;
    public double energyUsed;

    public ArmSenses() {
        Arrays.fill(state, "");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Senses(");
        sb.append("targetLocation=").append(targetLocation).append(", ");
        sb.append("wristToTarget=").append(wristToTarget).append(", ");
        sb.append("elbowToTarget=").append(elbowToTarget).append(", ");
        sb.append("wristLocation=").append(wristLocation).append(", ");
        sb.append("shoulderAngle=").append(shoulderAngle).append(", ");
        sb.append("elbowAngle=").append(elbowAngle).append(", ");
        sb.append("shoulderMomentum=").append(shoulderMomentum).append(", ");
        sb.append("elbowMomentum=").append(elbowMomentum).append(", ");
        sb.append("realForearmAngle=").append(realForearmAngle).append(", ");
        sb.append("targetCollected=").append(targetCollected).append(", ");
        sb.append("energyUsed=").append(energyUsed).append(")");
        return sb.toString();
    }

    public String[] getState() {
        state[0] = "targetLocation=" + targetLocation;
        state[1] = "wristToTarget=" + wristToTarget;
        state[2] = "elbowToTarget=" + elbowToTarget;
        state[3] = "wristLocation=" + wristLocation;
        state[4] = "shoulderAngle=" + shoulderAngle;
        state[5] = "elbowAngle=" + elbowAngle;
        state[6] = "shoulderMomentum=" + shoulderMomentum;
        state[7] = "elbowMomentum=" + elbowMomentum;
        state[8] = "realForearmAngle=" + realForearmAngle;
        state[9] = "targetCollected=" + targetCollected;
        state[10] = "energyUsed=" + energyUsed;
        return state;
    }
}
