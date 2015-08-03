package com.serotonin.goid.task.collector;

import javax.script.CompiledScript;

import com.serotonin.goid.applet.Task;
import com.serotonin.goid.applet.TaskListener;
import com.serotonin.goid.util.ControlScript;
import com.serotonin.goid.util.Environment;
import com.serotonin.goid.util2d.Obstacle;

public class CollectorTask implements Task {
    private CollectorBody collectorBody;
    private final ControlScript bugScript = new ControlScript();
    private CollectorEnvironment environment;
    private CompletionMonitor collectorTaskMonitor;
    private TaskListener taskListener;

    public CollectorTask() {
        reset();
    }

    public double getInitialTranslationX() {
        return 0;
    }

    public double getInitialTranslationY() {
        return 0;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void reset() {
        environment = new CollectorEnvironment();
        collectorBody = new CollectorBody(environment, bugScript);

        collectorTaskMonitor = new CompletionMonitor(collectorBody, 100);
        collectorTaskMonitor.setTaskListener(taskListener);

        environment.add(collectorTaskMonitor);
        environment.add(new Obstacle(Data.CAVE, 800, 100));
        environment.setCollectorBody(collectorBody);

        environment.init();
    }

    public void setScript(CompiledScript script) {
        bugScript.setScript(script);
    }

    public void clearScriptContext() {
        collectorBody.resetContext();
        bugScript.resetContext();
    }

    public void setTaskListener(TaskListener taskListener) {
        this.taskListener = taskListener;
        bugScript.setTaskListener(taskListener);
        if (collectorTaskMonitor != null)
            collectorTaskMonitor.setTaskListener(taskListener);
    }

    public void setDisplayAgentInfo(boolean display) {
        collectorBody.setDisplayAgentStates(display);
    }
}
