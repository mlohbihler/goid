package com.serotonin.goid.task.arm;

import com.serotonin.goid.applet.TaskListener;
import com.serotonin.goid.util.TurnListener;

public class CompletionMonitor implements TurnListener {
    private TaskListener taskListener;
    private final ArmBody armBody;
    private final int requiredTargets;

    public CompletionMonitor(ArmBody armBody, int requiredTargets) {
        this.armBody = armBody;
        this.requiredTargets = requiredTargets;
    }

    public void setTaskListener(TaskListener taskListener) {
        this.taskListener = taskListener;
    }

    public void next(long turn) {
        int left = requiredTargets - armBody.getTargetsCollected();
        if (left > 0) {
            if (armBody.isTargetCollected()) {
                String message;
                if (left == 1)
                    message = "1 target left";
                else
                    message = Integer.toString(left) + " targets to go";
                taskListener.taskMessage(message);
            }
        }
        else {
            double energy = armBody.getEnergyUsed();
            String details = "time=" + turn + ", energy=" + energy;
            int score = 50000 - (int) turn - (int) (energy * 2);
            taskListener.taskCompleted(score, details, "Task completed. Score: " + score);
        }
    }
}
