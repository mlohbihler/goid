package com.serotonin.goid.task.arm;

import javax.script.CompiledScript;

import com.serotonin.goid.applet.Task;
import com.serotonin.goid.applet.TaskListener;
import com.serotonin.goid.util.ControlScript;
import com.serotonin.goid.util.Environment;

public class ArmTask implements Task {
    private ArmBody armBody;
    private final ControlScript controlScript = new ControlScript();
    private ArmEnvironment environment;
    private CompletionMonitor completionMonitor;
    private TaskListener taskListener;

    public ArmTask() {
        reset();
    }

    public double getInitialTranslationX() {
        return 300;
    }

    public double getInitialTranslationY() {
        return 400;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void reset() {
        environment = new ArmEnvironment();
        armBody = new ArmBody(environment, controlScript);

        completionMonitor = new CompletionMonitor(armBody, 1000);
        completionMonitor.setTaskListener(taskListener);

        environment.add(completionMonitor);
        environment.setArmBody(armBody);

        environment.init();
    }

    public void setScript(CompiledScript script) {
        controlScript.setScript(script);
    }

    public void clearScriptContext() {
        armBody.resetContext();
        controlScript.resetContext();
    }

    public void setTaskListener(TaskListener taskListener) {
        this.taskListener = taskListener;
        controlScript.setTaskListener(taskListener);
        if (completionMonitor != null)
            completionMonitor.setTaskListener(taskListener);
    }

    public void setDisplayAgentInfo(boolean display) {
        armBody.setDisplayAgentStates(display);
    }
}
