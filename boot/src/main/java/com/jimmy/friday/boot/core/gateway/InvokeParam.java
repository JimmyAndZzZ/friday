package com.jimmy.friday.boot.core.gateway;

import lombok.Data;

import java.io.Serializable;

@Data
public class InvokeParam implements Serializable {

    private String name;

    private String className;

    private String jsonData;

    public InvokeParam() {

    }

    public InvokeParam(String name, String className, String jsonData) {
        this.name = name;
        this.className = className;
        this.jsonData = jsonData;
    }
}
