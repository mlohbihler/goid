package com.serotonin.goid.applet;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import sun.org.mozilla.javascript.internal.NativeArray;
import sun.org.mozilla.javascript.internal.NativeObject;

public class ScriptOutputHandler {
    private TaskListener taskListener;

    public void setListener(TaskListener taskListener) {
        this.taskListener = taskListener;
    }

    public void out(Object o) {
        if (taskListener != null)
            taskListener.scriptOutput(toString(o));
    }

    private String toString(String label, Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append(label).append("(");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (first)
                first = false;
            else
                sb.append(", ");
            sb.append(entry.getKey()).append("=").append(toString(entry.getValue()));
        }
        sb.append(")");
        return sb.toString();
    }

    private String toString(NativeObject o) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Object id : o.getAllIds()) {
            if (first)
                first = false;
            else
                sb.append(", ");
            sb.append(id).append(":").append(toString(o.get((String) id, null)));
        }
        sb.append("}");
        return sb.toString();
    }

    private String toString(NativeArray o) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int len = (int) o.getLength();
        for (int i = 0; i < len; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(toString(o.get(i, null)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String toString(Object o) {
        if (o == null)
            return "null";
        if (o instanceof NativeObject)
            return toString((NativeObject) o);
        if (o instanceof NativeArray)
            return toString((NativeArray) o);
        if (o instanceof String)
            return "\"" + o + "\"";
        if (o instanceof SimpleScriptContext) {
            SimpleScriptContext context = (SimpleScriptContext) o;
            Map<String, Object> map = new HashMap<String, Object>(context.getBindings(ScriptContext.ENGINE_SCOPE));
            map.remove("console");
            map.remove("println");
            map.remove("print");
            map.remove("context");
            return toString("context", map);
        }
        return o.toString();
    }
}
