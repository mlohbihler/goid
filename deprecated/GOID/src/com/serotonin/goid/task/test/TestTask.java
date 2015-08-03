package com.serotonin.goid.task.test;

import java.awt.Graphics2D;

import javax.script.CompiledScript;

import com.serotonin.goid.applet.Task;
import com.serotonin.goid.applet.TaskListener;
import com.serotonin.goid.util.Environment;

public class TestTask implements Task {
    public double getInitialTranslationX() {
        return 0;
    }

    public double getInitialTranslationY() {
        return 0;
    }

    public String getSampleScript() {
        return "// This is the sample script\n";
    }

    public void setScript(CompiledScript script) {
        // no op
    }

    public void clearScriptContext() {
        // no op
    }

    public Environment getEnvironment() {
        return new Environment() {
            public void render(Graphics2D g) {
                g.drawString("This is a test", 100, 100);
            }

            public void next(long turn) {
                // no op
            }
        };
    }

    public void setTaskListener(TaskListener taskListener) {
        // no op
    }

    public void reset() {
        // no op
    }

    public void setDisplayAgentInfo(boolean display) {
        // no op
    }
}
