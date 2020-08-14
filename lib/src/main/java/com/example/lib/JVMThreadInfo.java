package com.example.lib;

import java.io.Serializable;
import java.util.List;

public class JVMThreadInfo implements Serializable {
    private String id;
    private String name;
    private List<String> traceElement;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTraceElement() {
        return traceElement;
    }

    public void setTraceElement(List<String> traceElement) {
        this.traceElement = traceElement;
    }
}
