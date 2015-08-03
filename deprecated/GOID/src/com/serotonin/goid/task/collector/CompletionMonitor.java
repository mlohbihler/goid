package com.serotonin.goid.task.collector;

import com.serotonin.goid.applet.TaskListener;
import com.serotonin.goid.util.TurnListener;

public class CompletionMonitor implements TurnListener {
    private TaskListener taskListener;
    private final CollectorBody collectorBody;
    private final int requiredTargets;

    public CompletionMonitor(CollectorBody collectorBody, int requiredTargets) {
        this.collectorBody = collectorBody;
        this.requiredTargets = requiredTargets;
    }

    public void setTaskListener(TaskListener taskListener) {
        this.taskListener = taskListener;
    }

    public void next(long turn) {
        int left = requiredTargets - collectorBody.getTargetsCollected();
        if (left > 0) {
            if (collectorBody.isTargetCollected()) {
                String message;
                if (left == 1)
                    message = "1 target left";
                else
                    message = Integer.toString(left) + " target to go";
                taskListener.taskMessage(message);
            }
        }
        else {
            String time = taskListener.getDispatcherTime();
            int score = 20000 - ((int) turn);
            taskListener.taskCompleted(score, time, "Task completed in " + time);
        }
    }
}
