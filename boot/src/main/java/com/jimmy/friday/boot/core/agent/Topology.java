package com.jimmy.friday.boot.core.agent;

import lombok.Data;

import java.io.Serializable;

@Data
public class Topology implements Serializable {

    private String machine;

    private String application;

    private String type;

}
