package com.serotonin.goid.applet;

import javax.script.CompiledScript;

import com.serotonin.goid.util.Environment;

public interface Task {
    double getInitialTranslationX();

    double getInitialTranslationY();

    void setScript(CompiledScript script);

    void clearScriptContext();

    Environment getEnvironment();

    void setTaskListener(TaskListener taskListener);

    void reset();

    void setDisplayAgentInfo(boolean display);
}
