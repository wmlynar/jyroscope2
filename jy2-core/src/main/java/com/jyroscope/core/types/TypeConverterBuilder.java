package com.jyroscope.core.types;

import com.jyroscope.util.*;
import java.util.*;

public class TypeConverterBuilder {

    private static final String NAMESPACE = "com.jyroscope.dynamic";

    private String id;
    private String fromType;
    private String toType;
    private String output;
    private ArrayList<MethodBuilder> methods;
    
    public TypeConverterBuilder(String fromType, String toType) {
        this.id = "Convert_" + Id.generate(fromType, "to", toType);
        this.fromType = fromType;
        this.toType = toType;
        this.methods = new ArrayList<>();
    }
    
    public String getSource() {
        StringBuilder buffer = new StringBuilder();
        String template = 
                "package #1;\n" +
                "public class #2 extends TypeConverter<#3,#4> {\n" +
                "  @Override\n" +
                "  public #4 convert(#3 source) {\n" + 
                "    return #5;\n" +
                "  }\n";
        buffer.append(Template.apply(null, template, NAMESPACE, id, fromType, toType, output));
        for (MethodBuilder method : methods)
            method.makeMethod(buffer);
        buffer.append("}");
        return buffer.toString();
    }
    
    public String getInput() {
        return "source";
    }
    
    public void setOutput(String result) {
        this.output = result;
    }
    
    public MethodBuilder createMethod(String returnType, String... argumentTypes) {
        MethodBuilder result = new MethodBuilder(returnType, argumentTypes);
        methods.add(result);
        return result;
    }

    public String getName() {
        return NAMESPACE + "." + id;
    }

}
