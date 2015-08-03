package com.serotonin.goid.tools;

public class Test {
    // public static void main(String[] args) {
    // String response = "rank=1, changed=false, oldScore=-20000, score=-20000\n";
    //
    // int rank = StringUtils.parseInt(StringUtils.findGroup(Pattern.compile("rank=(\\d*)"), response), -1);
    // String changedStr = StringUtils.findGroup(Pattern.compile("changed=(.*?)"), response);
    // boolean changed = "1".equals(changedStr) || "true".equals(changedStr);
    // }

    // public static void main(String[] args) throws Exception {
    // // Creates and enters a Context. The Context stores information
    // // about the execution environment of a script.
    // Context cx = Context.enter();
    // try {
    // Scriptable scope = cx.initStandardObjects();
    // StringBuilder sb = new StringBuilder();
    // sb.append("if (typeof(x) == 'undefined')\n");
    // sb.append("    x = input.v1;\n");
    // sb.append("x++;");
    // sb.append("if (x >= 10) {\n");
    // sb.append("    if (typeof(y) == 'undefined')\n");
    // sb.append("        y = 0;\n");
    // sb.append("    y++;\n");
    // sb.append("}");
    //
    // Script script = cx.compileString(sb.toString(), "goid script", 1, null);
    //
    // Input input1 = new Input();
    // input1.v1 = 7;
    // input1.v2 = 4.2;
    // Output output1 = new Output();
    // Map<String, Object> scriptContext1 = new HashMap<String, Object>();
    // scriptContext1.put("input", Context.javaToJS(input1, scope));
    // scriptContext1.put("output", Context.javaToJS(output1, scope));
    //
    // Input input2 = new Input();
    // input2.v1 = 6;
    // input2.v2 = 7.5;
    // Output output2 = new Output();
    // Map<String, Object> scriptContext2 = new HashMap<String, Object>();
    // scriptContext2.put("input", Context.javaToJS(input2, scope));
    // scriptContext2.put("output", Context.javaToJS(output2, scope));
    //
    // // ScriptableObject.putProperty(agentContext2, "input", input2);
    // // ScriptableObject.putProperty(agentContext2, "output", Context.javaToJS(output2, agentContext2));
    // // script.exec(cx, agentContext2);
    //
    // for (int i = 0; i < 1000; i++) {
    // exec(script, scope, scriptContext1);
    // exec(script, scope, scriptContext2);
    // }
    //
    // System.out.println(scriptContext1);
    // System.out.println(scriptContext2);
    // }
    // catch (RhinoException e) {
    // System.out.println(e.getMessage());
    // System.out.println("line: " + e.lineNumber());
    // System.out.println("column: " + e.columnNumber());
    // System.out.println("details: " + e.details());
    // System.out.println("lineSource: " + e.lineSource());
    // System.out.println("sourceName: " + e.sourceName());
    // }
    // finally {
    // // Exit from the context.
    // Context.exit();
    // }
    // }
    //
    // public static void exec(Script script, Scriptable scope, Map<String, Object> scriptContext) {
    // Context cx = Context.enter();
    // try {
    // for (Map.Entry<String, Object> entry : scriptContext.entrySet())
    // scope.put(entry.getKey(), scope, entry.getValue());
    //
    // script.exec(cx, scope);
    //
    // for (Object oid : scope.getIds()) {
    // String id = (String) oid;
    // scriptContext.put(id, scope.get(id, scope));
    // scope.delete(id);
    // }
    // }
    // finally {
    // Context.exit();
    // }
    // }
    //
    // public static class Input {
    // public int v1;
    // public double v2;
    // }
    //
    // public static class Output {
    // public double result;
    // }
}
