package com.jyroscope.types;

import com.jyroscope.util.*;
import java.util.*;

public class BuilderState {
    
    private String contextPath;
    private StringBuilder code;
    private List<MethodBuilder> helpers;
    private List<BuilderState> children;
    
    public BuilderState(String initialPath) {
        this.contextPath = initialPath;
        this.code = new StringBuilder();
    }
    
    public void add(String statement) {
        code.append(statement);
    }
    
    public void add(MethodBuilder method) {
        if (helpers == null)
            helpers = new ArrayList<>();
        helpers.add(method);
    }
    
    public BuilderState createChild(String subPath) {
        BuilderState result = new BuilderState(contextPath + "." + subPath);
        if (children == null)
            children = new ArrayList<>();
        children.add(result);
        return result;
    }
    
    public void commit() {
        if (children != null) {
            for (BuilderState child : children) {
                child.commit();
                this.code.append(child.code);
                if (child.helpers != null) {
                    if (this.helpers == null)
                        this.helpers = child.helpers;
                    else
                        this.helpers.addAll(child.helpers);
                }
            }
        }
        children = null;
    }
    
    public String apply(String template, String... parameters) {
        return Template.apply(code, template, parameters);
    }
    
}
