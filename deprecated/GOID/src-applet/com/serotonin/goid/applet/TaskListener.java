package com.serotonin.goid.applet;

import javax.script.ScriptException;

public interface TaskListener {
    ScriptOutputHandler getScriptOutputHandler();

    void scriptException(ScriptException e);

    void scriptOutput(String output);

    void taskMessage(String message);

    void taskCompleted(int score, String resultDetails, String message);

    String getDispatcherTime();
}
