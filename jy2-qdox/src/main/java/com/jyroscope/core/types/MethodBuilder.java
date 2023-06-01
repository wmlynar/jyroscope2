package com.jyroscope.core.types;

import com.jyroscope.util.*;

public class MethodBuilder {
    
    private final String name;
    private final String returnType;
    private final String[] argumentTypes;
    private final StringBuilder body;
    
    public MethodBuilder(String returnType, String... argumentTypes) {
        this.name = Id.generate();
        this.returnType = returnType;
        this.argumentTypes = argumentTypes;
        this.body = new StringBuilder();
    }
    
    public String getName() {
        return name;
    }
    
    public String getInvocation(String... arguments) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(name).append("(");
        boolean first = true;
        for (int i=0; i<argumentTypes.length; i++) {
            if (first)
                first = false;
            else
                buffer.append(", ");
            buffer.append(arguments[i]);
        }
        buffer.append(")");
        return buffer.toString();
    }
    
    public String getArg(int index) {
        return "arg_" + index;
    }
    
    public void setResult(String result) {
        add(Template.apply(null, "result = #1;", result));
    }
    
    public String getResult() {
        return "result";
    }
    
    public void add(String statement) {
        body.append(statement).append("\n");
    }
    
    public void add(StringBuilder statement) {
        body.append(statement).append("\n");
    }

    void makeMethod(StringBuilder buffer) {
        if (returnType != null)
            buffer.append(Template.apply(null, "  private #1 #2(", returnType, name));
        else
            buffer.append(Template.apply(null, "  private void #2(", returnType, name));
        boolean first = true;
        for (int i=0; i<argumentTypes.length; i++) {
            if (first)
                first = false;
            else
                buffer.append(", ");
            buffer.append(Template.apply(null, "#1 #2", argumentTypes[i], getArg(i)));
        }
        buffer.append(") {\n");
        if (returnType != null)
            buffer.append(Template.apply(null, "    #1 result;\n", returnType));
        buffer.append(body);
        if (returnType != null)
            buffer.append("    return result;\n");
        buffer.append("  }\n");
    }
    
    public String apply(String template, String... parameters) {
        return Template.apply(body, template, parameters);
    }
    
}
