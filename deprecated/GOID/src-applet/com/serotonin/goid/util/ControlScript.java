package com.serotonin.goid.util;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import com.serotonin.goid.applet.ScriptUtils;
import com.serotonin.goid.applet.TaskListener;

public class ControlScript {
    public static final String KEY_SENSES = "senses";
    public static final String KEY_ACTUATORS = "actuators";

    private static final ScriptUtils scriptUtils = new ScriptUtils();
    private CompiledScript script;
    // private final SimpleScriptContext context = new SimpleScriptContext();
    private TaskListener taskListener;
    private final SimpleBindings globalScope;

    public ControlScript() {
        globalScope = new SimpleBindings();
        resetContext();
    }

    public void setTaskListener(TaskListener taskListener) {
        this.taskListener = taskListener;
    }

    public void execute(ScriptContext context) {
        context.setBindings(globalScope, ScriptContext.GLOBAL_SCOPE);
        ((Actuators) context.getAttribute(KEY_ACTUATORS, ScriptContext.ENGINE_SCOPE)).clear();

        if (script != null) {
            try {
                script.eval(context);
            }
            catch (ScriptException e) {
                if (taskListener != null)
                    taskListener.scriptException(e);
                else
                    e.printStackTrace();
                script = null;
            }
        }
    }

    public void setScript(CompiledScript script) {
        this.script = script;
    }

    public void resetContext() {
        globalScope.put("Utils", scriptUtils);
        if (taskListener != null)
            globalScope.put("console", taskListener.getScriptOutputHandler());
    }

    // public static void main(String[] args) throws Exception {
    // BugScript.listener = new ScriptedBugBehaviourListener() {
    // @Override
    // public void outputMessage(String message) {
    // System.out.println("output message: " + message);
    // }
    //
    // @Override
    // public void scriptException(ScriptException e) {
    // }
    //
    // @Override
    // public void senses(Senses senses) {
    // }
    // };
    //
    // BugScript sab = new BugScript();
    // // sab.script = "12;";
    // // sab.script = "[12,13,14,15];";
    // // sab.setScript("o['a'] = 'now this';");
    // // sab.script = "throw 'Hey Chump!';";
    // // sab.script = "gv;";
    // // sab.script = "ev;";
    // // sab.script = "'asdf';";
    // // sab.setScript("arr[1];");
    //
    // // String script =
    // // "if (typeof(mem) == 'undefined') {"+
    // // "    mema = 0;"+
    // // "    mem = {}; "+
    // // "    mem.m1 = 0;"+
    // // "}"+
    // // "mem.m1++; 1s;"+
    // // "mema = mem.m1;";
    // String script = "mema = 1; " + "map = {a:'b', c:'d', e:true};" +
    // "xarr = [0, 'zxcv', map];"
    // + "console.out('asdf');" + "console.out(senses);" + "console.out(map);" +
    // "console.out(xarr);"
    // + "console.out(mema);";
    // sab.setScript(script);
    //
    // sab.execute(new Senses());
    // // System.out.println(sab.context.getAttribute("mema"));
    // // sab.execute(new Senses());
    // // System.out.println(sab.context.getAttribute("mema"));
    // // sab.execute(new Senses());
    // // System.out.println(sab.context.getAttribute("mema"));
    // // sab.execute(new Senses());
    // // System.out.println(sab.context.getAttribute("mema"));
    //
    // // body.moveForward(1);
    // //
    // // // if (pheromoneCountdown <= 0) {
    // // // body.dropPheromone((short)1);
    // // // pheromoneCountdown = 200;
    // // // }
    // // // else
    // // // pheromoneCountdown--;
    // //
    // // List<Double> obstacles = body.getObstacles();
    // // if (obstacles != null) {
    // // for (Double deviance : obstacles) {
    // // if (deviance < 0) {
    // // if (deviance < -1.24)
    // // body.turn(-1);
    // // else
    // // body.turn(0.05);
    // // }
    // // else {
    // // if (deviance > 1.249) {
    // // if (body.isBlocked())
    // // body.turn(-0.01);
    // // else
    // // body.turn(1);
    // // }
    // // else
    // // body.turn(-0.05);
    // // }
    // // }
    // // }
    // // else
    // // body.turn(0.001);
    //
    // }
}
