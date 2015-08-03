package com.serotonin.goid.task.maze;

import javax.script.CompiledScript;

import com.serotonin.goid.applet.Task;
import com.serotonin.goid.applet.TaskListener;
import com.serotonin.goid.util.BasicEnvironment;
import com.serotonin.goid.util.ControlScript;
import com.serotonin.goid.util.Environment;
import com.serotonin.goid.util2d.Circle;
import com.serotonin.goid.util2d.Obstacle;

public class MazeTask implements Task {
    private RatBody ratBody;
    private final ControlScript ratScript = new ControlScript();
    private BasicEnvironment environment;
    private CompletionMonitor ratTaskMonitor;
    private TaskListener taskListener;

    public MazeTask() {
        reset();
    }

    public double getInitialTranslationX() {
        return 290;
    }

    public double getInitialTranslationY() {
        return 290;
    }

    public void setScript(CompiledScript script) {
        ratScript.setScript(script);
    }

    public void clearScriptContext() {
        ratBody.resetContext();
        ratScript.resetContext();
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setTaskListener(TaskListener taskListener) {
        this.taskListener = taskListener;
        ratScript.setTaskListener(taskListener);
        if (ratTaskMonitor != null)
            ratTaskMonitor.setTaskListener(taskListener);
    }

    public void reset() {
        environment = new BasicEnvironment();
        ratBody = new RatBody(environment, ratScript, new Circle(0, 0, RatBody.RADIUS));
        ratBody.setOrientation(Math.random() * 100);

        WorldBuilder mazeBuild = new WorldBuilder(10, 10);

        ratTaskMonitor = new CompletionMonitor(ratBody, mazeBuild.getGoalRegions());
        ratTaskMonitor.setTaskListener(taskListener);

        environment.add(ratTaskMonitor);
        environment.add(new Obstacle(mazeBuild.makeMazePoints(), 0, 0));
        environment.add(new Obstacle(mazeBuild.makeMazeCap(), 0, 0));
        environment.add(ratBody);
    }

    public void setDisplayAgentInfo(boolean display) {
        ratBody.setDisplayAgentStates(display);
    }
}
